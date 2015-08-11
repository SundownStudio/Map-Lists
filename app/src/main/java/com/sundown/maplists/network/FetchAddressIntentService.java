package com.sundown.maplists.network;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.sundown.maplists.R;
import com.sundown.maplists.extras.Constants;
import com.sundown.maplists.logging.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sundown on 7/22/2015.
 */
public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver mReceiver;


    //we need an empty default constructor or manifest doesnt like.. cest tres stupeeed..
    public FetchAddressIntentService(){super("empty");}

    //name is for worker thread
    public FetchAddressIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = new Bundle();

        // Get the location passed to this service through an extra.
        mReceiver = intent.getParcelableExtra(Constants.GEOCODE.RECEIVER);
        int operation = intent.getIntExtra(Constants.GEOCODE.GEO_OPERATION, 0);
        bundle.putString(Constants.GEOCODE.GEO_OPERATION, operation + "");

        Double latitude = intent.getDoubleExtra(Constants.GEOCODE.MAP_LATITUDE, 0f);
        Double longitude = intent.getDoubleExtra(Constants.GEOCODE.MAP_LONGITUDE, 0f);
        String add = intent.getStringExtra(Constants.GEOCODE.MAP_ADDRESS);
        List<Address> addresses = getAddresses(operation, add, latitude, longitude, bundle);
        handleResults(addresses, bundle, operation);
    }

    private List<Address> getAddresses(int operation, String address, double latitude, double longitude, Bundle bundle){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            if (operation == Constants.GEOCODE.FROM_ADDRESS) {
                return geocoder.getFromLocationName(address, 1);
            } else if (operation == Constants.GEOCODE.FROM_LATLNG){
                return geocoder.getFromLocation(latitude, longitude, 1);
            }

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            bundle.putString(Constants.GEOCODE.MAP_ERROR, getString(R.string.service_not_available));

        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            bundle.putString(Constants.GEOCODE.MAP_ERROR, getString(R.string.no_address_found));
        }

        return addresses;
    }


    private void handleResults(List<Address> addresses, Bundle bundle, int operation){
        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (!bundle.containsKey(Constants.GEOCODE.MAP_ERROR)){  bundle.putString(Constants.GEOCODE.MAP_ERROR, getString(R.string.no_address_found));}
            deliverResultToReceiver(Constants.GEOCODE.FAILURE_RESULT, bundle);

        } else {
            Address address = addresses.get(0);

            if (operation == Constants.GEOCODE.FROM_LATLNG) {
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }

                bundle.putString(Constants.GEOCODE.RESULT_DATA_KEY, TextUtils.join(System.getProperty("line.separator"), addressFragments));

            } else if (operation == Constants.GEOCODE.FROM_ADDRESS){
                bundle.putDouble(Constants.GEOCODE.MAP_LATITUDE, address.getLatitude());
                bundle.putDouble(Constants.GEOCODE.MAP_LONGITUDE, address.getLongitude());
            }

            Log.m(getString(R.string.address_found));
            deliverResultToReceiver(Constants.GEOCODE.SUCCESS_RESULT, bundle);

        }
    }


    private void deliverResultToReceiver(int resultCode, Bundle bundle) {
        mReceiver.send(resultCode, bundle);
    }

}
