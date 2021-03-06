package com.sundown.maplists.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sundown.maplists.R;
import com.sundown.maplists.models.lists.Schema;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.ManageSchemasView;

import java.util.ArrayList;

/**
 * Created by Sundown on 8/26/2015.
 */
public class ManageSchemasFragment extends Fragment {

    private ManageSchemasView view;
    private ArrayList<Schema> model;
    private ToolbarManager toolbarManager;
    public void setToolbarManager(ToolbarManager toolbarManager){ this.toolbarManager = toolbarManager;}

    public static ManageSchemasFragment getInstance(ArrayList<Schema> model, ToolbarManager toolbarManager){
        ManageSchemasFragment fragment = new ManageSchemasFragment();
        fragment.model = model;
        fragment.toolbarManager = toolbarManager;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ManageSchemasView) inflater.inflate(R.layout.fragment_manage_schemas, container, false);
        view.setList(model);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);

        toolbarManager.drawMenu(toolbarManager.DEFAULT_ADDLIST, false);
        toolbarManager.drawMenu(toolbarManager.SCHEMA_ACTIONS, false);
    }


    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);
    }

}
