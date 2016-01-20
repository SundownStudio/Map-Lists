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
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.sundown.maplists.Constants;
import com.sundown.maplists.R;
import com.sundown.maplists.dialogs.ActionDialogFragment;
import com.sundown.maplists.dialogs.AddFieldDialogFragment;
import com.sundown.maplists.dialogs.AddSchemaDialogFragment;
import com.sundown.maplists.dialogs.SelectNumberDialogFragment;
import com.sundown.maplists.fragments.AddListFragment;
import com.sundown.maplists.fragments.ManageSchemasFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.fields.EntryField;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.lists.BaseList;
import com.sundown.maplists.models.lists.MapList;
import com.sundown.maplists.models.lists.MapListFactory;
import com.sundown.maplists.models.lists.PrimaryList;
import com.sundown.maplists.models.lists.SchemaList;
import com.sundown.maplists.models.lists.SecondaryList;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.pojo.MenuOption;
import com.sundown.maplists.storage.ContentLoader;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.AddFieldView;
import com.sundown.maplists.views.ToolbarWithSpinner;

import java.util.ArrayList;
import java.util.Map;

import static com.sundown.maplists.pojo.MenuOption.GroupView.DEFAULT_TOP;
import static com.sundown.maplists.pojo.MenuOption.GroupView.EDIT_DELETE;
import static com.sundown.maplists.pojo.MenuOption.GroupView.SCHEMA_ACTIONS;
import static com.sundown.maplists.storage.JsonConstants.DOCUMENT_ID;
import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

/**
 * Created by Sundown on 8/18/2015.
 */
