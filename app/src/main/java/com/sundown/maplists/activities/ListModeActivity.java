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
import com.sundown.maplists.fragments.ListModeFragment;
import com.sundown.maplists.models.lists.SecondaryList;
import com.sundown.maplists.pojo.MenuOption;
import com.sundown.maplists.storage.JsonConstants;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.ListModeView;

import static com.sundown.maplists.pojo.MenuOption.GroupView.DEFAULT_TOP;

/**
 * Created by Sundown on 8/19/2015.
 */
public class ListModeActivity extends AppCompatActivity implements ListModeView.AllListsListener, ToolbarManager.ToolbarListener {

    private static final String FRAGMENT_LISTMODE = "LISTMODE";
    private static final int REQUEST_CODE = 101;

    private FragmentManager fm;
    private ToolbarManager toolbarManager;

    /** shows the list of SecondaryLists associated with a particular location. */
    private ListModeFragment listModeFragment;
    private String documentId;
    private int mapId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        if (mapId == 0) {
            Bundle bundle = getIntent().getExtras();
            mapId = bundle.getInt(JsonConstants.MAP_ID);
            documentId = bundle.getString(JsonConstants.DOCUMENT_ID);
        }

        fm = getSupportFragmentManager();
        setUpToolBars(getLocationName(getIntent().getExtras()));

        if (savedInstanceState == null){
            listModeFragment = ListModeFragment.newInstance(mapId, this, toolbarManager);
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, listModeFragment, FRAGMENT_LISTMODE);
            transaction.commit();


        } else {
            listModeFragment = (ListModeFragment) fm.findFragmentByTag(FRAGMENT_LISTMODE);
            if (listModeFragment != null){
                listModeFragment.setToolbarManager(toolbarManager);
                listModeFragment.setListener(this);
            }
        }
    }

    private String getLocationName(Bundle bundle){
        String locationName = bundle.getString(JsonConstants.FIELD_ENTRY);
        if (locationName != null && locationName.length() > 0)
            return locationName;
        return getString(R.string.list_mode_activity);
    }

    private void setUpToolBars(String locationName){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(locationName);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout, this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu topMenu) {
        Menu bottomMenu = toolbarManager.getBottomMenu();

        topMenu.clear();
        bottomMenu.clear();

        getMenuInflater().inflate(R.menu.menu_top, topMenu);
        getMenuInflater().inflate(R.menu.menu_bottom_secondarylists, bottomMenu);

        toolbarManager.drawMenu(new MenuOption(DEFAULT_TOP, false));
        listModeFragment.startLoader();
        return true;
    }

    @Override
    public void topToolbarPressed(MenuItem item) {}

    @Override
    public void bottomToolbarPressed(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_list: {
                if (listModeFragment != null && listModeFragment.getUserVisibleHint()) {
                    Intent intent = new Intent(ListModeActivity.this, AddListActivity.class);
                    intent.putExtra(JsonConstants.TYPE, Constants.TYPE_SECONDARY_LIST);
                    intent.putExtra(JsonConstants.OPERATION, Constants.OP_INSERT);
                    intent.putExtra(JsonConstants.DOCUMENT_ID, documentId);
                    intent.putExtra(JsonConstants.MAP_ID, mapId);
                    startActivity(intent);
                    break;
                }
            }
        }
    }


    @Override
    public void SecondaryListSelected(SecondaryList list) {
        Intent intent = new Intent(ListModeActivity.this, SecondaryListActivity.class);
        intent.putExtra(JsonConstants.PARENT_DOC_ID, documentId);
        intent.putExtra(JsonConstants.DOCUMENT_ID, list.getDocumentId());
        intent.putExtra(JsonConstants.MAP_ID, list.getMapId());
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
