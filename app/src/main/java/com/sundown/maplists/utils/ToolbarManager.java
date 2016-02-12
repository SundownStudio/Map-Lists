package com.sundown.maplists.utils;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.sundown.maplists.R;

/**
 * Created by Sundown on 7/21/2015.
 */
public class ToolbarManager {

    public interface ToolbarListener {
        void topToolbarPressed(MenuItem item);
        void bottomToolbarPressed(MenuItem item);
    }

    public static final int DEFAULT_TOP = 1;
    public static final int SCHEMA_ACTIONS = 2;
    public static final int DEFAULT_ADDLIST = 3;

    private static ToolbarManager instance;
    private Toolbar toolbarTop;
    public Toolbar getToolbarTop(){ return toolbarTop; }
    private Toolbar toolbarBottom;
    private LinearLayout toolbarTopLayout;


    public static ToolbarManager getInstance(final Toolbar toolbarTop, final Toolbar toolbarBottom, final LinearLayout toolbarTopLayout, final ToolbarListener listener){
        if (instance == null) instance = new ToolbarManager();
        else instance.clear();
        instance.setup(toolbarTop, toolbarBottom, toolbarTopLayout, listener);
        return instance;
    }


    private ToolbarManager(){}

    private void clear(){
        toolbarTop.setOnMenuItemClickListener(null);
        toolbarBottom.setOnMenuItemClickListener(null);
    }

    private void setup(final Toolbar toolbarTop, final Toolbar toolbarBottom, final LinearLayout toolbarTopLayout, final ToolbarListener listener){
        this.toolbarTop = toolbarTop;
        this.toolbarBottom = toolbarBottom;
        this.toolbarTopLayout = toolbarTopLayout;


        toolbarTop.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                listener.topToolbarPressed(item);
                return true;
            }
        });

        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                listener.bottomToolbarPressed(item);
                return true;
            }
        });
    }

    public void drawMenu(int menuId, boolean show){
        if (toolbarBottom == null || toolbarTop == null) return;

        switch (menuId){

            case DEFAULT_TOP:
                toolbarTop.getMenu().setGroupVisible(R.id.group_default_top, show);
                break;

            case SCHEMA_ACTIONS:
                toolbarBottom.getMenu().setGroupVisible(R.id.group_schema_actions, show);
                break;

            case DEFAULT_ADDLIST:
                toolbarBottom.getMenu().setGroupVisible(R.id.group_addlist_default, show);
                break;
        }

        toolbarTopLayout.bringToFront();
    }

    public void setTopVisibility(int visibility){
        toolbarTopLayout.setVisibility(visibility);
    }

    public Menu getBottomMenu(){
        return toolbarBottom.getMenu();
    }
}
