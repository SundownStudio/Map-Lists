package com.sundown.maplists.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.sundown.maplists.Constants;
import com.sundown.maplists.R;
import com.sundown.maplists.dialogs.ActionDialogFragment;
import com.sundown.maplists.dialogs.EnterAddressDialogFragment;
import com.sundown.maplists.fragments.MapFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.fields.EntryField;
import com.sundown.maplists.models.lists.BaseList;
import com.sundown.maplists.models.lists.PrimaryList;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.utils.ToolbarManager;

public class MapActivity extends AppCompatActivity implements
        ActionDialogFragment.ConfirmActionListener, ToolbarManager.ToolbarListener {


    //NOTE: This app follows a MVC pattern:
    //Activities behave as parent-controllers for their respective Fragments and Toolbars
    //Each Fragment is a controller for its own specific views and models.
    //The Activity handles all Toolbar clicks as those usually result in starting new activities or displaying additional fragments.
    //If you add more Activities/Fragments please try to keep this pattern intact

    private static final String FRAGMENT_MAP = "MAP";
    private static final String FRAGMENT_ACTION= "ACTION";
    private static final String FRAGMENT_ENTER_ADDRESS= "ENTER_ADDRESS";


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

    /** Delete confirmation */
    private ActionDialogFragment actionDialogFragment;

    /** Enter an address to place a map marker */
    private EnterAddressDialogFragment enterAddressDialogFragment;

    /** Navigation drawer */
    //private NavigationDrawerFragment drawerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        setUpToolBars();

        if (savedInstanceState == null){ //activity first created, show map fragment as default..
            mapFragment = MapFragment.newInstance();
            mapFragment.setToolbarManager(toolbarManager);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, mapFragment, FRAGMENT_MAP);
            transaction.commit();

        } else { //activity recreated, grab existing retained fragments and reset their listeners
            mapFragment = (MapFragment) fm.findFragmentByTag(FRAGMENT_MAP);
            if (mapFragment == null)
                mapFragment = MapFragment.newInstance();
            mapFragment.setToolbarManager(toolbarManager);

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
        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout, this);
    }

    /**
     * Inflates top/bottom toolbars and set listeners
     *
     * Called at different times depending on OS version, usually sometime during onCreate
     */
    @Override
    public boolean onCreateOptionsMenu(Menu topMenu) {
        Menu bottomMenu = toolbarManager.getBottomMenu();

        topMenu.clear();
        bottomMenu.clear();

        getMenuInflater().inflate(R.menu.menu_top, topMenu);
        getMenuInflater().inflate(R.menu.menu_bottom_map, bottomMenu);

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
    @Override
    public void topToolbarPressed(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                break;

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
    }

    /**
     * Handle bottom toolbar presses, switching fragments as needed.
     * Note that the intended behavior for each menu button
     * can depend on what fragment is currently visible
     *
     * @param item selected MenuItem
     */
    @Override
    public void bottomToolbarPressed(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_secondary_lists: {
                PrimaryList list = mapFragment.getSelectedPrimaryList();
                Intent intent = new Intent(MapActivity.this, ListModeActivity.class);
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
                EntryField entry = (EntryField) mapFragment.getSelectedPrimaryList().getField(0);
                String location = entry.getEntry(0);
                if (location.length() == 0)
                    location = getString(R.string.this_location);
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
                    PrimaryList list = mapFragment.getSelectedPrimaryList();
                    Intent intent = new Intent(MapActivity.this, AddListActivity.class);
                    intent.putExtra(JsonConstants.LIST_TYPE, BaseList.PRIMARY);
                    intent.putExtra(JsonConstants.OPERATION, Constants.OP_UPDATE);
                    intent.putExtra(JsonConstants.DOCUMENT_ID, list.getDocumentId());
                    intent.putExtra(JsonConstants.MAP_ID, list.getMapId());
                    startActivity(intent);
                }
                break;
            }
        }
    }


    /**
     * User has confirmed they wish to delete a list, so delete it
     *
     * @param confirmed delete list or not
     */
    @Override
    public void confirmAction (boolean confirmed){
        if (confirmed) {
            PrimaryList list = mapFragment.getSelectedPrimaryList();
            db.delete(list.getDocumentId(), list.getMapId(), Constants.OP_DELETE_LOCATION);
        }
    }
}
