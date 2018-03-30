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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

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
        Log.d("yo", "got in create");

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
                //stringBuffer.append(enabledProvider).append(" ");
                try {
                    locationManager.requestLocationUpdates(enabledProvider,0,0, this, null);
                    //Log.d("good", "got past update");
                } catch (SecurityException e) {
                    Log.d("shit", "got in catch");
                }
            }
        }
        return START_STICKY;
    }

    public void onDestroy()
    {
        Log.d("yo", "got in destroy");
        locationManager.removeUpdates(this);
        if(client != null)
            client.closeSocket();
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
        String[] locationArr = new String[2];
        locationArr[0] ="" + location.getLatitude();
        locationArr[1] ="" + location.getLongitude();

        Log.d("message: ", locationArr[0] + "/" + locationArr[1]);
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
