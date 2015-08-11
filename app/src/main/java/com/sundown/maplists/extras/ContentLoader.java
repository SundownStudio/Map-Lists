package com.sundown.maplists.extras;

import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.sundown.maplists.storage.DatabaseCommunicator;

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
    public abstract void displayItems(QueryEnumerator result);
    public abstract void drawModel();



}
