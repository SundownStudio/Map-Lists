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
import com.sundown.maplists.fragments.SecondaryListFragment;
import com.sundown.maplists.models.lists.BaseList;
import com.sundown.maplists.models.lists.ListFactory;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.utils.ToolbarManager;

import java.util.Map;

/**
 * Created by Sundown on 8/19/2015.
 */
public class SecondaryListActivity extends AppCompatActivity implements ActionDialogFragment.ConfirmActionListener, ToolbarManager.ToolbarListener {


    private static final String FRAGMENT_SECONDARY_LIST = "SECONDARY_LIST";
    private static final String FRAGMENT_ACTION= "ACTION";

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;
    private BaseList model;
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
        init(savedInstanceState != null ? true : false);

        Bundle bundle = getIntent().getExtras();
        mapId = bundle.getInt(JsonConstants.MAP_ID);
        documentId = bundle.getString(JsonConstants.DOCUMENT_ID);
        parentDocumentId = bundle.getString(JsonConstants.PARENT_DOC_ID);
    }

    private void init(boolean recreate){

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();

        if (recreate){ //hookup frags
            secondaryListFragment = (SecondaryListFragment) fm.findFragmentByTag(FRAGMENT_SECONDARY_LIST);
            actionDialogFragment = (ActionDialogFragment) fm.findFragmentByTag(FRAGMENT_ACTION);

        } else { //instantiate new frags
            secondaryListFragment = SecondaryListFragment.newInstance();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, secondaryListFragment, FRAGMENT_SECONDARY_LIST);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadModel();
        secondaryListFragment.setModel(model);
        setUpToolBars(getItemName());
    }

    private void loadModel(){
        Map<String, Object> properties = db.read(documentId);
        model =  ListFactory.createList(getResources(), Constants.TYPE_SECONDARY_LIST, mapId).setProperties(properties);
    }

    private void setUpToolBars(String itemName){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(itemName);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarManager = ToolbarManager.getInstance(toolbarTop, toolbarBottom, toolbarTopLayout, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu topMenu) {
        Menu bottomMenu = toolbarManager.getBottomMenu();

        topMenu.clear();
        bottomMenu.clear();

        getMenuInflater().inflate(R.menu.menu_top_empty, topMenu);
        getMenuInflater().inflate(R.menu.menu_bottom_edit, bottomMenu);

        return true;
    }

    @Override
    public void topToolbarPressed(MenuItem item) {}

    @Override
    public void bottomToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete: {

                if (secondaryListFragment != null && secondaryListFragment.getUserVisibleHint()) {
                    actionDialogFragment = ActionDialogFragment.newInstance(getString(R.string.delete_location), model.getListTitle() + " " + getResources().getString(R.string.delete_confirm));
                }
                if (actionDialogFragment != null)
                    actionDialogFragment.show(fm, FRAGMENT_ACTION);
                break;
            }

            case R.id.action_edit: {

                if (secondaryListFragment != null && secondaryListFragment.getUserVisibleHint()) {
                    Intent intent = new Intent(SecondaryListActivity.this, AddListActivity.class);
                    intent.putExtra(JsonConstants.TYPE, Constants.TYPE_SECONDARY_LIST);
                    intent.putExtra(JsonConstants.OPERATION, Constants.OP_UPDATE);
                    intent.putExtra(JsonConstants.DOCUMENT_ID, model.getDocumentId());
                    intent.putExtra(JsonConstants.MAP_ID, model.getMapId());
                    startActivity(intent);
                }
                break;

            }
        }
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
            db.delete(model.getDocumentId(), model.getMapId(), Constants.OP_DELETE_SECONDARY_LIST);
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
        String title = model.getListTitle();
        if (title != null && title.length() > 0) return title;
        return getString(R.string.secondary_list_activity); //else return default
    }
}
