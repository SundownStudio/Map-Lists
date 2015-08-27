package com.sundown.maplists.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sundown.maplists.R;
import com.sundown.maplists.models.SchemaList;
import com.sundown.maplists.views.ManageSchemasView;

import java.util.ArrayList;

/**
 * Created by Sundown on 8/26/2015.
 */
public class ManageSchemasFragment extends Fragment {

    private ManageSchemasView view;
    private ArrayList<SchemaList> model;

    public static ManageSchemasFragment getInstance(ArrayList<SchemaList> model){
        ManageSchemasFragment fragment = new ManageSchemasFragment();
        fragment.model = model;
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
    }


    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);
    }

}
