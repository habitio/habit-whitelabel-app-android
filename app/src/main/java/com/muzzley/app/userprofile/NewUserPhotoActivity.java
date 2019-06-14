package com.muzzley.app.userprofile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.muzzley.App;
import com.muzzley.Constants;
import com.muzzley.R;
import com.muzzley.util.FeedbackMessages;
import com.muzzley.util.picasso.CircleTransform;
import com.muzzley.util.picasso.ScaledCircleTransform;
import com.muzzley.services.PreferencesRepository;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;
import timber.log.Timber;

/**
 * Beware of the Memory Management within this class!
 *
 * Any calls to fit() or resize() with Picasso 2.5.2 and below
 * will result in the preview images not being loaded.
 * When available, Picasso version 2.5.3 should correct this issue and
 * these calls should be made to avoid memory problems!
 *
 * Created by Paulo on 2/12/2016.
 */
public class NewUserPhotoActivity extends AppCompatActivity {
    @Inject PreferencesRepository preferencesRepository;

    @BindView(R.id.placeholder_photo_edit) ImageView mProfileEditPhoto;
    @BindView(R.id.btn_photo_edit_take_photo) Button mProfileEditTakePhotoButton;
    @BindView(R.id.btn_photo_edit_choose_photo) Button mProfileEditChooseButton;
    @BindView(R.id.btn_photo_edit_save) Button mProfileEditSaveButton;

    private Context mContext;
    private boolean flag = true;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
        setContentView(R.layout.activity_user_profile_edit);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (flag) {
            mContext = this;
            configActionBar();
            setListeners();
            bindUserDataToView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onResult " + requestCode + " " + resultCode);
        flag = false;

        if(resultCode == Activity.RESULT_OK){
            switch (requestCode) {
                case Constants.REQUEST_PROFILE_NEW_PHOTO_CAMERA:
                    if (fileUri != null) {
                        // Load the taken image into a preview
                        loadImage(fileUri);
                    } else {
                        FeedbackMessages.showMessage(mProfileEditPhoto,
                                "Problem taking photo"); // TODO!
                    }
                    break;
                case Constants.REQUEST_PROFILE_NEW_PHOTO_OLD_GALLERY:
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        String selectedImagePath = getSelectedGalleryPath(selectedImageUri);
                        if (selectedImagePath != null) {
                            // Load the taken image into a preview
                            loadImage(selectedImageUri);
                        } else {
                            FeedbackMessages.showMessage(mProfileEditPhoto,
                                    "Problem loading photo (image path)"); // TODO!
                        }
                    } else {
                        FeedbackMessages.showMessage(mProfileEditPhoto,
                                "Problem loading photo"); // TODO!
                    }
                    break;
                case Constants.REQUEST_PROFILE_NEW_PHOTO_NEW_GALLERY:
                    if (Build.VERSION.SDK_INT >= 19 && data != null) { //Build.VERSION_CODES.KITKAT
                        Uri originalUri = data.getData();
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        // Check for the freshest data
                        getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
                        // Load the taken image into a preview
                        loadImage(Uri.parse("file://" + compileSelectedGalleryImagePath(originalUri)));
                    } else {
                        FeedbackMessages.showMessage(mProfileEditPhoto,
                                "Problem loading photo (kitkat code)"); // TODO!
                    }
                    break;
                default:
                    // do nothing ...
                    break;
            }
        }
    }

    private void loadImage(Uri selectedImageUri) {
        Picasso.get()
                .load(selectedImageUri)
                .transform(new ScaledCircleTransform(660, 660))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .into(mProfileEditPhoto);
    }

    @Override
    protected void onDestroy(){
        Timber.d("onDestroy");
        super.onDestroy();
    }

    private void configActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.edit_profile);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void setListeners() {
        mProfileEditTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCameraIntent();
            }
        });

        mProfileEditChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGalleryIntent();
            }
        });

        mProfileEditSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Save the new profile image to backend
                FeedbackMessages.showMessage(mProfileEditPhoto,
                        "Problem saving profile photo"); // TODO!
            }
        });
    }

    private void bindUserDataToView(){
        Picasso.get()
                .load(preferencesRepository.getUser().getPhotoUrl())
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .transform(new CircleTransform())
                .into(mProfileEditPhoto);
    }

    private void launchCameraIntent() {
        // create camera Intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(mContext.getPackageManager()) != null) {
            fileUri = Uri.fromFile(getOutputCameraPhotoFile());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(cameraIntent,
                            getString(R.string.edit_profile_photo_camera_intent_chooser_title)),
                    Constants.REQUEST_PROFILE_NEW_PHOTO_CAMERA);
        }
    }

    private void launchGalleryIntent() {
        if (Build.VERSION.SDK_INT < 19){ //Build.VERSION_CODES.KITKAT
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                            getString(R.string.edit_profile_photo_gallery_intent_chooser_title)),
                            Constants.REQUEST_PROFILE_NEW_PHOTO_OLD_GALLERY);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // TODO: Should be ACTION_GET_CONTENT instead?
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setType("image/*");
            startActivityForResult(intent, Constants.REQUEST_PROFILE_NEW_PHOTO_NEW_GALLERY);
        }
    }

    // For API >= 19
    private String compileSelectedGalleryImagePath(Uri originalUri) {
        /* now extract ID from Uri path using getLastPathSegment() and then split with ":"
           then call get Uri to for Internal storage or External storage for media I have used getUri()
        */
        String id = originalUri.getLastPathSegment().split(":")[1];
        final String[] imageColumns = {MediaStore.Images.Media.DATA };
        final String imageOrderBy = null;

        Uri uri = getUri();
        String selectedImagePath = "path";

        Cursor imageCursor = managedQuery(uri, imageColumns,
                MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);

        if (imageCursor.moveToFirst()) {
            selectedImagePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }
        Timber.d("path " + selectedImagePath); // use selectedImagePath
        return selectedImagePath;
    }

    // By using this method get the Uri of Internal/External Storage for Media
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    // For API < 19
    private String getSelectedGalleryPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // perform some logging or show user feedback
            FeedbackMessages.showMessage(mProfileEditPhoto,
                    "Problem loading photo (uri is null)"); // TODO!
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    public File getOutputCameraPhotoFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir;
        // If the external directory is writable then then return the External pictures directory.
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera/"), "");
        } else {
            mediaStorageDir = Environment.getDownloadCacheDirectory();
        }

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("MyCameraApp failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
}
