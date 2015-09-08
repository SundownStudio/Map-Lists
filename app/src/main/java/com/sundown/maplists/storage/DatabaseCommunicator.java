package com.sundown.maplists.storage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Revision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.sundown.maplists.MapListsApp;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.AbstractList;
import com.sundown.maplists.models.PhotoField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.sundown.maplists.storage.JsonConstants.LIST_ID;
import static com.sundown.maplists.storage.JsonConstants.MAP_ID;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
import static com.sundown.maplists.storage.JsonConstants.TYPE_LOCATION_LIST;
import static com.sundown.maplists.storage.JsonConstants.TYPE_MAP_LIST;
import static com.sundown.maplists.storage.JsonConstants.TYPE_SCHEMA_LIST;

/**
 * Created by Sundown on 7/14/2015.
 */
public class DatabaseCommunicator {

    public static final int QUERY_MAP = 1;
    public static final int QUERY_LOCATION = 2;
    public static final int QUERY_SCHEMA = 3;
    public static final int MAX_ITEMS_PER_LIST = 9999;
    private static final String IMAGE_TYPE = "image/jpg";

    private static CBManager cbManager;

    private static DatabaseCommunicator instance;
    public static DatabaseCommunicator getInstance(){
        if (instance == null) instance = new DatabaseCommunicator();
        return instance;
    }


    private DatabaseCommunicator(){
        cbManager = new CBManager();
    }

    public Map<String, Object> read(String documentId){
        return cbManager.database.getDocument(documentId).getProperties();
    }


    public void insert(final AbstractList list, String countId, String idType) {
        try {
            cbManager.insert(list, countId, idType);
        } catch (CouchbaseLiteException e) {
            Log.e(e); //TODO: something went wrong dialog
        }
    }


    public void update(final AbstractList list) {
        try {
            cbManager.update(list);
        } catch (CouchbaseLiteException e) {
            Log.e(e); //TODO: something went wrong dialog
        }
    }


    public void delete(String documentId, int mapId, Operation operation){
        try {
            cbManager.delete(documentId, mapId, operation);
        } catch (CouchbaseLiteException e) {
            Log.e(e); //TODO: something went wrong dialog
        }
    }

    public void deleteAttachment(String documentId, String attachmentName) {
        try {
            cbManager.deleteAttachment(documentId, attachmentName);
        } catch (CouchbaseLiteException e) {
            Log.e(e); //TODO: something went wrong dialog
        }
    }


    public Bitmap loadBitmap(String documentId, String bitmapName){
        if (bitmapName != null && bitmapName.length() > 0) {
            try {
                Revision currentRevision = cbManager.getCurrentRevision(documentId);
                Attachment att = currentRevision.getAttachment(bitmapName);
                if (att != null) {
                    InputStream is;
                    is = att.getContent();
                    return BitmapFactory.decodeStream(is);
                }
            } catch (CouchbaseLiteException e) {
                Log.e(e); //TODO: something went wrong dialog
            }
        }
        return null;
    }

    public LiveQuery getLiveQuery(int query){
        if (query == QUERY_MAP){
            return cbManager.getMapQuery().toLiveQuery();
        } else if (query == QUERY_SCHEMA){
            return cbManager.getSchemaQuery().toLiveQuery();
        }
        return null;
    }

    public LiveQuery getLiveQuery(int query, int mapId){
        if (query == QUERY_LOCATION){
            return cbManager.getLocationQuery(mapId).toLiveQuery();
        }
        return null;
    }


    private class CBManager {

        private static final String DATABASE_NAME = "inventory-db";
        private static final String TAG = "inventory";
        private static final String VIEW_BY_MAP_ID = "view-by-map-id";
        private static final String VIEW_BY_LIST_ID = "view-by-list-id";
        private static final String VIEW_BY_SCHEMA_ID = "view-by-schema-id";
        private static final String VIEW_VERSION = "1";
        private static final String DOC_COUNTS = "doc-counts";



        private Manager manager;
        private Database database;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


