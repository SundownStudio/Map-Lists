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
import com.sundown.maplists.fragments.AddItemDialogFragment;
import com.sundown.maplists.fragments.DeleteDialogFragment;
import com.sundown.maplists.fragments.EnterAddressDialogFragment;
import com.sundown.maplists.fragments.LocationItemFragment;
import com.sundown.maplists.fragments.LocationItemsFragment;
import com.sundown.maplists.fragments.MapFragment;
import com.sundown.maplists.fragments.NavigationDrawerFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Item;
import com.sundown.maplists.models.LocationItem;
import com.sundown.maplists.models.MapItem;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.extras.ToolbarManager;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.views.LocationItemsView;

import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_ADD_ITEM;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_DELETE;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_ENTER_ADDRESS;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_LOCATION_ITEM;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_LOCATION_ITEMS;
import static com.sundown.maplists.extras.Constants.FRAGMENT_TAGS.FRAGMENT_MAP;
import static com.sundown.maplists.storage.JsonConstants.ITEM_ID;

public class MainActivity extends ActionBarActivity implements
        DeleteDialogFragment.ConfirmDeleter, AddItemDialogFragment.AddItemListener, LocationItemsView.LocationItemsListener {



    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;
    private MapFragment mapFragment;
    private LocationItemsFragment locationItemsFragment;
    private AddItemDialogFragment addItemFragment;
    private LocationItemFragment locationItemFragment;
    private DeleteDialogFragment deleteDialogFragment;
    private EnterAddressDialogFragment enterAddressDialogFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolBars();

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment) fm.findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbarManager.toolbarTop);

        if (savedInstanceState == null){ //activity first created
            mapFragment = MapFragment.newInstance(toolbarManager);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, mapFragment, FRAGMENT_MAP);
            transaction.commit();

        } else { //activity recreated
            mapFragment = (MapFragment) fm.findFragmentByTag(FRAGMENT_MAP);
            if (mapFragment == null){ //todo why? is this the default view? make sure it goes to this then if nothing else is loaded...
                mapFragment = MapFragment.newInstance(toolbarManager);
            } else {
                mapFragment.setToolbarManager(toolbarManager);
            }
            locationItemsFragment = (LocationItemsFragment) fm.findFragmentByTag(FRAGMENT_LOCATION_ITEMS);
            if (locationItemsFragment != null){
                locationItemsFragment.setToolbarManager(toolbarManager);
                locationItemsFragment.setListener(this);
            }
            addItemFragment = (AddItemDialogFragment) fm.findFragmentByTag(FRAGMENT_ADD_ITEM);
            locationItemFragment = (LocationItemFragment) fm.findFragmentByTag(FRAGMENT_LOCATION_ITEM);
            if (locationItemFragment != null){
                locationItemFragment.setToolbarManager(toolbarManager);
            }
            deleteDialogFragment = (DeleteDialogFragment) fm.findFragmentByTag(FRAGMENT_DELETE);
            enterAddressDialogFragment = (EnterAddressDialogFragment) fm.findFragmentByTag(FRAGMENT_ENTER_ADDRESS);
            if (enterAddressDialogFragment != null){
                enterAddressDialogFragment.setListener(mapFragment);
            }
            //addItemFragment = (AddItemDialogFragment) fm.getFragment(savedInstanceState, FRAGMENT_ADD_ITEM);
            //locationItemFragment = (LocationItemFragment) fm.getFragment(savedInstanceState, FRAGMENT_LOCATION_ITEM);

        }
    }



    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (addItemFragment != null) //todo needed anymore? why? shouldn't be..
            getSupportFragmentManager().putFragment(outState, FRAGMENT_ADD_ITEM, addItemFragment);
    } */


    public void setUpToolBars(){

        Toolbar toolbarTop = (Toolbar) findViewById(R.id.app_bar);
        toolbarTop.setTitle("");
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayShowHomeEnabled(true); //we want the logo so we can click on it and trigger the navigation drawer

        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);


        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_location_list: {
                        MapItem mapItem = mapFragment.getSelectedMapItem();
                        startLocationItemsFragment(mapItem.mapId);
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

                        if (locationItemFragment != null && locationItemFragment.getUserVisibleHint()) {
                            deleteDialogFragment = DeleteDialogFragment.newInstance(locationItemFragment.model, confirmText);

                        } else if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                            mapFragment.gotoLocation();
                            deleteDialogFragment = DeleteDialogFragment.newInstance(mapFragment.getSelectedMapItem(), confirmText);

                        }
                        if (deleteDialogFragment != null)
                            deleteDialogFragment.show(fm, FRAGMENT_DELETE);
                        break;
                    }
                    case R.id.action_edit: {
                        if (locationItemFragment != null && locationItemFragment.getUserVisibleHint()) {
                            LocationItem model = locationItemFragment.model;
                            EntryField entryField = (EntryField) model.getField(0);
                            startAddItemFragment(model, "Edit " + entryField.entry, Operation.UPDATE, locationItemFragment, FRAGMENT_LOCATION_ITEM);


                        } else if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                            MapItem mapItem = mapFragment.getSelectedMapItem();
                            EntryField entryField = (EntryField) mapItem.getField(0);
                            startAddItemFragment(mapItem, "Edit " + entryField.entry, Operation.UPDATE, mapFragment, FRAGMENT_MAP);
                        }
                        break;
                    }
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbarBottom.inflateMenu(R.menu.menu_bottom);

        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom);
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;

            case R.id.action_enter_address:{
                enterAddressDialogFragment = new EnterAddressDialogFragment();
                enterAddressDialogFragment.setListener(mapFragment);
                enterAddressDialogFragment.show(fm, FRAGMENT_ENTER_ADDRESS);
                break;
            }

            case R.id.action_add:
                if (locationItemsFragment != null && locationItemsFragment.getUserVisibleHint()) {
                    startAddItemFragment(new LocationItem(locationItemsFragment.mapId), "Add New Item", Operation.INSERT, locationItemsFragment, FRAGMENT_LOCATION_ITEMS);

                } else if (mapFragment != null && mapFragment.getUserVisibleHint()) {
                    mapFragment.createNewLocation(null);
                }
                break;

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
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void confirmDelete (Item item){
        if (item instanceof MapItem) {
            db.delete(item.documentId, item.mapId, Operation.DELETE_LOCATION);

        } else if (item instanceof LocationItem){
            db.delete(item.documentId, item.mapId, Operation.DELETE_LOCATION_ITEM);
            fm.popBackStack();
        }

    }


    @Override
    public void itemAdded(Item item, Operation operation) { //todo: crash if cancel.. item will be null... gotta know where to go back to...

        if (item == null) {
            fm.popBackStack();
        } else if (item instanceof MapItem) {

            boolean fragmentPopped = fm.popBackStackImmediate(FRAGMENT_MAP, fm.POP_BACK_STACK_INCLUSIVE);
            mapFragment = (MapFragment) fm.findFragmentByTag(FRAGMENT_MAP);
            if (!fragmentPopped && fm.findFragmentByTag(FRAGMENT_MAP) == null){
                mapFragment = new MapFragment();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, mapFragment, FRAGMENT_MAP);
                transaction.commit();
            }

            MapItem mapItem = (MapItem) item;
            mapItem.list = true;

        } else if (item instanceof LocationItem) {

            boolean fragmentPopped = fm.popBackStackImmediate(FRAGMENT_LOCATION_ITEMS, fm.POP_BACK_STACK_INCLUSIVE);
            locationItemsFragment = (LocationItemsFragment) fm.findFragmentByTag(FRAGMENT_LOCATION_ITEMS);
            if (!fragmentPopped || fm.findFragmentByTag(FRAGMENT_LOCATION_ITEMS) == null) {
                locationItemsFragment = LocationItemsFragment.newInstance(item.mapId, this, toolbarManager);
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, locationItemsFragment, FRAGMENT_LOCATION_ITEMS);
                transaction.commit();
            }

        }

        if (operation == Operation.INSERT){
            db.insert(item, String.valueOf(item.mapId), ITEM_ID);
        } else if (operation == Operation.UPDATE){
            db.update(item);
        }

        addItemFragment = null;
    }


    public void startAddItemFragment(Item model, String title, Operation operation, Fragment fragmentToRemove, String currentFragmentTag){
        addItemFragment = AddItemDialogFragment.newInstance(model, title, operation);

        FragmentTransaction transaction = fm.beginTransaction();
        if (fragmentToRemove != null){
            transaction.remove(fragmentToRemove);
        }
        transaction.addToBackStack(currentFragmentTag);
        transaction.commit();
        addItemFragment.show(fm, FRAGMENT_ADD_ITEM);

    }

    public void startLocationItemsFragment(int mapId){
        locationItemsFragment = LocationItemsFragment.newInstance(mapId, this, toolbarManager);
        switchFragments(locationItemsFragment, FRAGMENT_LOCATION_ITEMS, FRAGMENT_MAP);
    }

    @Override
    public void LocationItemSelected(LocationItem item) {
        locationItemFragment = LocationItemFragment.newInstance(item, toolbarManager);
        switchFragments(locationItemFragment, FRAGMENT_LOCATION_ITEM, FRAGMENT_LOCATION_ITEMS);
    }


    public void switchFragments(Fragment fragment, String fragmentTag, String backStackTag){
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
            if (addItemFragment != null){
                addItemFragment.handleActivityResults(new ActivityResult(requestCode, resultCode, data));
            }

        } catch (Exception e){Log.e(e);}

    }



}
