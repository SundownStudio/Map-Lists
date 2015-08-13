package com.sundown.maplists.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sundown.maplists.R;
import com.sundown.maplists.extras.Constants;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.Locations;
import com.sundown.maplists.models.MapList;
import com.sundown.maplists.network.FetchAddressIntentService;
import com.sundown.maplists.pojo.MenuOption;
import com.sundown.maplists.storage.ContentLoader;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.utils.PreferenceManager;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.MapView;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static com.sundown.maplists.pojo.MenuOption.GroupView.EDIT_DELETE;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MAP_COMPONENTS;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MAP_ZOOMING;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MARKER_COMPONENTS;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MARKER_MOVE;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MARKER_NAVIGATION;
    /* NOTE: oldLatLng & savedLatLng = why not just use one since neither can coexist?
    Answer: Better readability.. but how much extra space?
    This is empty pointer, is 4 bytes on 32-bit systems or 8 bytes on 64-bit systems. However, you're not consuming any space
    for the class that the reference points to until you actually allocate an instance of that class to point the reference at.
    In addition, null takes up space.. however there is only one null value in JVM. No matter how many variables refer to null.
    Object s = (String)null;
    Object i = (Integer)null;
    System.out.println(s == i);//true
     */
/**
 * Created by Sundown on 4/13/2015.
 */
