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

import com.sundown.maplists.R;
import com.sundown.maplists.fragments.ActionDialogFragment;
import com.sundown.maplists.fragments.LocationListFragment;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.SecondaryList;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.storage.Operation;
import com.sundown.maplists.utils.ToolbarManager;

import java.util.Map;

/**
 * Created by Sundown on 8/19/2015.
 */
public class LocationListActivity extends AppCompatActivity implements ActionDialogFragment.ConfirmActionListener {


    private static final String FRAGMENT_LOCATION_LIST = "LOCATION_LIST";
    private static final String FRAGMENT_ACTION= "ACTION";

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;
    private SecondaryList model;
    private int mapId;
    private String documentId;
    private String parentDocumentId;

    /** Displays the contents of a single LocationList */
    private LocationListFragment locationListFragment;

    /** Delete confirmation */
    private ActionDialogFragment actionDialogFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        Bundle bundle = getIntent().getExtras();
        mapId = bundle.getInt(JsonConstants.MAP_ID);
        documentId = bundle.getString(JsonConstants.DOCUMENT_ID);
        parentDocumentId = bundle.getString(JsonConstants.PARENT_DOC_ID);

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        setUpToolBars();


        Map<String, Object> properties = db.read(documentId);
        model = new SecondaryList(mapId).setProperties(properties);


        if (savedInstanceState == null){
            locationListFragment = LocationListFragment.newInstance(model, toolbarManager);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, locationListFragment, FRAGMENT_LOCATION_LIST);
            transaction.commit();

        } else {

            locationListFragment = (LocationListFragment) fm.findFragmentByTag(FRAGMENT_LOCATION_LIST);
            if (locationListFragment != null){
                locationListFragment.setToolbarManager(toolbarManager);
            }

            actionDialogFragment = (ActionDialogFragment) fm.findFragmentByTag(FRAGMENT_ACTION);
        }

    }

    private void setUpToolBars(){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(R.string.location_list_activity);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout);

    }

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

        return true;
    }

    private boolean topToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return true;
        }
    }

    private boolean bottomToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete: {

                if (locationListFragment != null && locationListFragment.getUserVisibleHint()) {
                    EntryField entryField = (EntryField) model.getField(0);
                    actionDialogFragment = ActionDialogFragment.newInstance(getString(R.string.delete_location), entryField.entry + " " + getResources().getString(R.string.delete_confirm));
                }
                if (actionDialogFragment != null)
                    actionDialogFragment.show(fm, FRAGMENT_ACTION);
                break;
            }

            case R.id.action_edit: {

                if (locationListFragment != null && locationListFragment.getUserVisibleHint()) {
                    Intent intent = new Intent(LocationListActivity.this, AddListActivity.class);
                    intent.putExtra(JsonConstants.TYPE, JsonConstants.TYPE_LOCATION_LIST);
                    intent.putExtra(JsonConstants.OPERATION, Operation.UPDATE.name());
                    intent.putExtra(JsonConstants.DOCUMENT_ID, model.documentId);
                    intent.putExtra(JsonConstants.MAP_ID, model.mapId);
                    startActivity(intent);
                }
                break;

            }
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                returnActivityResult();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        returnActivityResult();
    }

    @Override
    public void confirmAction(boolean confirmed) {
        db.delete(model.documentId, model.mapId, Operation.DELETE_LOCATION_LIST);
        returnActivityResult();
    }

    private void returnActivityResult(){
        Intent intent = new Intent();
        intent.putExtra(JsonConstants.DOCUMENT_ID, parentDocumentId);
        intent.putExtra(JsonConstants.MAP_ID, mapId);
        setResult(RESULT_OK, intent);
        finish();
    }
}
