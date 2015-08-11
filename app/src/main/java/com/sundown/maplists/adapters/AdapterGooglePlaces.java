package com.sundown.maplists.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Sundown on 8/11/2015.
 */
public class AdapterGooglePlaces extends ArrayAdapter implements Filterable {

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static String key;
    private ArrayList resultList;

    public AdapterGooglePlaces(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        key = context.getString(R.string.google_places_key);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return String.valueOf(resultList.get(index));
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override //Invoked in a worker thread to filter the data according to the constraint.
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override //Invoked in the UI thread to publish the filtering results in the user interface.
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }



    public ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null; //gingerbread or higher, below use HttpClient.. see i was right!!! of course you could always use both if you support 2.2 and up..
        StringBuilder jsonResults = new StringBuilder();

        try {

            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + key);
            //sb.append("&components=country:gr"); //todo automatic detection if you don't use this but it may not always work.. test in different locals..
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e("Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e("cant parse JSON results", e);
        }

        return resultList;
    }

}
