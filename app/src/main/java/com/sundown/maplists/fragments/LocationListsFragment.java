package com.sundown.maplists.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.LocationList;
import com.sundown.maplists.models.LocationLists;
import com.sundown.maplists.storage.ContentLoader;
import com.sundown.maplists.pojo.MenuOption;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.LocationListsView;

import java.util.Iterator;
import java.util.Map;

import static com.sundown.maplists.pojo.MenuOption.GroupView.EDIT_DELETE;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MAP_COMPONENTS;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MAP_ZOOMING;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MARKER_NAVIGATION;

/**
 * Created by Sundown on 4/30/2015.
 */
public class LocationListsFragment extends Fragment {

    public int mapId;
    private LocationListsView view;
    private LocationLists model;
    private ContentLoader loader;
    private LocationListsView.LocationListsListener listener;
    public void setListener(LocationListsView.LocationListsListener listener){ this.listener = listener;}
    private ToolbarManager toolbarManager;
    public void setToolbarManager(ToolbarManager toolbarManager){ this.toolbarManager = toolbarManager;}


    public static LocationListsFragment newInstance(int mapId, LocationListsView.LocationListsListener listener, ToolbarManager toolbarManager){
        LocationListsFragment fragment = new LocationListsFragment();
        fragment.mapId = mapId;
        fragment.listener = listener;
        fragment.toolbarManager = toolbarManager;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new LocationLists();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (LocationListsView) inflater.inflate(R.layout.fragment_location, container, false);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.m("LocationItemsFragment is VISIBLE onResume");
        setUserVisibleHint(true);

        loader = new Loader().start();

        toolbarManager.drawMenu(new MenuOption(MAP_ZOOMING, false),
                new MenuOption(MAP_COMPONENTS, false),
                new MenuOption(MARKER_NAVIGATION, false),
                new MenuOption(EDIT_DELETE, false));

    }


    @Override
    public void onPause() {
        super.onPause();
        Log.m("LocationFragment is INVISIBLE onPause");
        setUserVisibleHint(false);
        loader.stop();
    }


    private class Loader extends ContentLoader {

        @Override
        public Loader start() {
            liveQuery  = db.getLiveQuery(db.QUERY_LOCATION, mapId);
            if (liveQuery != null) {
                liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
                    @Override
                    public void changed(LiveQuery.ChangeEvent event) {
                        if (event.getSource().equals(liveQuery)) {
                            updateModel(event.getRows());
                        }
                    }
                });
                liveQuery.start();
            }
            return this;
        }

        @Override
        public void updateModel(QueryEnumerator result) {
            //todo: ultimately this should store into a model where we keep track of what changes, that way when displaying list we can update the index only..
            model.clear();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                Map<String, Object> properties = db.read(row.getSourceDocumentId()); //todo: can also use row.getDocument.. try this afterwards

                model.addItem(new LocationList(mapId).setProperties(properties));
            }

            drawModel();
        }

        @Override
        public void drawModel() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setListAndListener(model.getList(), listener);
                }
            });
        }
    }

}

