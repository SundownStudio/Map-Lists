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


    public void stop() {
        liveQuery.stop();
    }

    public abstract ContentLoader start();
    public abstract void updateModel(QueryEnumerator result);
    public abstract void drawModel();



}
