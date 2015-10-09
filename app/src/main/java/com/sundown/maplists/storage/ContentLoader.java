package com.sundown.maplists.storage;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;

/**
 * Created by Sundown on 6/24/2015.
 */
public abstract class ContentLoader {

    public DatabaseCommunicator db;
    public LiveQuery liveQuery;

    public ContentLoader(){
        db = DatabaseCommunicator.getInstance();
    }

    public ContentLoader start(){
        liveQuery = getLiveQuery();
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

    public void stop() {
        liveQuery.stop();
    }

    public abstract LiveQuery getLiveQuery();
    public abstract void updateModel(QueryEnumerator result);
    public abstract void drawModel();
}
