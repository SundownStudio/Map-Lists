package com.sundown.maplists.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.sundown.maplists.R;
import com.sundown.maplists.dialogs.ActionDialogFragment;
import com.sundown.maplists.dialogs.EnterAddressDialogFragment;
import com.sundown.maplists.fragments.MapFragment;
import com.sundown.maplists.fragments.NavigationDrawerFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.ListType;
import com.sundown.maplists.models.MapList;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.storage.Operation;
import com.sundown.maplists.utils.ToolbarManager;

public class MainActivity extends AppCompatActivity implements
        ActionDialogFragment.ConfirmActionListener, MapFragment.MapFragmentListener {


    //NOTE: This app follows a MVC pattern:
    //The Activities behave as parent-controllers for their respective Fragments... each Fragment is a controller for its own specific views and models.
    //The Activity also handles all Toolbar clicks (since those usually result in starting new activities or displaying fragments).
    //If you add more Activities/Fragments please try to keep this pattern intact

    private static final String FRAGMENT_MAP = "MAP";
    private static final String FRAGMENT_ACTION= "ACTION";
    private static final String FRAGMENT_ENTER_ADDRESS= "ENTER_ADDRESS";


    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;

    private FloatingActionButton zoomIn;
    private FloatingActionButton zoomOut;
    private FloatingActionButton navigateNext;
    private FloatingActionButton navigatePrior;

    private final int INTERVAL = 100;
    private Handler handler = new Handler();
    private Runnable zoomInRunnable =  new Runnable() {
        @Override
        public void run() {
            mapFragment.zoom(true);
            schedulePeriodicMethod(zoomInRunnable);
        }
    };
    private Runnable zoomOutRunnable =  new Runnable() {
        @Override
        public void run() {
            mapFragment.zoom(false);
            schedulePeriodicMethod(zoomOutRunnable);
        }
    };


    //FRAGMENTS
    /**
     * shows the map and handles map marker operations.
     * Each map marker has a list of fields associated with it (a MapList), some of these fields will display
     * on the map when the marker is selected. Each map marker denotes a location which can also
     * have a list of fields associated with it (LocationList), these however do not display on the map. */
    private MapFragment mapFragment;

    /** Delete confirmation */
    private ActionDialogFragment actionDialogFragment;

    /** Enter an address to place a map marker */
    private EnterAddressDialogFragment enterAddressDialogFragment;

    /** Navigation drawer */
    private NavigationDrawerFragment drawerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpButtons();

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        setUpToolBars();

        if (savedInstanceState == null){ //activity first created, show map fragment as default..
            mapFragment = MapFragment.newInstance();
            mapFragment.setToolbarManager(toolbarManager);
            mapFragment.setMapFragmentListener(this);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, mapFragment, FRAGMENT_MAP);
            transaction.commit();

        } else { //activity recreated, grab existing retained fragments and reset their listeners
            mapFragment = (MapFragment) fm.findFragmentByTag(FRAGMENT_MAP);
            if (mapFragment == null)
                mapFragment = MapFragment.newInstance();
            mapFragment.setToolbarManager(toolbarManager);
            mapFragment.setMapFragmentListener(this);

            enterAddressDialogFragment = (EnterAddressDialogFragment) fm.findFragmentByTag(FRAGMENT_ENTER_ADDRESS);
            if (enterAddressDialogFragment != null){
                enterAddressDialogFragment.setListener(mapFragment);
            }

            actionDialogFragment = (ActionDialogFragment) fm.findFragmentByTag(FRAGMENT_ACTION);
        }
        Log.m("toolbar", "main onCreate");
    }


    @Override
    protected void onPause() {
        super.onPause();
        clearFloatingButtons();
    }

    private void setUpButtons(){
        zoomIn = (FloatingActionButton) findViewById(R.id.fab_zoomIn);
        zoomOut = (FloatingActionButton) findViewById(R.id.fab_zoomOut);
        navigateNext = (FloatingActionButton) findViewById(R.id.fab_navigateNext);
        navigatePrior = (FloatingActionButton) findViewById(R.id.fab_navigatePrior);

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
                mapFragment.navigateNext(true);
            }
        });

        navigatePrior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.navigateNext(false);
            }
        });
    }

    private void clearFloatingButtons(){
        zoomIn.setVisibility(View.GONE);
        zoomOut.setVisibility(View.GONE);
        navigatePrior.setVisibility(View.GONE);
        navigateNext.setVisibility(View.GONE);
    }

    /**
     * Grab top and bottom toolbar views and add to toolbarManager
     * also setup Navigation Drawer (third toolbar)
     */
    private void setUpToolBars(){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayShowHomeEnabled(true); //we want the logo so we can click on it and trigger the navigation drawer
        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout);

        drawerFragment = (NavigationDrawerFragment) fm.findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbarManager.toolbarTop);
        Log.m("toolbar", "toolbar setup in main");
    }

    /**
     * Inflates top/bottom toolbars and set listeners
     *
     * Called at different times depending on OS version, usually sometime during onCreate
     */
    @Override
    public boolean onCreateOptionsMenu(Menu topMenu) {
        Menu bottomMenu = toolbarManager.toolbarBottom.getMenu();

        topMenu.clear();
        bottomMenu.clear();

        getMenuInflater().inflate(R.menu.menu_top, topMenu);
        getMenuInflater().inflate(R.menu.menu_bottom_map, bottomMenu);


        toolbarManager.toolbarTop.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return topToolbarPressed(item);
            }
        });

        toolbarManager.toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return bottomToolbarPressed(item);
            }
        });

        Log.m("toolbar", "toolbar inflated");
        mapFragment.initMap(); //hacky.. find a better way.. must call this after toolbar instantiated, unfortunately onCreateOptionsMenu called at
        //all different times on different os versions and because of nested google map fragment we cannot put in map fragment
        //there really should be a lifecycle method for this.. google has open ticket to fix for years
        return true;
    }

    /**
     * Handle top toolbar presses, switching fragments as needed.
     * Note that the intended behavior for each menu button
     * can depend on what fragment is currently visible
     *
     * @param item selected MenuItem
     */
    private boolean topToolbarPressed(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;

            case R.id.action_enter_address:{
                enterAddressDialogFragment = new EnterAddressDialogFragment();
                enterAddressDialogFragment.setListener(mapFragment);
                enterAddressDialogFragment.show(fm, FRAGMENT_ENTER_ADDRESS);
                break;
            }

            case R.id.action_add: {
                if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                    mapFragment.createNewLocation(null);
                }
                break;
            }

        }
        return true;
    }

    /**
     * Handle bottom toolbar presses, switching fragments as needed.
     * Note that the intended behavior for each menu button
     * can depend on what fragment is currently visible
     *
     * @param item selected MenuItem
     */
    private boolean bottomToolbarPressed(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_secondary_lists: {
                MapList list = mapFragment.getSelectedMapList();
                Intent intent = new Intent(MainActivity.this, ListModeActivity.class);
                intent.putExtra(JsonConstants.DOCUMENT_ID, list.getDocumentId());
                intent.putExtra(JsonConstants.MAP_ID, list.getMapId());
                try {
                    EntryField entryField = (EntryField) list.getFields().get(0); //this will always work because it's a protected field
                    intent.putExtra(JsonConstants.FIELD_ENTRY, entryField.getEntry(0)); //location entry (for titling SecondaryListActivity)
                } catch (Exception e){}
                startActivity(intent);
                break;
            }
            case R.id.action_goto_location: {
                mapFragment.gotoLocation();
                break;
            }
            case R.id.action_drag_location: {
                mapFragment.dragLocation();
                break;
            }
            case R.id.action_delete: {
                EntryField entry = (EntryField) mapFragment.getSelectedMapList().getField(0);
                String location = entry.getEntry(0);
                if (location.length() == 0)
                    location = "This location";
                String confirmText = location + " " + getResources().getString(R.string.delete_confirm);

                if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                    mapFragment.gotoLocation();
                    actionDialogFragment = ActionDialogFragment.newInstance(getString(R.string.delete_location), confirmText);
                }
                if (actionDialogFragment != null)
                    actionDialogFragment.show(fm, FRAGMENT_ACTION);
                break;
            }
            case R.id.action_edit: {
                if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                    MapList list = mapFragment.getSelectedMapList();
                    Intent intent = new Intent(MainActivity.this, AddListActivity.class);
                    intent.putExtra(JsonConstants.LIST_TYPE, ListType.MAP.name());
                    intent.putExtra(JsonConstants.OPERATION, Operation.UPDATE.name());
                    intent.putExtra(JsonConstants.DOCUMENT_ID, list.getDocumentId());
                    intent.putExtra(JsonConstants.MAP_ID, list.getMapId());
                    startActivity(intent);
                }
                break;
            }
        }
        return true;
    }

    public void schedulePeriodicMethod(Runnable runnable) {
        handler.postDelayed(runnable, INTERVAL);
    }

    public void stopPeriodicMethod(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }


    /**
     * User has confirmed they wish to delete a list, so delete it
     *
     * @param confirmed delete list or not
     */
    @Override
    public void confirmAction (boolean confirmed){
        if (confirmed) {
            MapList list = mapFragment.getSelectedMapList();
            db.delete(list.getDocumentId(), list.getMapId(), Operation.DELETE_LOCATION);
        }
    }


    @Override
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
}