public class AddListActivity extends AppCompatActivity implements AddFieldView.FieldSelector, AddSchemaDialogFragment.AddSchemaListener,
        ActionDialogFragment.ConfirmActionListener, SelectNumberDialogFragment.SelectNumberListener, ToolbarWithSpinner.Listener, ToolbarManager.ToolbarListener {

    private static final String FRAGMENT_ADD_LIST = "ADD_LIST";
    private static final String FRAGMENT_ADD_FIELD = "ADD_FIELD";
    private static final String FRAGMENT_SELECT_NUMBER = "SELECT_NUMBER";
    private static final String FRAGMENT_ADD_SCHEMA = "ADD_SCHEMA";
    private static final String FRAGMENT_ACTION = "ACTION";
    private static final String FRAGMENT_MANAGE_SCHEMAS = "MANAGE_SCHEMAS";
    private static final int SCHEMA_UNIQUE = -1;
    private static final int SCHEMA_IGNORE = -2;

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;

    private AddListFragment addListFragment;

    /**
     * Fragment for select a new field to add onto this list
     */
    private AddFieldDialogFragment addFieldDialogFragment;

    /**
     * Select number of entries for this field.. option reserved for primary fields only
     */
    private SelectNumberDialogFragment selectNumberDialogFragment;

    /**
     * Fragment for adding a new schema
     */
    private AddSchemaDialogFragment addSchemaFragment;

    /**
     * Delete confirmation
     */
    private ActionDialogFragment actionDialogFragment;

    /**
     * Manage Schemas
     */
    private ManageSchemasFragment manageSchemasFragment;

    private int operation;
    private MapList model;
    private int listType;
    private Field newFieldToAdd;
    private boolean saveListUpdate;

    /**
     * our list of saved schemas
     */
    private ArrayList<SchemaList> savedSchemaLists;
    private ContentLoader schemaLoader;
    private SchemaList origSchemaList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //disable keyboard on some devices

        Bundle bundle = getIntent().getExtras();
        listType = bundle.getInt(JsonConstants.LIST_TYPE);
        operation = bundle.getInt(JsonConstants.OPERATION);
        String documentId = bundle.getString(JsonConstants.DOCUMENT_ID);
        int mapId = bundle.getInt(JsonConstants.MAP_ID);

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        savedSchemaLists = new ArrayList<>();
        setUpToolBars(getString(R.string.add_lists_activity));

        if (operation == Constants.OP_INSERT) { //only for secondarylists
            model = MapListFactory.createList(getResources(), listType, mapId);

        } else if (operation == Constants.OP_UPDATE) { //can be both maplists and secondarylists
            Map<String, Object> properties = db.read(documentId);
            model = MapListFactory.createList(getResources(), listType, mapId).setProperties(properties);
        }

        if (savedInstanceState == null) {
            addListFragment = AddListFragment.newInstance(model);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, addListFragment, FRAGMENT_ADD_LIST);
            transaction.commit();

        } else {
            addListFragment = (AddListFragment) fm.findFragmentByTag(FRAGMENT_ADD_LIST);
            addFieldDialogFragment = (AddFieldDialogFragment) fm.findFragmentByTag(FRAGMENT_ADD_FIELD);
            if (addFieldDialogFragment != null) {
                addFieldDialogFragment.setListener(this);
            }
            selectNumberDialogFragment = (SelectNumberDialogFragment) fm.findFragmentByTag(FRAGMENT_SELECT_NUMBER);
            addSchemaFragment = (AddSchemaDialogFragment) fm.findFragmentByTag(FRAGMENT_ADD_SCHEMA);
            actionDialogFragment = (ActionDialogFragment) fm.findFragmentByTag(FRAGMENT_ACTION);
            manageSchemasFragment = (ManageSchemasFragment) fm.findFragmentByTag(FRAGMENT_MANAGE_SCHEMAS);
            if (manageSchemasFragment != null) {
                manageSchemasFragment.setToolbarManager(toolbarManager);
            }
        }

        origSchemaList =  model.copySchema();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (schemaLoader != null) schemaLoader.stop();
        model = addListFragment.refreshModel();

        if (saveListUpdate) {
            if (operation == Constants.OP_INSERT) {
                db.insert(model, String.valueOf(model.getMapId()), LIST_ID);
            } else if (operation == Constants.OP_UPDATE) {
                db.update(model);
            }
        }
    }

    private void setUpToolBars(String title) {
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(title);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout, this);
        toolbarManager.setTopVisibility(View.VISIBLE);
    }

    private void setUpToolbarSpinner() {
        toolbarManager.setTopVisibility(View.INVISIBLE);
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_spinner_layout);
        ToolbarWithSpinner toolbarTop = (ToolbarWithSpinner) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout, this);

        ArrayList<String> list = new ArrayList<>();
        int index = 0;
        int size = savedSchemaLists.size();

        for (int i = 0; i < size; ++i){
            SchemaList schemaList = savedSchemaLists.get(i);
            list.add(schemaList.getSchemaName());
            if (schemaList.getSchemaId() == model.getSchemaId()){
                index = i;
            }
        }

        toolbarTop.setSpinner(list, index, this);
        invalidateOptionsMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu topMenu) {
        Menu bottomMenu = toolbarManager.getBottomMenu();

        topMenu.clear();
        bottomMenu.clear();

        getMenuInflater().inflate(R.menu.menu_top, topMenu);
        getMenuInflater().inflate(R.menu.menu_bottom_addlist, bottomMenu);

        boolean showSchemaActions = false;
        if (savedSchemaLists.size() > 0)
            showSchemaActions = true;

        toolbarManager.drawMenu(
                new MenuOption(EDIT_DELETE, false),
                new MenuOption(DEFAULT_TOP, false),
                new MenuOption(SCHEMA_ACTIONS, showSchemaActions));

        if (schemaLoader == null) schemaLoader = new Loader().start();

        return true;
    }

    @Override
    public void topToolbarPressed(MenuItem item) {}

    @Override
    public void bottomToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_add_field: {
                addFieldDialogFragment = AddFieldDialogFragment.newInstance(this);
                addFieldDialogFragment.show(fm, FRAGMENT_ADD_FIELD);
                break;
            }

            case R.id.action_add_list:
                saveListUpdate = true;
                model = addListFragment.refreshModel();
                SchemaList newSchemaList = model.copySchema();
                int saveSchema = SCHEMA_IGNORE;

                if (didSchemaChange(newSchemaList)) { //schema has changed! does schema match an existing schema?
                    saveSchema = doesChangedSchemaMatchExisting(newSchemaList);
                }

                if (saveSchema == SCHEMA_IGNORE){ //schema is currently selected so end
                    finish();

                } else {
                    if (saveSchema == SCHEMA_UNIQUE) { //schema is unique so prompt user to save it
                        addSchemaFragment = AddSchemaDialogFragment.getInstance(Constants.OP_INSERT, getString(R.string.enter_name_for_schema), getString(R.string.schema) + "0" + (savedSchemaLists.size()), saveSchema);

                    } else { //same schema already exists! so let's ask user if they wish to consolidate under this existing schema.. saveSchema is now the index to update..
                        SchemaList list = savedSchemaLists.get(saveSchema);
                        addSchemaFragment = AddSchemaDialogFragment.getInstance(Constants.OP_UPDATE, getString(R.string.a_schema_already_exists), list.getSchemaName(), saveSchema);
                    }
                    addSchemaFragment.show(fm, FRAGMENT_ADD_SCHEMA);
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

    }

    private boolean didSchemaChange(SchemaList list){
        if (list.getSchemaId() != origSchemaList.getSchemaId()) return true;
        return !origSchemaList.hasSameAttributes(list);
    }

    private int doesChangedSchemaMatchExisting(SchemaList list){
        int num = savedSchemaLists.size();
        for (int i = 0; i < num; ++i){
            if (savedSchemaLists.get(i).hasSameAttributes(list)) {
                if (i == ((ToolbarWithSpinner) toolbarManager.getToolbarTop()).getSelectedIndex()) {
                    return SCHEMA_IGNORE; //were already on the selected schema so end this
                } else {
                    return i; //schema not unique, tell user this schema already exists and prompt to enter new name if creating new one..
                }
            }
        }
        return SCHEMA_UNIQUE;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (addListFragment != null) {
                addListFragment.setActivityResult(new ActivityResult(requestCode, resultCode, data));
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (manageSchemasFragment != null && manageSchemasFragment.isVisible()) {
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.fragment_container, addListFragment, FRAGMENT_ADD_LIST);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    transaction.commit();
                    schemaLoader = new Loader().start();
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
        newFieldToAdd = field;

        String fieldTypeName = getPrimaryFieldTypeString(field.getType());
        if (fieldTypeName != null) {
            selectNumberDialogFragment = SelectNumberDialogFragment.newInstance(fieldTypeName, field.getType(), this);
            selectNumberDialogFragment.show(fm, FRAGMENT_SELECT_NUMBER);

        } else {
            addListFragment.addNewField(field);
        }
    }


    private String getPrimaryFieldTypeString(int type) {
        String[] fieldNames = getResources().getStringArray(R.array.all_field_names);
        switch (type) {
            case Field.NAME:
                return fieldNames[0];
            case Field.PHONE:
                return fieldNames[1];
            case Field.EMAIL:
                return fieldNames[2];
            case Field.URL:
                return fieldNames[6];
            case Field.PRICE:
                return fieldNames[7];
            case Field.ITEM_LIST: {
                String name = fieldNames[9];
                return name.substring(0, name.lastIndexOf("-")); }
            case Field.PRICE_LIST: {
                String name = fieldNames[10];
                return name.substring(0, name.lastIndexOf("-")); }
        }
        return null;
    }

    @Override
    public void confirmAction(boolean confirmed) { //leave without saving changes?
        if (confirmed) {
            saveListUpdate = false;
            finish();
        }
    }

    @Override
    public void numberSelected(int number) {
        EntryField field = (EntryField) newFieldToAdd;
        field.addAdditionalBlankEntries(number);
        addListFragment.addNewField(field);
    }

    @Override
    public void schemaAdded(int operation, String schemaName, int indexToUpdate) {
        if (schemaLoader != null) schemaLoader.stop();
        if (schemaName != null) {
            if (operation == Constants.OP_INSERT) {
                SchemaList schemaList = model.copySchema();
                schemaList.setSchemaName(schemaName);
                int schemaId = db.insert(schemaList, JsonConstants.COUNT_SCHEMAS, JsonConstants.SCHEMA_ID);
                if (schemaId != -1) {
                    model.setSchemaId(schemaId);
                    model.setSchemaName(schemaName);
                }
            } else {
                SchemaList schemaList = savedSchemaLists.get(indexToUpdate);
                model.setSchemaId(schemaList.getSchemaId());
                model.setSchemaName(schemaList.getSchemaName());
            }
            saveListUpdate = true;
        }
        finish();
    }



    @Override
    public void onSchemaSelected(int position) {
        MapList list = MapListFactory.createList(getResources(), listType, model.getMapId());

        if (listType == BaseList.PRIMARY) {
            ((PrimaryList) list).setLatLng(((PrimaryList) model).getLatLng());

        } else if (listType == BaseList.SECONDARY) {
            ((SecondaryList) list).setListId(((SecondaryList) model).getListId());
        }
        Map<String, Object> properties = savedSchemaLists.get(position).getProperties();
        properties.put(DOCUMENT_ID, model.getDocumentId()); //make sure we are using model docID, not the schema docID
        model = list.setSchemaProperties(properties, listType);

        addListFragment.setModel(model);
    }

    private class Loader extends ContentLoader {

        private int determineListTypeForLoader(int listType){
            if (listType == BaseList.PRIMARY)
                return BaseList.PRIMARY_SCHEMA;
            return BaseList.SECONDARY_SCHEMA;
        }

        @Override
        public LiveQuery getLiveQuery() {
            return db.getLiveQuery(db.QUERY_SCHEMA, determineListTypeForLoader(listType));
        }

        @Override
        public void updateModel(QueryEnumerator result) {
            savedSchemaLists.clear();
            SchemaList defaultSchema = MapListFactory.createList(getResources(), listType, model.getMapId()).copySchema();
            savedSchemaLists.add(defaultSchema);

            while(result.hasNext()){
                QueryRow row = result.next();
                Map<String, Object> properties = db.read(row.getSourceDocumentId());
                savedSchemaLists.add(defaultSchema.copy().setProperties(properties));
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
