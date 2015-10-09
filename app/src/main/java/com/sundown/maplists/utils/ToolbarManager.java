package com.sundown.maplists.utils;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.pojo.MenuOption;

/**
 * Created by Sundown on 7/21/2015.
 */
public class ToolbarManager {

    public interface ToolbarListener {
        void topToolbarPressed(MenuItem item);
        void bottomToolbarPressed(MenuItem item);
    }

    private Toolbar toolbarTop;
    public Toolbar getToolbarTop(){ return toolbarTop; }
    private Toolbar toolbarBottom;
    private LinearLayout toolbarTopLayout;
    private ToolbarListener listener;


    public ToolbarManager(Toolbar toolbarTop, Toolbar toolbarBottom, LinearLayout toolbarTopLayout, ToolbarListener listener){
        this.toolbarTop = toolbarTop;
        this.toolbarBottom = toolbarBottom;
        this.toolbarTopLayout = toolbarTopLayout;
        this.listener = listener;
        setup();
    }


    private void setup(){
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

    public void drawMenu(MenuOption... options){
        if (toolbarBottom == null || toolbarTop == null)
            return;

        for (MenuOption option: options){
            Log.m("toolbar", "drawing toolbar " + option);
            switch(option.groupView){

                case EDIT_DELETE:
                    toolbarBottom.getMenu().setGroupVisible(R.id.group_edit_delete, option.show);
                    break;

                case MARKER_COMPONENTS:
                    toolbarBottom.getMenu().setGroupVisible(R.id.group_marker_components, option.show);
                    break;

                case MARKER_MOVE:
                    toolbarBottom.getMenu().setGroupVisible(R.id.group_marker_move, option.show);
                    break;

                case DEFAULT_TOP:
                    toolbarTop.getMenu().setGroupVisible(R.id.group_default_top, option.show);
                    break;

                case SCHEMA_ACTIONS:
                    toolbarBottom.getMenu().setGroupVisible(R.id.group_schema_actions, option.show);
                    break;

                case DEFAULT_ADDLIST:
                    toolbarBottom.getMenu().setGroupVisible(R.id.group_addlist_default, option.show);
                    break;

            }
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
