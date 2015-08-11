package com.sundown.maplists.pojo;

/**
 * Created by Sundown on 7/21/2015.
 */
public class MenuOption{

    public enum GroupView {
        EDIT_DELETE, MARKER_COMPONENTS, MARKER_MOVE, MARKER_NAVIGATION, MAP_COMPONENTS, MAP_ZOOMING
    }

    public GroupView groupView;
    public boolean show;

    public MenuOption(GroupView groupView, boolean show){
        this.groupView = groupView;
        this.show = show;
    }
}
