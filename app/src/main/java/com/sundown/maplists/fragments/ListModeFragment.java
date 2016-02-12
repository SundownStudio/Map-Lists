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
import com.sundown.maplists.Constants;
import com.sundown.maplists.R;
import com.sundown.maplists.models.lists.ListFactory;
import com.sundown.maplists.models.lists.SecondaryList;
import com.sundown.maplists.storage.ContentLoader;
import com.sundown.maplists.views.ListModeView;

import java.util.ArrayList;
import java.util.Map;


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

    public static ListModeFragment newInstance(int mapId, ListModeView.AllListsListener listener){
        ListModeFragment fragment = new ListModeFragment();
        fragment.mapId = mapId;
        fragment.listener = listener;
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
        loader = new Loader().start();
    }

    @Override
    public void onPause() {
        super.onPause();
        setUserVisibleHint(false);
        loader.stop();
    }

    private class Loader extends ContentLoader {

        @Override
        public LiveQuery getLiveQuery() {
            return db.getLiveQuery(db.QUERY_LOCATION, mapId);
        }

        @Override
        public void updateModel(QueryEnumerator result) {
            //todo: ultimately this should store into a model where we keep track of what changes, that way when displaying list we can update the index only..
            model.clear();
            while (result.hasNext()) {
                QueryRow row = result.next();
                Map<String, Object> properties = db.read(row.getSourceDocumentId());
                model.add((SecondaryList) ListFactory.createList(getResources(), Constants.TYPE_SECONDARY_LIST, mapId).setProperties(properties));
            }

            drawModel();
        }

        @Override
        public void drawModel() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.init(model, listener);
                }
            });
        }
    }
}

