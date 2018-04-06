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
 * CLASS:       LocationService
 *
 * FUNCTIONS:   onCreate()
 *              onStartCommand(Intent intent, int flags, int startId)
 *              onDestroy()
 *              onLocationChanged(Location location)
 *
 * DATE:        Apr. 29, 2018
 *
 * REVISIONS:
 *
 * DESIGNER:    Jeffrey Chou
 *
 * PROGRAMMER:  Jeffrey Chou
 *
 * NOTES:
 * A service that class that can run without focus on the phone. LocationService
 * also implements the LocationListener interface so OnLocationChanged can be used.
 */
public class LocationService extends Service implements LocationListener  {

    public LocationManager locationManager;
    public Client client;
    public String ip;
    int portNo;
    List<String> enabledProviders;


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    /**
     * FUNCTION:    onCreate
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Jeffrey Chou
     *
     * PROGRAMMER:  Jeffrey Chou
     *
     * INTERFACE:   onCreate()
     *
     * RETURNS:     void
     *
     * NOTES:
     * Initializes the location manager and client connection class
     *
     */
    public void onCreate()
    {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        client = new Client();

    }

    /**
     * FUNCTION:    onStartCommand
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Jeffrey Chou
     *
     * PROGRAMMER:  Jeffrey Chou
     *
     * INTERFACE:   onStartCommand(Intent intent, int flags, int startId)
     *                  Intent intent: Object that stores values received from the
     *                      main activity
     *                  int flags: flags for the service, not used in this program
     *                  int startId: start id for the service, not used in this program
     *
     * RETURNS:     START_STICKY, an int value that tells android allow this service to
     *                      run when it has no focus
     *
     * NOTES:
     * Grabs the ip and port input by the user and stores it. A background task is then called to
     * connect the app to the server using the ip and port values.
     *
     * The location manager is also set up to listen to location updates in this function
     *
     */
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

    /**
     * FUNCTION:    onDestroy
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Jeffrey Chou
     *
     * PROGRAMMER:  Jeffrey Chou
     *
     * INTERFACE:   onDestroy()
     *
     * RETURNS:     void
     *
     * NOTES:
     * Stops the location manager from receiving anymore updates and closes the
     * client socket
     *
     */
    public void onDestroy()
    {

        locationManager.removeUpdates(this);
        if(client != null)
            client.closeSocket();
        super.onDestroy();
    }


    /**
     * FUNCTION:    onLocationChanged
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Jeffrey Chou
     *
     * PROGRAMMER:  Jeffrey Chou
     *
     * INTERFACE:   onLocationChanged(Location location)
     *                  Location location: Location object that has information on the new location
     *
     * RETURNS:     void
     *
     * NOTES:
     * Callback function that is called when the phone determines the location has changed. Stores
     * the latitude and longitude into a string array and calls an async task to send the string
     * array to the server.
     *
     */
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
            boolean ret = client.connect(ip,portNo);
            if(ret == false)
            {
                Intent broad = new Intent();
                broad.setAction("COM_ERROR");
                broad.putExtra("msg", "\nConnection failed");
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
