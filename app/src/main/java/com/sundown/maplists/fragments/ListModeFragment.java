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
import com.sundown.maplists.models.ListFactory;
import com.sundown.maplists.models.ListType;
import com.sundown.maplists.models.SecondaryList;
import com.sundown.maplists.pojo.MenuOption;
import com.sundown.maplists.storage.ContentLoader;
import com.sundown.maplists.utils.ToolbarManager;
import com.sundown.maplists.views.ListModeView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static com.sundown.maplists.pojo.MenuOption.GroupView.DEFAULT_TOP;


/**
 * Created by Sundown on 4/30/2015.
 */
public class ListModeFragment extends Fragment {

    public int mapId;
    private ListModeView view;
    private ArrayList<SecondaryList> model;
    private ContentLoader loader;
    private ListModeView.AllListsListener listener;
    public void setListener(ListModeView.AllListsListener listener){ this.listener = listener;}
    private ToolbarManager toolbarManager;
    public void setToolbarManager(ToolbarManager toolbarManager){ this.toolbarManager = toolbarManager;}


    public static ListModeFragment newInstance(int mapId, ListModeView.AllListsListener listener, ToolbarManager toolbarManager){
        ListModeFragment fragment = new ListModeFragment();
        fragment.mapId = mapId;
        fragment.listener = listener;
        fragment.toolbarManager = toolbarManager;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ArrayList();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ListModeView) inflater.inflate(R.layout.fragment_list_mode, container, false);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);
        loader.stop();
    }

    public void startLoader(){
        loader = new Loader().start();
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
                model.add((SecondaryList) ListFactory.createList(getResources(), ListType.SECONDARY, mapId).setProperties(properties));
            }

            drawModel();
        }

        @Override
        public void drawModel() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarManager.drawMenu(new MenuOption(DEFAULT_TOP, false));
                    view.init(model, listener);
                }
            });
        }
    }

}

