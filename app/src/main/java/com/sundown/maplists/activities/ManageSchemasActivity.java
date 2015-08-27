package com.sundown.maplists.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import com.sundown.maplists.R;
import com.sundown.maplists.fragments.ManageSchemasFragment;
import com.sundown.maplists.storage.DatabaseCommunicator;
import com.sundown.maplists.utils.ToolbarManager;

/**
 * Created by Sundown on 8/26/2015.
 */
public class ManageSchemasActivity extends AppCompatActivity {

    private static final String FRAGMENT_MANAGE_SCHEMAS = "MANAGE_SCHEMAS";

    private FragmentManager fm;
    private DatabaseCommunicator db;
    private ToolbarManager toolbarManager;

    private ManageSchemasFragment manageSchemasFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        fm = getSupportFragmentManager();
        db = DatabaseCommunicator.getInstance();
        setUpToolBars();

        /* todo decide what to do with this activity.. do we want this accessible from nav-bar? if so move loader to this activity.. once model loads load frag
        if (savedInstanceState == null){
            manageSchemasFragment = ManageSchemasFragment.getInstance();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, manageSchemasFragment, FRAGMENT_MANAGE_SCHEMAS);
            transaction.commit();

        } else {
            manageSchemasFragment = (ManageSchemasFragment) fm.findFragmentByTag(FRAGMENT_MANAGE_SCHEMAS);
        }*/
    }

    private void setUpToolBars(){
        LinearLayout toolbarTopLayout = (LinearLayout) findViewById(R.id.toolbar_top_layout);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        Toolbar toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);

        toolbarTop.setTitle(R.string.manage_schemas_activity);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarManager = new ToolbarManager(toolbarTop, toolbarBottom, toolbarTopLayout);

    }
}
