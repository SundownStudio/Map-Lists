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
import com.sundown.maplists.fragments.SecondaryListsFragment;
import com.sundown.maplists.models.SecondaryList;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.storage.Operation;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.SecondaryListsView;

/**
 * Created by Sundown on 8/19/2015.
 */
public class SecondaryListsActivity extends AppCompatActivity implements SecondaryListsView.AllListsListener {

    private static final String FRAGMENT_SECONDARY_LISTS = "SECONDARY_LISTS";
    private static final int REQUEST_CODE = 101;

    private FragmentManager fm;
    private ToolbarManager toolbarManager;

    /** shows the list of LocationLists associated with a particular location. */
    private SecondaryListsFragment secondaryListsFragment;
    private String documentId;
    private int mapId;
    private String locationName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        if (mapId == 0) {
            Bundle bundle = getIntent().getExtras();
            mapId = bundle.getInt(JsonConstants.MAP_ID);
            documentId = bundle.getString(JsonConstants.DOCUMENT_ID);
            locationName = bundle.getString(JsonConstants.FIELD_ENTRY);
            if (locationName == null || locationName.length() == 0)
                locationName = getString(R.string.secondary_lists_activity);
        }

        fm = getSupportFragmentManager();
        setUpToolBars();

        if (savedInstanceState == null){
            secondaryListsFragment = SecondaryListsFragment.newInstance(mapId, this, toolbarManager);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, secondaryListsFragment, FRAGMENT_SECONDARY_LISTS);
            transaction.commit();


        } else {
            secondaryListsFragment = (SecondaryListsFragment) fm.findFragmentByTag(FRAGMENT_SECONDARY_LISTS);
            if (secondaryListsFragment != null){
                secondaryListsFragment.setToolbarManager(toolbarManager);
                secondaryListsFragment.setListener(this);
            }
        }

    }

    private void setUpToolBars(){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(locationName);
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

            case R.id.action_add: {
                if (secondaryListsFragment != null && secondaryListsFragment.getUserVisibleHint()) {
                    Intent intent = new Intent(SecondaryListsActivity.this, AddListActivity.class);
                    intent.putExtra(JsonConstants.TYPE, JsonConstants.TYPE_LOCATION_LIST);
                    intent.putExtra(JsonConstants.OPERATION, Operation.INSERT.name());
                    intent.putExtra(JsonConstants.DOCUMENT_ID, documentId);
                    intent.putExtra(JsonConstants.MAP_ID, mapId);
                    startActivity(intent);
                    break;
                }
            }
        }
        return true;
    }


    private boolean bottomToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return true;
        }
    }


    @Override
    public void LocationListSelected(SecondaryList list) {
        Intent intent = new Intent(SecondaryListsActivity.this, LocationListActivity.class);
        intent.putExtra(JsonConstants.PARENT_DOC_ID, documentId);
        intent.putExtra(JsonConstants.DOCUMENT_ID, list.documentId);
        intent.putExtra(JsonConstants.MAP_ID, list.mapId);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mapId = data.getIntExtra(JsonConstants.MAP_ID, 0);
                documentId = data.getStringExtra(JsonConstants.DOCUMENT_ID);
            }
        }
    }
}
