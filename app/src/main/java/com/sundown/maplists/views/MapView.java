package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.PhotoField;
import com.sundown.maplists.models.Locations;
import com.sundown.maplists.models.MapList;
import com.sundown.maplists.storage.DatabaseCommunicator;

/**
 * Created by Sundown on 5/21/2015.
 */
public class MapView extends FrameLayout {

    public interface MapViewListener {
        void markerClicked(Marker marker);
        void mapClicked();
        void markerDragged(Marker marker);
    }

    private MapViewListener listener;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Locations model;
    private Context context;


    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        model = Locations.getInstance();
    }

    public void setListener(MapViewListener listener){
        this.listener = listener;
    }

    public void setMap(GoogleMap map){
        mMap = map;
        setUpMap();
    }

    public void cleanup(){
        try {
            mMap.clear();
        } catch (Exception e){
            Log.e(e);
        }
    }


    public void animateToLocation(LatLng latLng){
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void moveToLocation(LatLng latLng){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public float getZoomLevel(){
        return mMap.getCameraPosition().zoom;
    }

    public void setZoomLevel(float zoom){
        mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    public float zoom(boolean in){
        if (in){
            mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom + 0.5f));
        } else {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 0.5f));
        }
        return mMap.getCameraPosition().zoom;
    }

    public LatLng getCameraPosition(){
        return mMap.getCameraPosition().target;
    }

    public Marker addMarker(LatLng latLng){
        return mMap.addMarker(new MarkerOptions().position(latLng));
    }

    private void setUpMap(){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setInfoWindowAdapter(new AdapterInfoWindow(context));


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                animateToLocation(marker.getPosition());
                listener.markerClicked(marker);
                return true;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                listener.mapClicked();
            }
        });

        //this is so very wierd.. if you don't set this (even leaving all methods empty),
        //it appears the latlng doesnt change when viewed in infowindow..
        //if you dont and instead do map.setOnMarkerClickListener and use getposition for infowindow
        //you still get old position.. id does not change so its not removing/creating new marker..
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                listener.markerDragged(marker);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

        });
    }



    private class AdapterInfoWindow implements GoogleMap.InfoWindowAdapter {

        ViewGroup infoWindow;
        TextView infoTitle;
        TextView infoSnippet;
        ImageView infoImage;

        BitmapDrawable icon;
        Context context;
        DatabaseCommunicator db;


        public AdapterInfoWindow(Context context){
            this.context = context;

            db = DatabaseCommunicator.getInstance();
            icon = (BitmapDrawable) context.getApplicationInfo().loadIcon(context.getPackageManager());

            LayoutInflater inflater = LayoutInflater.from(context);
            infoWindow = (ViewGroup) inflater.inflate(R.layout.map_info_layout, null);
            infoTitle = (TextView) infoWindow.findViewById(R.id.info_title);
            infoSnippet = (TextView) infoWindow.findViewById(R.id.info_snippet);
            infoImage = (ImageView) infoWindow.findViewById(R.id.info_image);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            // Getting the position from the marker
            LatLng latLng = marker.getPosition();

            MapList list = model.getMapList(latLng);
            EntryField titleEntry = (EntryField) list.getField(0);
            EntryField snippetEntry = (EntryField) list.getField(1);
            PhotoField photoField = (PhotoField) list.getField(2);

            infoTitle.setText(titleEntry.entry);
            infoSnippet.setText(snippetEntry.entry);


            Bitmap thumb = null;


            if (photoField.thumbName != null && photoField.thumbName.length() > 0) {
                thumb = db.loadBitmap(list.documentId, photoField.thumbName);
            }


            if (thumb != null){
                infoImage.setImageBitmap(thumb);
            } else {
                infoImage.setImageBitmap(icon.getBitmap());
            }


            // Returning the view containing InfoWindow contents
            return infoWindow;
        }



    }


}
