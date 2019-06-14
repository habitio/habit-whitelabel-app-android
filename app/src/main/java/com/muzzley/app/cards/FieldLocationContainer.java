package com.muzzley.app.cards;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

//import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.muzzley.model.cards.Field;
import com.muzzley.providers.BusProvider;
import com.muzzley.providers.MainThreadBus;
import com.squareup.otto.Subscribe;

import java.util.UUID;

/**
 * Created by caan on 28-09-2015.
 */
public class FieldLocationContainer extends LinearLayout implements Container<Field>, OnMapReadyCallback {
    private Field data;
    private MapView mapView;
    private LatLng location;
    private String uuid = UUID.randomUUID().toString();
    private MainThreadBus bus;
    private GoogleMap map;


    public FieldLocationContainer(Context context) {
        super(context);
    }

    public FieldLocationContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FieldLocationContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        bus.register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bus.unregister(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        ButterKnife.bind(this, this);
        bus = BusProvider.getInstance();
    }

    @Override
    public void setContainerData(Field data) {
        this.data = data;

        location = new LatLng(data.placeholder.get(0).latitude, data.placeholder.get(0).longitude);
        mapView = (MapView) getChildAt(0); // FIXME:
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        MapsInitializer.initialize(getContext().getApplicationContext());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f));
        map.addMarker(new MarkerOptions().position(location));
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
//                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                bus.post(new MapEventRequest(location, uuid));
            }
        });
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Subscribe
    public void onMapResponse(MapEventResponse mapEventResponse) {
        if (uuid.equals(mapEventResponse.uuid) && map != null) {
            location = mapEventResponse.target;
            data.placeholder.get(0).latitude = location.latitude;
            data.placeholder.get(0).longitude = location.longitude;
            map.clear();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f));
            map.addMarker(new MarkerOptions().position(location));
        }
    }
}
