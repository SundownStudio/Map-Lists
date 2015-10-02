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
import com.sundown.maplists.dialogs.ActionDialogFragment;
import com.sundown.maplists.fragments.SecondaryListFragment;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.ListFactory;
import com.sundown.maplists.models.ListType;
import com.sundown.maplists.models.SchemaList;
import com.sundown.maplists.pojo.MenuOption;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.storage.Operation;
import com.sundown.maplists.utils.ToolbarManager;

import java.util.Map;

/**
 * Created by Sundown on 8/19/2015.
 */
public class SecondaryListActivity extends AppCompatActivity implements ActionDialogFragment.ConfirmActionListener {


    private static final String FRAGMENT_SECONDARY_LIST = "SECONDARY_LIST";
    private static final String FRAGMENT_ACTION= "ACTION";

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;
    private SchemaList model;
    private int mapId;
    private String documentId;
    private String parentDocumentId;

    /** Displays the contents of a single SecondaryList */
    private SecondaryListFragment secondaryListFragment;

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
        setUpToolBars(getItemName());

        if (savedInstanceState == null){
            secondaryListFragment = SecondaryListFragment.newInstance();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, secondaryListFragment, FRAGMENT_SECONDARY_LIST);
            transaction.commit();

        } else {

            secondaryListFragment = (SecondaryListFragment) fm.findFragmentByTag(FRAGMENT_SECONDARY_LIST);
            actionDialogFragment = (ActionDialogFragment) fm.findFragmentByTag(FRAGMENT_ACTION);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadModel();
        secondaryListFragment.setModel(model);
    }

    private void loadModel(){
        Map<String, Object> properties = db.read(documentId);
        model =  ListFactory.createList(ListType.SECONDARY, mapId).setProperties(properties);
    }

    private void setUpToolBars(String itemName){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(itemName);
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

        toolbarManager.drawMenu(new MenuOption(MenuOption.GroupView.EDIT_DELETE, true),
                new MenuOption(MenuOption.GroupView.DEFAULT_TOP, false));

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

                if (secondaryListFragment != null && secondaryListFragment.getUserVisibleHint()) {
                    EntryField entryField = (EntryField) model.getField(0);
                    actionDialogFragment = ActionDialogFragment.newInstance(getString(R.string.delete_location), entryField.getEntry(0) + " " + getResources().getString(R.string.delete_confirm));
                }
                if (actionDialogFragment != null)
                    actionDialogFragment.show(fm, FRAGMENT_ACTION);
                break;
            }

            case R.id.action_edit: {

                if (secondaryListFragment != null && secondaryListFragment.getUserVisibleHint()) {
                    Intent intent = new Intent(SecondaryListActivity.this, AddListActivity.class);
                    intent.putExtra(JsonConstants.LIST_TYPE, ListType.SECONDARY.name());
                    intent.putExtra(JsonConstants.OPERATION, Operation.UPDATE.name());
                    intent.putExtra(JsonConstants.DOCUMENT_ID, model.getDocumentId());
                    intent.putExtra(JsonConstants.MAP_ID, model.getMapId());
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
        if (confirmed) {
            db.delete(model.getDocumentId(), model.getMapId(), Operation.DELETE_SECONDARY_LIST);
            returnActivityResult();
        }
    }

    private void returnActivityResult(){
        Intent intent = new Intent();
        intent.putExtra(JsonConstants.DOCUMENT_ID, parentDocumentId);
        intent.putExtra(JsonConstants.MAP_ID, mapId);
        setResult(RESULT_OK, intent);
        finish();
    }

    private String getItemName(){
        try {
            EntryField entryField = (EntryField) model.getField(0); //subject
            String entry = entryField.getEntry(0); //subject entry
            if (entry.length() > 0)
                return entry;
        } catch (Exception e){}
        return getString(R.string.secondary_list_activity); //else return default
    }
}
