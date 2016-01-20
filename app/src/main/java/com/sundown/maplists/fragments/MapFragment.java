package com.sundown.maplists.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sundown.maplists.R;
import com.sundown.maplists.dialogs.EnterAddressDialogFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.Locations;
import com.sundown.maplists.models.lists.BaseList;
import com.sundown.maplists.models.lists.MapListFactory;
import com.sundown.maplists.models.lists.PrimaryList;
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

import static com.sundown.maplists.network.GeocodeConstants.FAILURE_RESULT;
import static com.sundown.maplists.network.GeocodeConstants.FROM_ADDRESS;
import static com.sundown.maplists.network.GeocodeConstants.FROM_LATLNG;
import static com.sundown.maplists.network.GeocodeConstants.GEO_OPERATION;
import static com.sundown.maplists.network.GeocodeConstants.MAP_ADDRESS;
import static com.sundown.maplists.network.GeocodeConstants.MAP_ERROR;
import static com.sundown.maplists.network.GeocodeConstants.MAP_LATITUDE;
import static com.sundown.maplists.network.GeocodeConstants.MAP_LONGITUDE;
import static com.sundown.maplists.network.GeocodeConstants.RECEIVER;
import static com.sundown.maplists.network.GeocodeConstants.RESULT_DATA_KEY;
import static com.sundown.maplists.network.GeocodeConstants.SUCCESS_RESULT;
import static com.sundown.maplists.pojo.MenuOption.GroupView.DEFAULT_TOP;
import static com.sundown.maplists.pojo.MenuOption.GroupView.EDIT_DELETE;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MARKER_COMPONENTS;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MARKER_MOVE;

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
        OnMapReadyCallback,
        MapView.MapViewListener,
        EnterAddressDialogFragment.EnterAddressListener,
        //for intent service and map stuff
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public interface MapFragmentListener{
        void displayFloatingButtons(boolean displayNavigationButtons);
    }

    /** Constants */
    private static final String FRAGMENT_GMAP ="FRAGMENT_GMAP";
    private static final String LAT ="LAT";
    private static final String LON ="LON";
    private static final String ZOOM_LEVEL = "ZOOM";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final float DEFAULT_ZOOM = 2.0f;

    private ToolbarManager toolbarManager;
    public void setToolbarManager(ToolbarManager toolbarManager){ this.toolbarManager = toolbarManager;}
    private DatabaseCommunicator db;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private PreferenceManager prefs;
    private MapView view;
    private Locations model;
    private ContentLoader loader;
    private AddressResultReceiver mResultReceiver;
    private MapFragmentListener listener;
    public void setMapFragmentListener(MapFragmentListener listener) { this.listener = listener;}
    private static Toast toast = null;
    private boolean drag;


    public Marker selectedMarker; //the selected marker with infowindow open
    public LatLng oldLatLng;    //latLng of marker prior to drag
    public LatLng savedLatLng; //latLng of selectedMarker from bundle on rotation so we can reset selectedMarker on redraw

    public static MapFragment newInstance(){ return new MapFragment();}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = Locations.getInstance();
        mResultReceiver = new AddressResultReceiver(new Handler());
        db = DatabaseCommunicator.getInstance();
        Log.m("toolbar", "mapfragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        prefs = PreferenceManager.getInstance();
        view = (MapView) inflater.inflate(R.layout.fragment_map, container, false);
        view.setListener(this);
        Log.m("toolbar", "mapfragment onCreateView");
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
        model.clear();
        getActivity().invalidateOptionsMenu();
        Log.m("toolbar", "mapfragment onResume");

    }


    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);
        savePrefs();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.m("MapFragment GoogleApiClient disconnected");
        }

        releaseMarker();
        model.clear();
        view.cleanup();
        loader.stop();
        Log.m("toolbar", "mapfragment onPause");
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
        view.setZoomLevel(prefs.getFloat(ZOOM_LEVEL, DEFAULT_ZOOM));
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


        if (model.getPrimaryList(latLng) == null){
            savedLatLng = latLng;
            PrimaryList list = (PrimaryList) MapListFactory.createList(getResources(), BaseList.PRIMARY, -1);
            list.setLatLng(latLng);
            db.insert(list, JsonConstants.COUNT_PRIMARY_LISTS, JsonConstants.MAP_ID);



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

        if (model.getPrimaryList(newLatLng) != null){
            view.animateToLocation(oldLatLng);
            Log.Toast(getActivity(), "You already have a marker at this location", Log.TOAST_LONG);
            marker.setPosition(oldLatLng);

        } else {
            drag = true;
            model.swap(oldLatLng, newLatLng);
            savedLatLng = newLatLng;
            PrimaryList list = model.getPrimaryList(newLatLng);
            db.update(list);

        }
    }


    public PrimaryList getSelectedPrimaryList(){
        if (selectedMarker != null)
           return model.getPrimaryList(selectedMarker.getPosition());
        return null;
    }



    private Marker addMarker(LatLng latLng, int color) {
        Marker marker = view.addMarker(latLng, color);
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
                new MenuOption(MARKER_COMPONENTS, true));

    }

    private void releaseMarker() {
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

    public void initMap() {
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentByTag(FRAGMENT_GMAP);
        if (mapFragment == null)
            mapFragment = SupportMapFragment.newInstance();
        fm.beginTransaction().replace(R.id.fragment_map_container, mapFragment, FRAGMENT_GMAP).commit();
        mapFragment.getMapAsync(this);

    }

    @Override //called when getMapAsync is done (iirc)
    public void onMapReady(GoogleMap googleMap) {
        Log.m("MapFragment map is synced and ready to be used!");
        view.setMap(googleMap);
        loadPrefs();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mGoogleApiClient.connect();
        loader = new Loader().start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.m("GoogleApiClient connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.m("GoogleApiClient suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.m("GoogleApiClient onConnectionFailed");
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
            Log.m("GoogleApiClient connection failed with code " + connectionResult.getErrorCode());
        }
    }




    private void startIntentService(LatLng latLng) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
            intent.putExtra(RECEIVER, mResultReceiver);
            intent.putExtra(MAP_LATITUDE, latLng.latitude);
            intent.putExtra(MAP_LONGITUDE, latLng.longitude);
            intent.putExtra(GEO_OPERATION, FROM_LATLNG);
            getActivity().startService(intent);
        }
    }

    @Override
    public void onAddressAdded(String address) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
            intent.putExtra(RECEIVER, mResultReceiver);
            intent.putExtra(MAP_ADDRESS, address);
            intent.putExtra(GEO_OPERATION, FROM_ADDRESS);
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
                if (resultCode == SUCCESS_RESULT) {
                    String result = resultData.getString(GEO_OPERATION);

                    if (result == null) {
                        resultCode = FAILURE_RESULT;

                    } else {
                        int operation = Integer.parseInt(result);

                        if (operation == FROM_ADDRESS) {
                            double lat = resultData.getDouble(MAP_LATITUDE);
                            double lon = resultData.getDouble(MAP_LONGITUDE);
                            createNewLocation(new LatLng(lat, lon));

                        } else if (operation == FROM_LATLNG) {
                            String address = resultData.getString(RESULT_DATA_KEY);
                            Log.m("MapFragment", address);
                            showToast(getActivity().getApplicationContext(), address);
                        }
                    }
                }

                if (resultCode == FAILURE_RESULT) {
                    String errorMessage = resultData.getString(MAP_ERROR);
                    if (errorMessage == null) errorMessage = "Sorry, an unknown error has occurred";
                    showToast(getActivity().getApplicationContext(), errorMessage);
                }
            } catch (Exception e){
                Log.e(e);
            }
        }
    }



    private class Loader extends ContentLoader {

        @Override
        public LiveQuery getLiveQuery() {
            return db.getLiveQuery(db.QUERY_MAP);
        }

        @Override
        public void updateModel(QueryEnumerator result) {
            model.clear();
            while (result.hasNext()) {
                QueryRow row = result.next();
                Map<String, Object> properties = db.read(row.getSourceDocumentId());

                PrimaryList list = (PrimaryList) MapListFactory.createList(getResources(), BaseList.PRIMARY, -1).setProperties(properties);
                model.storePrimaryList(list.getLatLng(), list);

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
                    TreeMap<LatLng, PrimaryList> locations = model.getPrimaryLists();
                    Iterator it = locations.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next(); //todo: could iterate over keyset but leaving this cuz we will merge model lists at some point..  pair.getValue()
                        LatLng latLng = (LatLng) pair.getKey();
                        PrimaryList list = (PrimaryList) pair.getValue();

                        marker = addMarker(latLng, list.getColor());
                        if (savedLatLng != null && savedLatLng.equals(marker.getPosition())) {
                            //this is the marker to select and focus on..
                            if (!drag) //dragging should glide, rotations should not
                                view.moveToLocation(latLng);
                            selectMarker(marker);
                            drag = false;
                            savedLatLng = null;
                        }
                    }

                    if (savedLatLng != null){ //we haven't focused on any markers so this denotes an empty location
                        view.moveToLocation(savedLatLng);
                    }


                    toolbarManager.drawMenu(new MenuOption(DEFAULT_TOP, true));
                    listener.displayFloatingButtons((model.numLocations() > 1));

                }
            });

        }
    }

}
