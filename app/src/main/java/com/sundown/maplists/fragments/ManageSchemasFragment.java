package com.sundown.maplists.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sundown.maplists.R;
import com.sundown.maplists.models.lists.SchemaList;
import com.sundown.maplists.pojo.MenuOption;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.ManageSchemasView;

import java.util.ArrayList;

import static com.sundown.maplists.pojo.MenuOption.GroupView.DEFAULT_ADDLIST;
import static com.sundown.maplists.pojo.MenuOption.GroupView.SCHEMA_ACTIONS;

/**
 * Created by Sundown on 8/26/2015.
 */
public class ManageSchemasFragment extends Fragment {

    private ManageSchemasView view;
    private ArrayList<SchemaList> model;
    private ToolbarManager toolbarManager;
    public void setToolbarManager(ToolbarManager toolbarManager){ this.toolbarManager = toolbarManager;}

    public static ManageSchemasFragment getInstance(ArrayList<SchemaList> model, ToolbarManager toolbarManager){
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

        toolbarManager.drawMenu(
                new MenuOption(DEFAULT_ADDLIST, false),
                new MenuOption(SCHEMA_ACTIONS, false));
    }


    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);
    }

}
