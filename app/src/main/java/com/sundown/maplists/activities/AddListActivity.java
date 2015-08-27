package com.sundown.maplists.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.sundown.maplists.R;
import com.sundown.maplists.fragments.ActionDialogFragment;
import com.sundown.maplists.fragments.AddFieldDialogFragment;
import com.sundown.maplists.fragments.AddListFragment;
import com.sundown.maplists.fragments.AddSchemaDialogFragment;
import com.sundown.maplists.fragments.ManageSchemasFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.LocationList;
import com.sundown.maplists.models.MapList;
import com.sundown.maplists.models.SchemaList;
import com.sundown.maplists.models.SecondaryList;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.pojo.MenuOption;
import com.sundown.maplists.storage.ContentLoader;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.storage.Operation;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.AddFieldView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static com.sundown.maplists.pojo.MenuOption.GroupView.DEFAULT_TOP;
import static com.sundown.maplists.pojo.MenuOption.GroupView.EDIT_DELETE;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MAP_COMPONENTS;
import static com.sundown.maplists.pojo.MenuOption.GroupView.SCHEMA_ACTIONS;
import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

/**
 * Created by Sundown on 8/18/2015.
 */
public class AddListActivity extends AppCompatActivity implements AddFieldView.FieldSelector, AddSchemaDialogFragment.AddSchemaListener,
        ActionDialogFragment.ConfirmActionListener{

    private static final String FRAGMENT_ADD_LIST = "ADD_LIST";
    private static final String FRAGMENT_ADD_FIELD = "ADD_FIELD";
    private static final String FRAGMENT_ADD_SCHEMA = "ADD_SCHEMA";
    private static final String FRAGMENT_ACTION= "ACTION";
    private static final String FRAGMENT_MANAGE_SCHEMAS = "MANAGE_SCHEMAS";

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;

    private AddListFragment addListFragment;

    /** Fragment for select a new field to add onto this list */
    private AddFieldDialogFragment addFieldDialogFragment;

    /** Fragment for adding a new schema */
    private AddSchemaDialogFragment addSchemaFragment;

    /** Delete confirmation */
    private ActionDialogFragment actionDialogFragment;

    /** Manage Schemas */
    private ManageSchemasFragment manageSchemasFragment;

    private Operation operation;
    private LocationList model;
    private boolean saveUpdate;

    /** our list of saved schemas */
    private ArrayList<SchemaList> savedSchemaLists;
    private ContentLoader schemaLoader;
    private SchemaList originalSchemaList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);

        Bundle bundle = getIntent().getExtras();
        String type = bundle.getString(JsonConstants.TYPE);
        operation = Operation.valueOf(bundle.getString(JsonConstants.OPERATION));
        String documentId = bundle.getString(JsonConstants.DOCUMENT_ID);
        int mapId = bundle.getInt(JsonConstants.MAP_ID);

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        savedSchemaLists = new ArrayList<>();
        setUpToolBars(getString(R.string.add_lists_activity));

        if (type.equals(JsonConstants.TYPE_MAP_LIST)){
            Map<String, Object> properties = db.read(documentId);
            MapList list = new MapList().setProperties(properties);
            list.multipleListsEnabled = true;
            model = list;

        } else if (type.equals(JsonConstants.TYPE_LOCATION_LIST)){
            if (operation == Operation.INSERT) {
                model = new SecondaryList(mapId);

            } else if (operation == Operation.UPDATE){
                Map<String, Object> properties = db.read(documentId);
                model = new SecondaryList(mapId).setProperties(properties);
            }
        }

        if (savedInstanceState == null){
            addListFragment = AddListFragment.newInstance(model);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, addListFragment, FRAGMENT_ADD_LIST);
            transaction.commit();

        } else {
            addListFragment = (AddListFragment) fm.findFragmentByTag(FRAGMENT_ADD_LIST);
            addFieldDialogFragment = (AddFieldDialogFragment) fm.findFragmentByTag(FRAGMENT_ADD_FIELD);
            addSchemaFragment = (AddSchemaDialogFragment) fm.findFragmentByTag(FRAGMENT_ADD_SCHEMA);
            actionDialogFragment = (ActionDialogFragment) fm.findFragmentByTag(FRAGMENT_ACTION);
            manageSchemasFragment = (ManageSchemasFragment) fm.findFragmentByTag(FRAGMENT_MANAGE_SCHEMAS);
            if (manageSchemasFragment != null){
                manageSchemasFragment.setToolbarManager(toolbarManager);
            }
        }

        originalSchemaList = new SchemaList(model);
    }

    @Override
    protected void onPause() {
        super.onPause();
        schemaLoader.stop();

        if (saveUpdate) {
            if (operation == Operation.INSERT) {
                db.insert(model, String.valueOf(model.mapId), LIST_ID);
            } else if (operation == Operation.UPDATE) {
                db.update(model);
            }
        }
    }

    private void setUpToolBars(String title){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        toolbarTopLayout.setVisibility(View.VISIBLE);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(title);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout);

    }

    private void setUpToolbarSpinner(){
        toolbarManager.toolbarTopLayout.setVisibility(View.INVISIBLE);
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_spinner_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_schema);
        ArrayList<String> list = new ArrayList<>();
        for (SchemaList schemaList : savedSchemaLists){
            list.add(schemaList.getSchemaName());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        invalidateOptionsMenu();


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

        boolean showSchemaActions = false;
        if (savedSchemaLists.size() > 0)
            showSchemaActions = true;

        toolbarManager.drawMenu(
                new MenuOption(MAP_COMPONENTS, false),
                new MenuOption(EDIT_DELETE, false),
                new MenuOption(DEFAULT_TOP, false),
                new MenuOption(SCHEMA_ACTIONS, showSchemaActions));

        if (schemaLoader == null) {
            schemaLoader = new Loader().start();
        }

        return true;
    }

    private boolean topToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {

        }
        return true;
    }


    private boolean bottomToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_add_field: {
                addFieldDialogFragment = AddFieldDialogFragment.newInstance(this);
                addFieldDialogFragment.show(fm, FRAGMENT_ADD_FIELD);
                break;
            }

            case R.id.action_add_list:
                saveUpdate = true;
                model = addListFragment.refreshModel();
                SchemaList newSchemaList = new SchemaList(model);
                if (!originalSchemaList.equals(newSchemaList)){ //schema has changed! prompt user to save new schema
                    addSchemaFragment = AddSchemaDialogFragment.getInstance(getString(R.string.schema) + "0" + (savedSchemaLists.size()+1));
                    addSchemaFragment.show(fm, FRAGMENT_ADD_SCHEMA);
                } else {
                    finish();
                }
                break;

            case R.id.action_cancel_add_list:
                actionDialogFragment = ActionDialogFragment.newInstance(getString(R.string.abandon_changes), getString(R.string.abandon_changes_text));
                if (actionDialogFragment != null)
                    actionDialogFragment.show(fm, FRAGMENT_ACTION);
                break;

            case R.id.action_manage_schemas:
                setUpToolBars(getString(R.string.manage_schemas_activity));
                manageSchemasFragment = ManageSchemasFragment.getInstance(savedSchemaLists, toolbarManager);
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, manageSchemasFragment, FRAGMENT_MANAGE_SCHEMAS);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
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
                if (manageSchemasFragment.isVisible()){
                    setUpToolbarSpinner();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.fragment_container, addListFragment, FRAGMENT_ADD_LIST);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                } else {
                    finish();
                }
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
    public void schemaAdded(String schemaName) {
        schemaLoader.stop();
        if (schemaName != null) {
            SchemaList schemaList = new SchemaList(model);
            schemaList.setSchemaName(schemaName);
            db.insert(schemaList, JsonConstants.COUNT_SCHEMAS, JsonConstants.SCHEMA_ID);
        }
        finish();
    }

    @Override
    public void confirmAction(boolean confirmed) {
        if (confirmed){
            saveUpdate = false;
            finish();
        }
    }

    private class Loader extends ContentLoader {

        @Override
        public Loader start() {
            liveQuery = db.getLiveQuery(db.QUERY_SCHEMA);
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
            savedSchemaLists.clear();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                Map<String, Object> properties = db.read(row.getSourceDocumentId());
                savedSchemaLists.add(new SchemaList().setProperties(properties));
            }

            if (savedSchemaLists.size() > 0)
                drawModel();
        }

        @Override
        public void drawModel() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setUpToolbarSpinner();
                }
            });
        }
    }
}