public class MapFragment extends Fragment implements
        LocationListener,
        OnMapReadyCallback,
        MapView.MapViewListener,
        //for intent service and map stuff
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, EnterAddressDialogFragment.EnterAddressListener {



    /** Constants */
    private static final String LAT ="LAT";
    private static final String LON ="LON";
    private static final String ZOOM_LEVEL = "ZOOM";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private ToolbarManager toolbarManager;
    public void setToolbarManager(ToolbarManager toolbarManager){ this.toolbarManager = toolbarManager;}
    private DatabaseCommunicator db;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private PreferenceManager prefs;
    private MapView view;
    private Locations model;
    private ContentLoader loader;
    private AddressResultReceiver mResultReceiver;
    private static Toast toast = null;


    public Marker selectedMarker; //the selected marker with infowindow open
    public LatLng oldLatLng;    //latLng of marker prior to drag
    public LatLng savedLatLng; //latLng of selectedMarker from bundle on rotation so we can reset selectedMarker on redraw

    public static MapFragment newInstance(ToolbarManager toolbarManager){
        MapFragment fragment = new MapFragment();
        fragment.toolbarManager = toolbarManager;
        return fragment;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = Locations.getInstance();
        mResultReceiver = new AddressResultReceiver(new Handler());
        db = DatabaseCommunicator.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        prefs = PreferenceManager.getInstance();

        view = (MapView) inflater.inflate(R.layout.fragment_map, container, false);
        view.setListener(this);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
        model.clear();
        initMap();
        try {
            //mGoogleApiClient.connect(); disabled for now cuz keeps crashing
        } catch (Exception e) {
            Log.e(e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.m("MapFragment OnPause");
        setUserVisibleHint(false);
        savePrefs();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            Log.m("MapFragment GoogleApiClient disconnected");
        }

        releaseMarker();
        model.clear();
        view.cleanup();
        loader.stop();
    }



    public void savePrefs(){
        LatLng latLng = mapFragment.getMap().getCameraPosition().target;
        if (selectedMarker != null){
            latLng = selectedMarker.getPosition();
        }
        prefs.putDouble(LAT, latLng.latitude);
        prefs.putDouble(LON, latLng.longitude);
        prefs.putFloat(ZOOM_LEVEL, view.getZoomLevel());
        prefs.apply();
    }

    public void loadPrefs(){
        savedLatLng = null;
        if (prefs.containsKey(LAT) && prefs.containsKey(LON)){
            try {
                savedLatLng = new LatLng(prefs.getDouble(LAT), prefs.getDouble(LON));
                Log.m("Selected Marker Coords restored: " + savedLatLng.latitude + " by " + savedLatLng.longitude);
            } catch (Exception e){ Log.e(e); }
            prefs.remove(LAT);
            prefs.remove(LON);
            prefs.apply();
        }
        view.setZoomLevel(prefs.getFloat(ZOOM_LEVEL, Constants.SPECS.DEFAULT_ZOOM));
    }

    ////TOOLBAR INTERACTION METHODS ////
    public void gotoLocation(){
        view.animateToLocation(selectedMarker.getPosition());
    }

    public void dragLocation(){
        Log.Toast(getActivity(), "Hold down on the marker until it pops up, then drag it to a new location", Log.TOAST_LONG);
        gotoLocation();
        oldLatLng = selectedMarker.getPosition();
        Log.m("Old latLng: " + oldLatLng);
        selectedMarker.setDraggable(true);
    }


    public void createNewLocation(LatLng latLng) {
        if (latLng == null) latLng = truncateLatLng(view.getCameraPosition());


        if (model.getMapList(latLng) == null){
            savedLatLng = latLng;
            MapList list = new MapList();
            list.latLng = latLng;
            db.insert(list, JsonConstants.COUNT_MAP_LISTS, JsonConstants.MAP_ID);



        } else {
            Log.Toast(getActivity(), "A marker exists at that location latitude: " + latLng.latitude + " longitude: " + latLng.longitude, Log.TOAST_LONG);

        }
    }

    public void navigateNext(boolean forward) {
        LatLng currLatLng = null;
        if (selectedMarker != null) {
            selectedMarker.setDraggable(false);
            currLatLng = selectedMarker.getPosition();
        }
        Marker marker = model.getNextMarker(currLatLng, forward);
        selectMarker(marker);

    }

    public void zoom(boolean in){
        view.zoom(in);
    }

    ////MAP MANIPULATION METHODS ////


    @Override
    public void markerClicked(Marker marker) {
        if (selectedMarker != null && marker != selectedMarker){
            selectedMarker.setDraggable(false);
        }
        selectMarker(marker);
    }

    @Override
    public void mapClicked() {
        releaseMarker();
    }

    @Override
    public void markerDragged(Marker marker) {
        marker.setDraggable(false);
        LatLng newLatLng = truncateLatLng(marker.getPosition());

        if (model.getMapList(newLatLng) != null){
            view.animateToLocation(oldLatLng);
            Log.Toast(getActivity(), "You already have a marker at this location", Log.TOAST_LONG);
            marker.setPosition(oldLatLng);

        } else {
            view.animateToLocation(newLatLng);
            marker.setPosition(newLatLng);
            model.swap(oldLatLng, newLatLng);
            savedLatLng = newLatLng;
            MapList list = model.getMapList(newLatLng);
            db.update(list);

        }
    }


    public MapList getSelectedMapList(){
        if (selectedMarker != null)
           return model.getMapList(selectedMarker.getPosition());
        return null;
    }



    private Marker addMarker(LatLng latLng) {
        Marker marker = view.addMarker(latLng);
        model.storeMarker(latLng, marker);
        return marker;
    }


    private void selectMarker(Marker marker){
        selectedMarker = marker;
        LatLng latLng = selectedMarker.getPosition();
        startIntentService(latLng);
        view.animateToLocation(latLng);
        marker.showInfoWindow();

        toolbarManager.drawMenu(new MenuOption(EDIT_DELETE, true),
                new MenuOption(MARKER_MOVE, true),
                new MenuOption(MARKER_COMPONENTS, getSelectedMapList().multipleListsEnabled));

        Log.m("MapFragment marker selected: " + marker.getId());
    }

    private void releaseMarker() {
        Log.m("MapFragment No marker selected");
        selectedMarker = null;
        toolbarManager.drawMenu(new MenuOption(EDIT_DELETE, false),
                new MenuOption(MARKER_MOVE, false),
                new MenuOption(MARKER_COMPONENTS, false));
    }


    private LatLng truncateLatLng(LatLng latLng){
        return new LatLng(Math.floor(latLng.latitude * 1000000) / 1000000,
                Math.floor(latLng.longitude * 1000000) / 1000000);
    }



    ////MAP SETUP METHODS ////

    private void initMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        FragmentManager fm = getChildFragmentManager();
        //mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.fragment_map_container);

        Log.m("MapFragment creating new map and calling getMapAsync");
        mapFragment = SupportMapFragment.newInstance();
        fm.beginTransaction().replace(R.id.fragment_map_container, mapFragment).commit();
        mapFragment.getMapAsync(this);

        Log.m("MapFragment GoogleApiClient connect");
    }

    @Override //called when getMapAsync is done (iirc)
    public void onMapReady(GoogleMap googleMap) {
        Log.m("MapFragment map is synced and ready to be used!");
        view.setMap(googleMap);
        loadPrefs();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)

                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds - if another app is using location services, we will get loc after this interval for minimal additional power


        mGoogleApiClient.connect();

        loader = new Loader().start();

    }




    ////AUTOCALL OVERRIDE METHODS ////
    private void handleNewLocation(Location location) {
        //Log.m("MapFragment handle new location : " + location.toString());
        /*  I dont care about this right now.. if ultimately we dont use these services we should remove them.. */
        //double currentLatitude = location.getLatitude();
        //double currentLongitude = location.getLongitude();
        //LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //MarkerOptions options = new MarkerOptions()
        //        .position(latLng)
        //        .title("I am here!")
        //        .draggable(true)
        //        .snippet("snippet")
         //       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        //try { //surrounding this because i'm not sure if this will work on all phones during rotation.. connect() will call this and if mMap hasnt been
            //attached yet this might fail, it doesnt on my phone but it may be an issue of speed, better safe than sorry

            //mMap.addMarker(options);
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
        //} catch (Exception e) {
        //    Log.e(e);
        //}

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.m("MapFragment Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Log.m("Last loc null.");
            // for this example were only gonna request loc updates when last loc is not known
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } catch (Exception e){
                Log.e(e);
            }

        } else {
            handleNewLocation(location);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.m("MapFragment Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.m("MapFragment onConnectionFailed");
        /* todo this may be the old way.. check out bottom of https://developers.google.com/android/guides/setup for different ways to handle this..
        * Google Play services can resolve some errors it detects.
        * If the error has a resolution, try sending an Intent to
        * start a Google Play services activity that can resolve
        * error.
        */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                Log.e(e);
            }
        } else {
            /*
            * If no resolution is available, display a dialog to the
            * user with the error.
            */
            Log.m("Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.m("MapFragment Location changed");
        handleNewLocation(location);
    }


    private void startIntentService(LatLng latLng) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
            intent.putExtra(Constants.GEOCODE.RECEIVER, mResultReceiver);
            intent.putExtra(Constants.GEOCODE.MAP_LATITUDE, latLng.latitude);
            intent.putExtra(Constants.GEOCODE.MAP_LONGITUDE, latLng.longitude);
            intent.putExtra(Constants.GEOCODE.GEO_OPERATION, Constants.GEOCODE.FROM_LATLNG);
            getActivity().startService(intent);
        }
    }

    @Override
    public void onAddressAdded(String address) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
            intent.putExtra(Constants.GEOCODE.RECEIVER, mResultReceiver);
            intent.putExtra(Constants.GEOCODE.MAP_ADDRESS, address);
            intent.putExtra(Constants.GEOCODE.GEO_OPERATION, Constants.GEOCODE.FROM_ADDRESS);
            getActivity().startService(intent);
        }
    }

    private static void showToast(Context context,String msg){
        if(toast!=null) //must be local object in order to cancel.. why? doesn't make sense, look into this..
            toast.cancel();
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            try {
                if (resultCode == Constants.GEOCODE.SUCCESS_RESULT) {
                    int operation = Integer.parseInt(resultData.getString(Constants.GEOCODE.GEO_OPERATION));

                    if (operation == Constants.GEOCODE.FROM_ADDRESS) {
                        double lat = resultData.getDouble(Constants.GEOCODE.MAP_LATITUDE);
                        double lon = resultData.getDouble(Constants.GEOCODE.MAP_LONGITUDE);
                        createNewLocation(new LatLng(lat, lon));

                    } else if (operation == Constants.GEOCODE.FROM_LATLNG) {
                        String address = resultData.getString(Constants.GEOCODE.RESULT_DATA_KEY);
                        Log.m("MapFragment", address);
                        showToast(getActivity().getApplicationContext(), address);
                    }

                } else if (resultCode == Constants.GEOCODE.FAILURE_RESULT) {
                    String errorMessage = resultData.getString(Constants.GEOCODE.MAP_ERROR);
                    if (errorMessage == null) errorMessage = "Sorry, an unknown error has occurred";
                    showToast(getActivity().getApplicationContext(), errorMessage);
                }
            } catch (Exception e){
                Log.e(e);
            }
        }
    }


    //LIVE QUERY - Couchbases answer to Loaders
    private class Loader extends ContentLoader {

        @Override
        public Loader start() {
            // Set up my live query during view initialization:
            liveQuery = db.getLiveQuery(db.QUERY_MAP, 0);
            if (liveQuery != null) {
                liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                    @Override
                    public void changed(LiveQuery.ChangeEvent event) {
                        if (event.getSource().equals(liveQuery)) {
                            updateModel(event.getRows());
                        }
                    }
                });
                liveQuery.start();
            }
            return this;
        }

        @Override
        public void updateModel(QueryEnumerator result) {
            model.clear();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                Map<String, Object> properties = db.read(row.getSourceDocumentId()); //todo: can also use row.getDocument.. try this afterwards

                MapList mapList = new MapList().setProperties(properties);
                model.storeMapList(mapList.latLng, mapList);

            }

            drawModel();
        }

        @Override
        public void drawModel() {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.cleanup();
                    releaseMarker();
                    Marker marker;
                    TreeMap<LatLng, MapList> locations = model.getLocations();
                    Iterator it = locations.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next(); //todo: could iterate over keyset but leaving this cuz we will merge model lists at some point..  pair.getValue()
                        LatLng latLng = (LatLng) pair.getKey();

                        marker = addMarker(latLng);
                        if (savedLatLng != null) {

                            if (savedLatLng.equals(marker.getPosition())) {
                                view.moveToLocation(latLng);
                                selectMarker(marker);
                                savedLatLng = null;
                                Log.m("Selected Marker Restored from Saved Coords");

                            } else {
                                view.moveToLocation(savedLatLng);
                            }
                        }
                    }

                    toolbarManager.drawMenu(new MenuOption(MAP_ZOOMING, true),
                            new MenuOption(MAP_COMPONENTS, true),
                            new MenuOption(MARKER_NAVIGATION, (model.numLocations() > 1) ? true : false));
                }
            });

        }
    }

}
