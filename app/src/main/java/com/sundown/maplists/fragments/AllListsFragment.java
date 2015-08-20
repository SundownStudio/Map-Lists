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
import com.sundown.maplists.views.AllListsView;

import java.util.Iterator;
import java.util.Map;

import static com.sundown.maplists.pojo.MenuOption.GroupView.EDIT_DELETE;
import static com.sundown.maplists.pojo.MenuOption.GroupView.MAP_COMPONENTS;


/**
 * Created by Sundown on 4/30/2015.
 */
public class AllListsFragment extends Fragment {

    public int mapId;
    private AllListsView view;
    private LocationLists model;
    private ContentLoader loader;
    private AllListsView.AllListsListener listener;
    public void setListener(AllListsView.AllListsListener listener){ this.listener = listener;}
    private ToolbarManager toolbarManager;
    public void setToolbarManager(ToolbarManager toolbarManager){ this.toolbarManager = toolbarManager;}


    public static AllListsFragment newInstance(int mapId, AllListsView.AllListsListener listener, ToolbarManager toolbarManager){
        AllListsFragment fragment = new AllListsFragment();
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
        view = (AllListsView) inflater.inflate(R.layout.fragment_all_lists, container, false);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.m("LocationItemsFragment is VISIBLE onResume");
        setUserVisibleHint(true);

        loader = new Loader().start();

        toolbarManager.drawMenu(
                new MenuOption(MAP_COMPONENTS, false),
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
                Map<String, Object> properties = db.read(row.getSourceDocumentId());
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

