package com.sundown.maplists.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.sundown.maplists.R;
import com.sundown.maplists.fragments.AddListFragment;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.List;
import com.sundown.maplists.models.LocationList;
import com.sundown.maplists.models.MapList;
import com.sundown.maplists.pojo.ActivityResult;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.storage.Operation;
import com.sundown.maplists.utils.ToolbarManager;

import java.util.Map;

import static com.sundown.maplists.storage.JsonConstants.LIST_ID;

/**
 * Created by Sundown on 8/18/2015.
 */
public class AddListActivity extends AppCompatActivity {

    private static final String FRAGMENT_ADD_LIST = "ADD_LIST";

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;

    private AddListFragment addListFragment;

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
            addListFragment = AddListFragment.newInstance(model);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, addListFragment, FRAGMENT_ADD_LIST);
            transaction.commit();

        } else {
            addListFragment = (AddListFragment) fm.findFragmentByTag(FRAGMENT_ADD_LIST);
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
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(R.string.add_lists_activity);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom);

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
}
