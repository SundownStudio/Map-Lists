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
import com.sundown.maplists.MapLists;
import com.sundown.maplists.extras.Constants;
import com.sundown.maplists.extras.Operation;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.PhotoField;
import com.sundown.maplists.models.Item;

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

import static com.sundown.maplists.storage.JsonConstants.ITEM_ID;
import static com.sundown.maplists.storage.JsonConstants.MAP_ID;
import static com.sundown.maplists.storage.JsonConstants.TYPE;
import static com.sundown.maplists.storage.JsonConstants.TYPE_LOCATION_ITEM;
import static com.sundown.maplists.storage.JsonConstants.TYPE_MAP_ITEM;

/**
 * Created by Sundown on 7/14/2015.
 */
public class DatabaseCommunicator {

    public static final int QUERY_MAP = 1;
    public static final int QUERY_LOCATION = 2;

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


    public void insert(final Item item, String countId, String idType) {
        try {
            cbManager.insert(item, countId, idType);
        } catch (CouchbaseLiteException e) {
            Log.e(e); //TODO: something went wrong dialog
        }
    }


    public void update(final Item item) {
        try {
            cbManager.update(item);
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


    public Bitmap loadBitmap(String documentId, String pictureName){
        try {
            Revision currentRevision = cbManager.getCurrentRevision(documentId);
            Attachment att = currentRevision.getAttachment(pictureName);
            if (att != null) {
                InputStream is;
                is = att.getContent();
                return BitmapFactory.decodeStream(is);
            }
        } catch (CouchbaseLiteException e) {
            Log.e(e); //TODO: something went wrong dialog
        }
        return null;
    }


    public LiveQuery getLiveQuery(int query, int mapId){
        switch (query){
            case QUERY_MAP:
                return cbManager.getMapQuery().toLiveQuery();
            case QUERY_LOCATION:
                return cbManager.getLocationQuery(mapId).toLiveQuery();
        }
        return null;
    }


    private class CBManager {

        private static final String DATABASE_NAME = "inventory-db";
        private static final String TAG = "inventory";
        private static final String VIEW_BY_MAP_ID = "view-by-map-id";
        private static final String VIEW_BY_ITEM_ID = "view-by-item-id";
        private static final String VIEW_VERSION = "1";
        private static final String DOC_COUNTS = "doc-counts";
        private static final String DOC_DEFAULT_SCHEMA = "doc-default-schemas";


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
                manager = new Manager(new AndroidContext(MapLists.getContext()), Manager.DEFAULT_OPTIONS);
                database = manager.getDatabase(DATABASE_NAME);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
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
                    if (type.equals(TYPE_MAP_ITEM)) {
                        emitter.emit(properties.get(JsonConstants.MAP_ID), null);
                    }
                }
            }, VIEW_VERSION);     //view last parameter is version, this is retained so if map/reduce updated it will go back


            View viewByItemId = database.getView(VIEW_BY_ITEM_ID);
            viewByItemId.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> properties, Emitter emitter) {
                    String type = (String) properties.get(JsonConstants.TYPE);
                    if (type.equals(TYPE_LOCATION_ITEM)) {
                        int[] arr = new int[]{(Integer) properties.get(MAP_ID), (Integer) properties.get(ITEM_ID)};
                        emitter.emit(arr, null);
                    }
                }
            }, VIEW_VERSION);     //view last parameter is version, this is retained so if map/reduce updated it will go back
        }


        public Query getMapQuery(){ return database.getView(VIEW_BY_MAP_ID).createQuery(); }

        public Query getLocationQuery(int mapId){
            Query query =  database.getView(VIEW_BY_ITEM_ID).createQuery();

            query.setStartKey(new int[]{mapId, 0});
            query.setEndKey(new int[]{mapId, Constants.LIMITS.MAX_ITEMS_PER_LIST});
            return query;
        }

        public void insert(Item item, String countId, String idType) throws CouchbaseLiteException {
            int count = increaseCount(countId);

            UUID uuid = UUID.randomUUID();
            Calendar calendar = GregorianCalendar.getInstance();
            long currentTime = calendar.getTimeInMillis();
            String currentTimeString = dateFormatter.format(calendar.getTime());

            String id = currentTime + "-" + uuid.toString();

            Document document = database.createDocument();
            Map<String, Object> properties = item.getProperties();
            properties.put("_id", id);
            properties.put("created_at", currentTimeString);
            properties.put(idType, count);

            saveDocument(document.createRevision(), properties, item.getPhotos());
        }

        public void update(final Item item) throws CouchbaseLiteException {
            Document doc = database.getDocument(item.documentId);
            saveDocument(doc.createRevision(), item.getProperties(), item.getPhotos());
        }


        private void saveDocument(UnsavedRevision newRevision, final Map<String, Object> properties, final ArrayList<PhotoField> photoFields) throws CouchbaseLiteException {
            newRevision.setUserProperties(properties);

            if (photoFields != null){
                for (PhotoField photoField : photoFields){
                    if (photoField.image != null) {
                        newRevision = setImageAttachment(newRevision, photoField.imageName, photoField.image);
                    }
                    if (photoField.thumb != null) {
                        newRevision = setImageAttachment(newRevision, photoField.thumbName, photoField.thumb);
                    }

                    photoField.recycle(true);
                }
            }

            newRevision.save();
        }

        private UnsavedRevision setImageAttachment(UnsavedRevision newRevision, String fileName, Bitmap image){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 50, out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            newRevision.setAttachment(fileName, "image/jpg", in);
            return newRevision;
        }

        public void delete(String documentId, int mapId, Operation operation) throws CouchbaseLiteException {

            if (operation == Operation.DELETE_LOCATION){

                Query locationsQuery = getLocationQuery(mapId);

                QueryEnumerator result = locationsQuery.run();

                //delete all items at this location
                for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                    QueryRow row = it.next();
                    deleteDocument(row.getSourceDocumentId());
                }


                //now delete document
                if (deleteDocument(documentId)) {
                    removeCount(String.valueOf(mapId));
                }

            } else if (operation == Operation.DELETE_LOCATION_ITEM){
                if (deleteDocument(documentId)){
                    decreaseCount(String.valueOf(mapId));
                }
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

            existingDocument = database.getExistingDocument(DOC_DEFAULT_SCHEMA);
            if (existingDocument == null){
                createSchemasDoc();
            }
        }


        private void createCountsDoc(){

            //create the counts doc if it doesn't exist yet
            Map<String, Object> properties = new HashMap(3);
            properties.put(TYPE, JsonConstants.COUNTS);
            properties.put(JsonConstants.COUNT_MAP_ITEMS, 0);
            properties.put(JsonConstants.COUNT_SCHEMAS, 0);
            Document document = database.getDocument(DOC_COUNTS); //todo make this global.. see if it works..
            try {
                document.putProperties(properties);
            } catch (CouchbaseLiteException e) {
                com.couchbase.lite.util.Log.e(TAG, "doc already exists", e);
            }
        }

        private void createSchemasDoc(){
            //todo
        }

    }


}
