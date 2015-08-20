package com.sundown.maplists.pojo;

/**
 * Created by Sundown on 7/21/2015.
 */
public class MenuOption{

    public enum GroupView {
        EDIT_DELETE, MARKER_COMPONENTS, MARKER_MOVE, MAP_COMPONENTS
    }

    public GroupView groupView;
    public boolean show;

    public MenuOption(GroupView groupView, boolean show){
        this.groupView = groupView;
        this.show = show;
    }

    public String toString(){
        return "Groupview: " + groupView + " show: " + show;
    }
}
