package ca.bcit.project.c4985_asn03;


import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

/**
 * Created by jdawg on 2018-03-28.
 */

public class LocationService extends Service implements LocationListener  {

    public LocationManager locationManager;
    public Client client;
    public String ip;
    List<String> enabledProviders;


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        client = new Client();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        ip = intent.getStringExtra("ip");
        int port = intent.getIntExtra("port", -1);
        new ServerConnection().execute();
        Criteria criteria = new Criteria ();
        criteria.setAccuracy (Criteria.ACCURACY_COARSE);
        enabledProviders = locationManager.getProviders(criteria, true);


        if (!enabledProviders.isEmpty()) {
            for (String enabledProvider : enabledProviders) {

                try {
                    locationManager.requestLocationUpdates(enabledProvider,5000,5, this, null);

                } catch (SecurityException e) {

                }
            }
        }
        return START_STICKY;
    }

    public void onDestroy()
    {

        locationManager.removeUpdates(this);
        if(client != null)
            client.closeSocket();
        super.onDestroy();
    }


    public void onLocationChanged(Location location)
    {
        String[] locationArr = new String[2];
        locationArr[0] ="" + location.getLatitude();
        locationArr[1] ="" + location.getLongitude();

        if(client.isConnected())
            new ServerSend().execute(locationArr);
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

    private class ServerConnection extends AsyncTask<Void, Void, Void>
    {
        protected Void doInBackground(Void ... params)
        {
            boolean ret = client.connect(ip);
            if(ret == false)
            {
                Intent broad = new Intent();
                broad.setAction("CONN_FAILED");
                LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(broad);
                stopSelf();
            }
            return null;
        }
    }

    private class ServerSend extends AsyncTask<String[], Void, Void>
    {
        protected Void doInBackground(String[] ... params)
        {


            boolean ret = client.send(params[0]);
            return null;
        }
    }


}
