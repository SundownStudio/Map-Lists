package com.sundown.maplists.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.sundown.maplists.R;
import com.sundown.maplists.extras.Operation;
import com.sundown.maplists.extras.ToolbarManager;
import com.sundown.maplists.fragments.AddListDialogFragment;
import com.sundown.maplists.fragments.DeleteDialogFragment;
import com.sundown.maplists.fragments.EnterAddressDialogFragment;
import com.sundown.maplists.fragments.LocationListFragment;
import com.sundown.maplists.fragments.LocationListsFragment;
import com.sundown.maplists.fragments.MapFragment;
import com.sundown.maplists.fragments.NavigationDrawerFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.List;
import com.sundown.maplists.models.LocationList;
import com.sundown.maplists.models.MapList;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.views.LocationListsView;

import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.*;
import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

public class MainActivity extends ActionBarActivity implements
        DeleteDialogFragment.ConfirmDeleter, AddListDialogFragment.AddListListener, LocationListsView.LocationListsListener {


    //NOTE: This app follows a MVC pattern:
    //The Activity behaves as a parent-controller for its respective Fragments... each Fragment is a controller for its own specific views and models.
    //The Activity also handles all Toolbar clicks (since those usually result in displaying fragments).
    //If you add more Activities/Fragments please try to keep this pattern intact

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;

    //FRAGMENTS

    /**
     * shows the map and handles map marker operations.
     * Each map marker has a list of fields associated with it (a MapList), some of these fields will display
     * on the map when the marker is selected. Each map marker denotes a location which can also
     * have a list of fields associated with it (LocationList), these however do not display on the map. */
    private MapFragment mapFragment;

    /** shows the list of LocationLists associated with a particular location. */
    private LocationListsFragment locationListsFragment;

    /** An appendable form for adding fields to both MapLists and LocationLists */
    private AddListDialogFragment addListFragment;

    /** Displays the contents of a single LocationList */
    private LocationListFragment locationListFragment;

    /** Delete confirmation */
    private DeleteDialogFragment deleteDialogFragment;

    /** Enter an address to place a map marker */
    private EnterAddressDialogFragment enterAddressDialogFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        setUpToolBars();

        if (savedInstanceState == null){ //activity first created, show map fragment as default..
            mapFragment = MapFragment.newInstance(toolbarManager);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, mapFragment, FRAGMENT_MAP);
            transaction.commit();

        } else { //activity recreated, grab existing retained fragments and reset their listeners
            mapFragment = (MapFragment) fm.findFragmentByTag(FRAGMENT_MAP);
            if (mapFragment == null){
                mapFragment = MapFragment.newInstance(toolbarManager);
            } else {
                mapFragment.setToolbarManager(toolbarManager);
            }

            locationListsFragment = (LocationListsFragment) fm.findFragmentByTag(FRAGMENT_LOCATION_LISTS);
            if (locationListsFragment != null){
                locationListsFragment.setToolbarManager(toolbarManager);
                locationListsFragment.setListener(this);
            }

            locationListFragment = (LocationListFragment) fm.findFragmentByTag(FRAGMENT_LOCATION_LIST);
            if (locationListFragment != null){
                locationListFragment.setToolbarManager(toolbarManager);
            }

            enterAddressDialogFragment = (EnterAddressDialogFragment) fm.findFragmentByTag(FRAGMENT_ENTER_ADDRESS);
            if (enterAddressDialogFragment != null){
                enterAddressDialogFragment.setListener(mapFragment);
            }

            addListFragment = (AddListDialogFragment) fm.findFragmentByTag(FRAGMENT_ADD_LIST);
            if (addListFragment != null){
                addListFragment.setListener(this);
            }

            deleteDialogFragment = (DeleteDialogFragment) fm.findFragmentByTag(FRAGMENT_DELETE);
        }
    }


    /**
     * Grab top and bottom toolbar views and add to toolbarManager
     * also setup Navigation Drawer (third toolbar)
     */
    private void setUpToolBars(){
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle("");
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayShowHomeEnabled(true); //we want the logo so we can click on it and trigger the navigation drawer
        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom);

        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment) fm.findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbarManager.toolbarTop);
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
        getMenuInflater().inflate(R.menu.menu_bottom, bottomMenu);


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

                //If we are viewing a specific location, add a new list to that location
                if (locationListsFragment != null && locationListsFragment.getUserVisibleHint()) {
                    startAddListFragment(new LocationList(locationListsFragment.mapId), getString(R.string.add_new_item), Operation.INSERT, locationListsFragment, FRAGMENT_LOCATION_LISTS);

                    //If we are viewing the map, add a new location
                } else if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                    mapFragment.createNewLocation(null);
                }
                break;
            }

            case R.id.action_navigate_prior:
                mapFragment.navigateNext(false);
                break;

            case R.id.action_navigate_next:
                mapFragment.navigateNext(true);
                break;

            case R.id.action_zoom_in:
                mapFragment.zoom(true);
                break;

            case R.id.action_zoom_out:
                mapFragment.zoom(false);
                break;

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
            case R.id.action_location_list: {
                MapList list = mapFragment.getSelectedMapList();
                startLocationListsFragment(list.mapId);
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
                String confirmText = getResources().getString(R.string.delete_confirm);

                if (locationListFragment != null && locationListFragment.getUserVisibleHint()) {
                    deleteDialogFragment = DeleteDialogFragment.newInstance(locationListFragment.model, confirmText);

                } else if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                    mapFragment.gotoLocation();
                    deleteDialogFragment = DeleteDialogFragment.newInstance(mapFragment.getSelectedMapList(), confirmText);

                }
                if (deleteDialogFragment != null)
                    deleteDialogFragment.show(fm, FRAGMENT_DELETE);
                break;
            }
            case R.id.action_edit: {
                if (locationListFragment != null && locationListFragment.getUserVisibleHint()) {
                    LocationList model = locationListFragment.model;
                    EntryField entryField = (EntryField) model.getField(0);
                    startAddListFragment(model, getString(R.string.edit) + entryField.entry, Operation.UPDATE, locationListFragment, FRAGMENT_LOCATION_LIST);


                } else if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                    MapList list = mapFragment.getSelectedMapList();
                    EntryField entryField = (EntryField) list.getField(0);
                    startAddListFragment(list, getString(R.string.edit) + entryField.entry, Operation.UPDATE, mapFragment, FRAGMENT_MAP);
                }
                break;
            }
        }
        return true;
    }


    /**
     * User has confirmed they wish to delete a list, so delete it
     *
     * @param list list to be deleted
     */
    @Override
    public void confirmDelete (List list){
        if (list instanceof MapList) {
            db.delete(list.documentId, list.mapId, Operation.DELETE_LOCATION);

        } else if (list instanceof LocationList){
            db.delete(list.documentId, list.mapId, Operation.DELETE_LOCATION_LIST);
            fm.popBackStack();
        }
    }


    /**
     * User has added a list, so display the correct fragment (depending on list type)
     * and perform the requested Operation
     *
     * @param list list to be added
     * @param operation DB Operation to be performed
     */
    @Override
    public void listAdded(List list, Operation operation) {

        if (list == null) { //user cancelled, return to previous fragment
            fm.popBackStack();

        } else if (list instanceof MapList) {//user added map list

            boolean fragmentPopped = fm.popBackStackImmediate(FRAGMENT_MAP, fm.POP_BACK_STACK_INCLUSIVE);
            mapFragment = (MapFragment) fm.findFragmentByTag(FRAGMENT_MAP);
            if (!fragmentPopped && fm.findFragmentByTag(FRAGMENT_MAP) == null){
                mapFragment = new MapFragment();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, mapFragment, FRAGMENT_MAP);
                transaction.commit();
            }

            MapList mapList = (MapList) list;
            mapList.multipleListsEnabled = true;

        } else if (list instanceof LocationList) {//user added location list

            boolean fragmentPopped = fm.popBackStackImmediate(FRAGMENT_LOCATION_LISTS, fm.POP_BACK_STACK_INCLUSIVE);
            locationListsFragment = (LocationListsFragment) fm.findFragmentByTag(FRAGMENT_LOCATION_LISTS);
            if (!fragmentPopped || fm.findFragmentByTag(FRAGMENT_LOCATION_LISTS) == null) {
                locationListsFragment = LocationListsFragment.newInstance(list.mapId, this, toolbarManager);
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, locationListsFragment, FRAGMENT_LOCATION_LISTS);
                transaction.commit();
            }
        }

        if (operation == Operation.INSERT){
            db.insert(list, String.valueOf(list.mapId), LIST_ID);
        } else if (operation == Operation.UPDATE){
            db.update(list);
        }

        addListFragment = null;
    }


    /**
     * User has requested to add a list, show the AddListFragment
     *
     * @param model the list to be added to
     * @param title to use for this dialog
     * @param operation DB Operation to be performed
     * @param fragmentToRemove fragment to remove from screen
     * @param currentFragmentTag current fragments tag to be added to backstack
     */
    private void startAddListFragment(List model, String title, Operation operation, Fragment fragmentToRemove, String currentFragmentTag){
        addListFragment = AddListDialogFragment.newInstance(model, title, operation);

        FragmentTransaction transaction = fm.beginTransaction();
        if (fragmentToRemove != null){
            transaction.remove(fragmentToRemove);
        }
        transaction.addToBackStack(currentFragmentTag);
        transaction.commit();
        addListFragment.show(fm, FRAGMENT_ADD_LIST);

    }


    /**
     * User has requested to view their location lists so show it
     *
     * @param locationId the id for this location
     */
    private void startLocationListsFragment(int locationId){
        locationListsFragment = LocationListsFragment.newInstance(locationId, this, toolbarManager);
        switchFragments(locationListsFragment, FRAGMENT_LOCATION_LISTS, FRAGMENT_MAP);
    }

    /**
     * User has selected to view a list at their location
     *
     * @param list to be shown
     */
    @Override
    public void LocationListSelected(LocationList list) {
        locationListFragment = LocationListFragment.newInstance(list, toolbarManager);
        switchFragments(locationListFragment, FRAGMENT_LOCATION_LIST, FRAGMENT_LOCATION_LISTS);
    }


    /**
     * Switch from the current fragment to a new fragment and add the old
     * fragment tag to backstack
     *
     * @param fragment to be shown
     * @param fragmentTag the new fragment's tag
     * @param backStackTag the current fragment's tag to be added to backstack
     */
    private void switchFragments(Fragment fragment, String fragmentTag, String backStackTag){
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, fragmentTag);
        if (backStackTag != null)
            transaction.addToBackStack(backStackTag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (addListFragment != null){
                addListFragment.handleActivityResults(new ActivityResult(requestCode, resultCode, data));
            }

        } catch (Exception e){Log.e(e);}

    }
}
