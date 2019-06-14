package com.muzzley.app.cards;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.muzzley.Constants;
import com.muzzley.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapEventRequest mapEventRequest;
    @BindView(R.id.map)
    MapView mapView;
    @BindView(R.id.text)
    TextView text;
    private GoogleMap map;
    private Geocoder geocoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapEventRequest = (MapEventRequest) getIntent().getParcelableExtra(Constants.EXTRA_MAP_REQUEST);

        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(this);
        geocoder = new Geocoder(MapsActivity.this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;
        MapsInitializer.initialize(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapEventRequest.location, 13f));
//        map.addMarker(new MarkerOptions().position(mapEventRequest.location));
        map.getUiSettings().setMapToolbarEnabled(false);

//        Observable<LatLng> latLngObservable = Observable.create(new Observable.OnSubscribe<LatLng>() {
//            @Override
//            public void call(final Subscriber<? super LatLng> subscriber) {
//                map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//                    @Override
//                    public void onCameraChange(CameraPosition cameraPosition) {
//                        subscriber.onNext(cameraPosition.target);
//                    }
//                });
//            }
//        }).subscribeOn(AndroidSchedulers.mainThread());

        final PublishSubject<LatLng> latLngObservable = PublishSubject.create();
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                latLngObservable.onNext(cameraPosition.target);
            }
        });

        // check for address only if the user has not moved map for 500ms
        latLngObservable.debounce(500, TimeUnit.MILLISECONDS).flatMap(new Function<LatLng, Observable<String>>() {
            @Override
            public Observable<String> apply(final LatLng target) {

                return Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> observableEmitter) throws Exception {
                        try {
                            List<Address> addresses = geocoder.getFromLocation(target.latitude, target.longitude, 1);
                            if (addresses != null && addresses.size() > 0) {
                                Address address = addresses.get(0);
                                StringBuilder sb = new StringBuilder();
                                int lineCount = address.getMaxAddressLineIndex();
                                for (int i = 0; i < lineCount; i++) {
                                    sb.append(address.getAddressLine(i)).append("\n");
                                }
                                observableEmitter.onNext(sb.toString().trim());
                            }
                            observableEmitter.onComplete();
                        } catch (Exception e) {
                            observableEmitter.onError(e);
                        }
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        text.setText(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Timber.d(throwable, "Error getting address");
                    }
                });

    }

    @OnClick(R.id.button_set_location)
    public void onSetLocation(View view) {

        if (map != null) {
            setResult(RESULT_OK, new Intent().putExtra(Constants.EXTRA_MAP_REQUEST,
                    new MapEventResponse(map.getCameraPosition().target, mapEventRequest.uuid)));
            finish();
        }
    }
}
