package ca.bcit.project.c4985_asn03;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jdawg on 2018-03-28.
 */

public class LocationService extends Service implements LocationListener  {

    public LocationManager locationManager;
    List<String> enabledProviders;


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        Log.d("yo", "got in create");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria ();
        criteria.setAccuracy (Criteria.ACCURACY_COARSE);

        enabledProviders = locationManager.getProviders(criteria, true);


        if (!enabledProviders.isEmpty()) {
            for (String enabledProvider : enabledProviders) {
                //stringBuffer.append(enabledProvider).append(" ");
                try {
                    locationManager.requestLocationUpdates(enabledProvider,0,0, this, null);
                    //Log.d("good", "got past update");
                } catch (SecurityException e) {
                    Log.d("shit", "got in catch");
                }
            }
        }
    }

    public void onDestroy()
    {
        Log.d("yo", "got in destroy");
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    /*
    protected void onHandleIntent(Intent intent)
    {
        Log.d("message: ", "started service");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria ();
        criteria.setAccuracy (Criteria.ACCURACY_COARSE);
        while(true) {

            enabledProviders = locationManager.getProviders(criteria, true);


            if (!enabledProviders.isEmpty()) {
                for (String enabledProvider : enabledProviders) {
                    //stringBuffer.append(enabledProvider).append(" ");
                    try {
                        locationManager.requestSingleUpdate(enabledProvider, this, null);
                        //Log.d("good", "got past update");
                    } catch (SecurityException e) {
                        Log.d("shit", "got in catch");
                    }
                }
            }
        }
    }*/

    public void onLocationChanged(Location location)
    {
        String locationStr;
        locationStr = location.getLatitude() + "/" + location.getLongitude();

        Log.d("message: ", locationStr);
    }

    public void onProviderDisabled(String provider)
    {

    }

    public void onProviderEnabled(String provider)
    {

    }

    public void onStatusChanged (String provider, int status, Bundle extras)
    {

    }



}