        private CBManager(){     //need to start this in onCreate of main activity..
            Manager.enableLogging(TAG, com.couchbase.lite.util.Log.VERBOSE);
            Manager.enableLogging(com.couchbase.lite.util.Log.TAG, com.couchbase.lite.util.Log.VERBOSE);
            Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC_ASYNC_TASK, com.couchbase.lite.util.Log.VERBOSE);
            Manager.enableLogging(com.couchbase.lite.util.Log.TAG_SYNC, com.couchbase.lite.util.Log.VERBOSE);
            Manager.enableLogging(com.couchbase.lite.util.Log.TAG_QUERY, com.couchbase.lite.util.Log.VERBOSE);
            Manager.enableLogging(com.couchbase.lite.util.Log.TAG_VIEW, com.couchbase.lite.util.Log.VERBOSE);
            Manager.enableLogging(com.couchbase.lite.util.Log.TAG_DATABASE, com.couchbase.lite.util.Log.VERBOSE);

            try {
                manager = new Manager(new AndroidContext(MapListsApp.getContext()), Manager.DEFAULT_OPTIONS);
                database = manager.getDatabase(DATABASE_NAME);

            } catch (IOException e) {
                Log.e(e);
            } catch (CouchbaseLiteException e) {
                Log.e(e);
            }

            initViews();
            createDefaultDocs();

        }

