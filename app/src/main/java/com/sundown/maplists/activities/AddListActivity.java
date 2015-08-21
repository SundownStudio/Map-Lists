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
import com.sundown.maplists.fragments.AddFieldDialogFragment;
import com.sundown.maplists.fragments.AddListFragment;
import com.sundown.maplists.fragments.AddSchemaDialogFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.List;
import com.sundown.maplists.models.LocationList;
import com.sundown.maplists.models.MapList;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.storage.Operation;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.AddFieldView;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

/**
 * Created by Sundown on 8/18/2015.
 */
public class AddListActivity extends AppCompatActivity implements AddFieldView.FieldSelector, AddSchemaDialogFragment.AddSchemaListener {

    private static final String FRAGMENT_ADD_LIST = "ADD_LIST";
    public static final String FRAGMENT_ADD_FIELD = "ADD_FIELD";
    public static final String FRAGMENT_ADD_SCHEMA = "ADD_SCHEMA";

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;

    private AddListFragment addListFragment;

    /** Fragment for select a new field to add onto this list */
    private AddFieldDialogFragment addFieldDialogFragment;

    /** Fragment for adding a new schema */
    private AddSchemaDialogFragment addSchemaFragment;

    private Operation operation;
    private List model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        Bundle bundle = getIntent().getExtras();
        String type = bundle.getString(JsonConstants.TYPE);
        operation = Operation.valueOf(bundle.getString(JsonConstants.OPERATION));
        String documentId = bundle.getString(JsonConstants.DOCUMENT_ID);
        int mapId = bundle.getInt(JsonConstants.MAP_ID);

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        setUpToolBars();

        if (type.equals(JsonConstants.TYPE_MAP_LIST)){
            Map<String, Object> properties = db.read(documentId);
            MapList list = new MapList().setProperties(properties);
            list.multipleListsEnabled = true;
            model = list;

        } else if (type.equals(JsonConstants.TYPE_LOCATION_LIST)){
            if (operation == Operation.INSERT) {
                model = new LocationList(mapId);

            } else if (operation == Operation.UPDATE){
                Map<String, Object> properties = db.read(documentId);
                model = new LocationList(mapId).setProperties(properties);
            }
        }

        if (savedInstanceState == null){
            addListFragment = AddListFragment.newInstance(model, toolbarManager);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, addListFragment, FRAGMENT_ADD_LIST);
            transaction.commit();

        } else {
            addListFragment = (AddListFragment) fm.findFragmentByTag(FRAGMENT_ADD_LIST);
            if (addListFragment != null){
                addListFragment.setToolbarManager(toolbarManager);
            }

            addFieldDialogFragment = (AddFieldDialogFragment) fm.findFragmentByTag(FRAGMENT_ADD_FIELD);
            addSchemaFragment = (AddSchemaDialogFragment) fm.findFragmentByTag(FRAGMENT_ADD_SCHEMA);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        addListFragment.refreshModel();

        if (operation == Operation.INSERT){
            db.insert(model, String.valueOf(model.mapId), LIST_ID);
        } else if (operation == Operation.UPDATE){
            db.update(model);
        }
    }

    private void setUpToolBars(){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(R.string.add_lists_activity);
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
        getMenuInflater().inflate(R.menu.menu_bottom_addlist, bottomMenu);


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
            case R.id.action_add: {
                addFieldDialogFragment = AddFieldDialogFragment.newInstance(this);
                addFieldDialogFragment.show(fm, FRAGMENT_ADD_FIELD);
                break;
            }
        }
        return true;
    }


    private boolean bottomToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_add_list:
                addSchemaFragment = AddSchemaDialogFragment.getInstance(getString(R.string.schema) + "1");
                addSchemaFragment.show(fm, FRAGMENT_ADD_SCHEMA);
                break;

        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (addListFragment != null){
                addListFragment.setActivityResult(new ActivityResult(requestCode, resultCode, data));
            }
        } catch (Exception e){
            Log.e(e);}

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void addNewField(Field field) {
        addFieldDialogFragment.dismiss();
        addListFragment.addNewField(field);
    }

    @Override
    public void schemaAdded(String schema) {
        finish();
    }
}
