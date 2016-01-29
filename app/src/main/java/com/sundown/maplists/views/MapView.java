package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.Locations;
import com.sundown.maplists.models.fields.EntryField;
import com.sundown.maplists.models.fields.PhotoField;
import com.sundown.maplists.models.lists.PrimaryList;
import com.sundown.maplists.models.lists.Schema;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.utils.ColorUtils;

/**
 * Created by Sundown on 5/21/2015.
 */
public class MapView extends RelativeLayout {

    public interface MapViewListener {
        void markerClicked(Marker marker);
        void mapClicked();
        void markerDragged(Marker marker);
        void navigate(boolean forward);
    }

    private MapViewListener listener;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Locations model;
    private Context context;
    private FloatingActionButton zoomIn, zoomOut, navigateNext, navigatePrior;

    private final int INTERVAL = 100;
    private Handler handler = new Handler();
    private Runnable zoomInRunnable =  new Runnable() {
        @Override
        public void run() {
            zoom(true);
            schedulePeriodicMethod(zoomInRunnable);
        }
    };
    private Runnable zoomOutRunnable =  new Runnable() {
        @Override
        public void run() {
            zoom(false);
            schedulePeriodicMethod(zoomOutRunnable);
        }
    };


    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        model = Locations.getInstance();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        zoomIn = (FloatingActionButton) findViewById(R.id.fab_zoomIn);
        zoomOut = (FloatingActionButton) findViewById(R.id.fab_zoomOut);
        navigateNext = (FloatingActionButton) findViewById(R.id.fab_navigateNext);
        navigatePrior = (FloatingActionButton) findViewById(R.id.fab_navigatePrior);

        //pre-lollipop uses a different FAB graphic where shadow is part of margin so need to reset margins
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) navigateNext.getLayoutParams();
            params.setMargins(0, 0, 0, -36);
            navigateNext.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) navigatePrior.getLayoutParams();
            params.setMargins(0, 0, 110, -36);
            navigatePrior.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) zoomIn.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            zoomIn.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) zoomOut.getLayoutParams();
            params.setMargins(0, 0, 110, 0);
            zoomOut.setLayoutParams(params);
        }

        zoomIn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        schedulePeriodicMethod(zoomInRunnable);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        stopPeriodicMethod(zoomInRunnable);
                        break;
                }
                return true;
            }
        });


        zoomOut.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        schedulePeriodicMethod(zoomOutRunnable);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        stopPeriodicMethod(zoomOutRunnable);
                        break;
                }
                return true;
            }
        });

        navigateNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.navigate(true);
            }
        });

        navigatePrior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.navigate(false);
            }
        });
    }

    public void setListener(MapViewListener listener){
        this.listener = listener;
    }

    public void setMap(GoogleMap map){
        mMap = map;
        setUpMap();
    }

    public void cleanup(){
        clearFloatingButtons();
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


    public Marker addMarker(LatLng latLng, int color){
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.marker);
        Bitmap markerBitmap = ColorUtils.changeImageColor(ColorUtils.convertDrawableToBitmap(drawable), color);
        return mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));
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

    private void schedulePeriodicMethod(Runnable runnable) {
        handler.postDelayed(runnable, INTERVAL);
    }

    private void stopPeriodicMethod(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }

    private void clearFloatingButtons(){
        zoomIn.setVisibility(View.GONE);
        zoomOut.setVisibility(View.GONE);
        navigatePrior.setVisibility(View.GONE);
        navigateNext.setVisibility(View.GONE);
    }

    public void displayFloatingButtons(boolean displayNavigationButtons) {
        if (displayNavigationButtons){
            navigateNext.setVisibility(View.VISIBLE);
            navigatePrior.setVisibility(View.VISIBLE);
        } else {
            navigateNext.setVisibility(View.GONE);
            navigatePrior.setVisibility(View.GONE);
        }
        zoomOut.setVisibility(View.VISIBLE);
        zoomIn.setVisibility(View.VISIBLE);
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

            LatLng latLng = marker.getPosition();

            PrimaryList list = model.getPrimaryList(latLng);
            Schema schema = list.getSchema();
            EntryField titleEntry = (EntryField) schema.getField(0);
            EntryField snippetEntry = (EntryField) schema.getField(1);
            PhotoField photoField = (PhotoField) schema.getField(2);

            infoTitle.setText(titleEntry.getEntry(0));
            infoSnippet.setText(snippetEntry.getEntry(0));

            Bitmap thumb = db.loadBitmap(list.getDocumentId(), photoField.getThumbName());

            if (thumb != null){
                infoImage.setImageBitmap(thumb);
            } else {
                infoImage.setImageBitmap(icon.getBitmap());
            }

            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

    }


}