        private void initViews(){
            //view map/reduce functions are not persistent and must be registered at runtime so do this after loading manager/database

            //todo see if its better to merge these by type.. if so, then on livequery do we need to be able to differentiate?..
            View viewByMapId = database.getView(VIEW_BY_MAP_ID);
            viewByMapId.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> properties, Emitter emitter) {
                    String type = (String) properties.get(TYPE);
                    if (type.equals(TYPE_MAP_LIST)) {
                        emitter.emit(properties.get(JsonConstants.MAP_ID), null);
                    }
                }
            }, VIEW_VERSION);     //view last parameter is version, this is retained so if map/reduce updated it will go back


            View viewByItemId = database.getView(VIEW_BY_LIST_ID);
            viewByItemId.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> properties, Emitter emitter) {
                    String type = (String) properties.get(TYPE);
                    if (type.equals(TYPE_LOCATION_LIST)) {
                        int[] arr = new int[]{(Integer) properties.get(MAP_ID), (Integer) properties.get(LIST_ID)};
                        emitter.emit(arr, null);
                    }
                }
            }, VIEW_VERSION);     //view last parameter is version, this is retained so if map/reduce updated it will go back


            View viewBySchemaId = database.getView(VIEW_BY_SCHEMA_ID);
            viewBySchemaId.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get(TYPE);
                    if (type.equals(TYPE_SCHEMA_LIST)) {
                        emitter.emit(document.get(JsonConstants.SCHEMA_ID), null);
                    }
                }
            }, VIEW_VERSION);
        }


        public Query getMapQuery(){ return database.getView(VIEW_BY_MAP_ID).createQuery(); }

        public Query getLocationQuery(int mapId){
            Query query =  database.getView(VIEW_BY_LIST_ID).createQuery();

            query.setStartKey(new int[]{mapId, 0});
            query.setEndKey(new int[]{mapId, MAX_ITEMS_PER_LIST});
            return query;
        }

        public Query getSchemaQuery(){ return database.getView(VIEW_BY_SCHEMA_ID).createQuery();}


        public void insert(AbstractList list, String countId, String idName) throws CouchbaseLiteException {
            int count = increaseCount(countId);

            UUID uuid = UUID.randomUUID();
            Calendar calendar = GregorianCalendar.getInstance();
            long currentTime = calendar.getTimeInMillis();
            String currentTimeString = dateFormatter.format(calendar.getTime());

            String id = currentTime + "-" + uuid.toString();

            Document document = database.createDocument();
            Map<String, Object> properties = list.getProperties();
            properties.put("_id", id);
            properties.put("created_at", currentTimeString);
            properties.put(idName, count); //mapID = count, listID = count, schemaID = count.. all increasing

            if (idName.equals(JsonConstants.SCHEMA_ID)){
                saveDocument(document.createRevision(), properties, null);
            } else {
                saveDocument(document.createRevision(), properties, list.getPhotos());
            }

        }

        public void update(final AbstractList list) throws CouchbaseLiteException {
            Document doc = database.getDocument(list.getDocumentId());
            saveDocument(doc.createRevision(), list.getProperties(), list.getPhotos());
        }


        private void saveDocument(UnsavedRevision newRevision, final Map<String, Object> properties, final ArrayList<PhotoField> photoFields) throws CouchbaseLiteException {
            newRevision.setUserProperties(properties);

            if (photoFields != null){
                for (PhotoField photoField : photoFields){
                    newRevision = setImageAttachment(newRevision, photoField.getImageName(), photoField.getImageBitmap());
                    newRevision = setImageAttachment(newRevision, photoField.getThumbName(), photoField.getThumbBitmap());
                    photoField.recycle(true);
                }
            }
            newRevision.save();
        }

        private UnsavedRevision setImageAttachment(UnsavedRevision newRevision, String fileName, Bitmap image){
            if (image != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 50, out);
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                newRevision.setAttachment(fileName, IMAGE_TYPE, in);
            }
            return newRevision;
        }

        public void delete(String documentId, int mapId, Operation operation) throws CouchbaseLiteException {

            if (operation == Operation.DELETE_LOCATION){

                Query locationsQuery = getLocationQuery(mapId);

                QueryEnumerator result = locationsQuery.run();

                //delete all lists at this location
                for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    deleteDocument(row.getSourceDocumentId());
                }


                //now delete document
                if (deleteDocument(documentId)) {
                    removeCount(String.valueOf(mapId));
                }

            } else if (operation == Operation.DELETE_LOCATION_LIST){
                if (deleteDocument(documentId)){
                    decreaseCount(String.valueOf(mapId));
                }

            } else if (operation == Operation.DELETE_SCHEMA){
                deleteDocument(documentId);
            }

            //todo: when do we compact? We need to get rid of big attachments too.. look up if that gets taken care of when deleting doc.. iirc it gets handled automatically on compact


        }

        public void deleteAttachment(String documentId, String attachmentName) throws CouchbaseLiteException {
            Document doc = database.getExistingDocument(documentId);
            if (doc != null) {
                UnsavedRevision newRev = doc.getCurrentRevision().createRevision();
                newRev.removeAttachment(attachmentName);
                //could also update newRev.properties while here.. potentially packing deletes together although would need to handle rotation..
                newRev.save();
            }
        }

        public Revision getCurrentRevision(String documentId){
            return database.getDocument(documentId).getCurrentRevision();
        }

        private void removeCount(String countId) throws CouchbaseLiteException {
            Document counts = database.getDocument(DOC_COUNTS);
            Map<String, Object> countProps = new HashMap<>();
            countProps.putAll(counts.getProperties());

            countProps.remove(countId);
            counts.putProperties(countProps);
        }

        private void decreaseCount(String countId) throws CouchbaseLiteException {
            Document counts = database.getDocument(DOC_COUNTS);
            Map<String, Object> countProps = new HashMap<>();
            countProps.putAll(counts.getProperties());

            int x = ((Integer) counts.getProperty(countId)) - 1;
            counts.putProperties(countProps);

        }


        private int increaseCount(String countId) throws CouchbaseLiteException {
            Document counts = database.getDocument(DOC_COUNTS);
            Map<String, Object> countProps = new HashMap<>();
            countProps.putAll(counts.getProperties());

            int x = 0;

            String num = String.valueOf(counts.getProperty(countId));
            if (num != null && !num.equals("null")){
                x = Integer.parseInt(num);
            }
            countProps.put(countId, ++x);
            counts.putProperties(countProps);
            return x;
        }

        private boolean deleteDocument(String id) throws CouchbaseLiteException {
            return database.getDocument(id).delete();
        }

        private void createDefaultDocs(){
            Document existingDocument = database.getExistingDocument(DOC_COUNTS);
            if (existingDocument == null) {
                createCountsDoc();
            }
        }


        private void createCountsDoc(){

            //create the counts doc if it doesn't exist yet
            Map<String, Object> properties = new HashMap(3);
            properties.put(TYPE, JsonConstants.COUNTS);
            properties.put(JsonConstants.COUNT_MAP_LISTS, 0);
            properties.put(JsonConstants.COUNT_SCHEMAS, 0);
            Document document = database.getDocument(DOC_COUNTS);
            try {
                document.putProperties(properties);
            } catch (CouchbaseLiteException e) {
                com.couchbase.lite.util.Log.e(TAG, "doc counts already exists", e);
            }
        }

    }
}
