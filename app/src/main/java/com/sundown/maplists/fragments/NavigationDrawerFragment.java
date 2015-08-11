package com.sundown.maplists.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sundown.maplists.R;
import com.sundown.maplists.adapters.AdapterNavigationDrawer;
import com.sundown.maplists.utils.PreferenceManager;
import com.sundown.maplists.pojo.NavigationItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sundown on 4/7/2015.
 */
public class NavigationDrawerFragment extends Fragment implements AdapterNavigationDrawer.NavigationClickListener {


    public static final String KEY_USER_LEARNED_DRAWER="drawerLearned";


    //this class implements DrawerLayout.DrawerListener,
    // it ties together functionality of DrawerLayout and ActionBar (and ToolBar)
    private ActionBarDrawerToggle drawerToggle;

    private RecyclerView recyclerView;
    private AdapterNavigationDrawer adapter;
    private boolean userLearnedDrawer; //is user aware of drawer
    private boolean fromSavedInstanceState; //is fragment being started for very first time or is it coming back i.e. after rotation we dont want it to be open
    private View containerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userLearnedDrawer = PreferenceManager.getInstance().getBoolean(KEY_USER_LEARNED_DRAWER);

        if (savedInstanceState != null){ //if savedInstanceState is null the app is being started for the very first time,
            //not coming back from rotation
            fromSavedInstanceState = true;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
        adapter = new AdapterNavigationDrawer(getListItems(), this);
        recyclerView.setAdapter(adapter);
        //LayoutManager is responsible for measuring and positioning item views within RecycleView
        //as well as determining the policy for when to recycle item views no longer visible to user
        //there is LinearLayoutManager, StaggeredGridLayoutManager, GridLayoutManager..
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return layout;
    }

    public List<NavigationItem> getListItems(){
        List<NavigationItem> list = new ArrayList<>();
        int[] icons = {R.drawable.ic_number1, R.drawable.ic_number2, R.drawable.ic_number3, R.drawable.ic_number4};
        String[] titles = getResources().getStringArray(R.array.drawer_item_names);

        for (int i = 0; i < 4; ++i){
            list.add(new NavigationItem(icons[i%icons.length], titles[i%titles.length])); //this is cool!!
        }

        return list;
    }


    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId); //here you are getting view from fragment id on activity_main..
        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){


            //THIS METHOD only gets called once drawer is completely open
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //Log.d("debug", "onDrawerOpened");
                if (!userLearnedDrawer){ //if the user has never seen the drawer before
                    userLearnedDrawer = true; //well then they saw it just now
                    PreferenceManager prefs = PreferenceManager.getInstance();
                    prefs.putBoolean(KEY_USER_LEARNED_DRAWER, true);
                    prefs.apply();

                }

                //force activity to draw action bar again.. need this in both open/close because the drawer
                //will come in front of actionbar and block stuff
                getActivity().invalidateOptionsMenu();
            }

            //THIS METHOD only gets called once drawer is completely closed after being open
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //Log.d("debug", "onDrawerClosed");
                getActivity().invalidateOptionsMenu(); //make activity draw toolbar again
            }


            @Override //here were just using this method to change the alpha as the drawer slides...
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                //Log.d("debug", "offset: " + slideOffset);

                if (slideOffset <0.6){
                    toolbar.setAlpha(1 - slideOffset);
                }


            }
        };

        if (!userLearnedDrawer && !fromSavedInstanceState){
            drawerLayout.openDrawer(containerView);
        }
        drawerLayout.setDrawerListener(drawerToggle); //this is our listener that ties drawer functionality with toolbar
        drawerLayout.post(new Runnable(){ //view.post only reliably works if view is in window.. documentation not helpful here

            @Override
            public void run() {
                drawerToggle.syncState(); //this gets us the hamburger
                //synchronizes the state of the drawer indicator with the linked DrawerLayout
            }
        });
    }




    @Override
    public void itemClicked(View view, int position) {
        Log.d("debug", "position clicked: " + position);

    }
}
