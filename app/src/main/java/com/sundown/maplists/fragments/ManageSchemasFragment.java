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
import com.sundown.maplists.models.SchemaList;
import com.sundown.maplists.storage.ContentLoader;
import com.sundown.maplists.views.ManageSchemasView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Sundown on 8/26/2015.
 */
public class ManageSchemasFragment extends Fragment {

    private ManageSchemasView view;
    private ArrayList<SchemaList> model;
    private ContentLoader loader;

    public static ManageSchemasFragment getInstance(){
        return new ManageSchemasFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ManageSchemasView) inflater.inflate(R.layout.fragment_manage_schemas, container, false);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
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
        public Loader start() {
            liveQuery = db.getLiveQuery(db.QUERY_SCHEMA);
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
            model.clear();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                Map<String, Object> properties = db.read(row.getSourceDocumentId());
                model.add(new SchemaList().setProperties(properties));
            }

            if (model.size() > 0)
                drawModel();
        }

        @Override
        public void drawModel() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setList(model);
                }
            });
        }
    }
}
