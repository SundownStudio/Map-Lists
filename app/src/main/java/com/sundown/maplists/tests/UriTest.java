package com.sundown.maplists.tests;

import android.content.UriMatcher;
import android.net.Uri;

import com.sundown.maplists.logging.Log;

/**
 * Created by Sundown on 4/23/2015.
 */
public class UriTest {

    private static final int LOCOS = 10;
    private static final int LOCO_ID = 20;

    public UriTest() {
        Uri uri = Uri.parse("content://" + "com.sundown.inventory.contentprovider"
                + "/" + "ivn" + "/tablename/13");


        Log.m("uri.getPath() " + uri.getPath());
        Log.m("uri.getAuthority() " + uri.getAuthority());
        Log.m("uri.getLastPathSegment() " + uri.getLastPathSegment());
        Log.m("uri.getFragment() " + uri.getFragment());
        Log.m("uri.getPathSegments() " + uri.getPathSegments());
        Log.m("uri.getQuery() " + uri.getQuery());

        UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sURIMatcher.addURI("com.sundown.inventory.contentprovider", "ivn/*", LOCOS);
        sURIMatcher.addURI("com.sundown.inventory.contentprovider", "ivn/*" + "/#", LOCO_ID);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case LOCOS:
                Log.m("ContentProvider - Query for All Locations");
                break;

            case LOCO_ID:
                Log.m("ContentProvider - Query for ID: " + uri.getLastPathSegment());
                break;

            default:
                Log.m("ContentProvider - cant read");
                break;

        }

    }


}
