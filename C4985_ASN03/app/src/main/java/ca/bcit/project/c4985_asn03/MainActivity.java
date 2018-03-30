package ca.bcit.project.c4985_asn03;

import android.Manifest;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private LocationManager locationManager;
    List<String> enabledProviders;
    boolean connected = false;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},1);
        //locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        TextView ctxt = (TextView)findViewById(R.id.countText);
        ctxt.setText("" + count);
        Button connect = (Button)findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connected = true;
                Button connect = (Button)findViewById(R.id.connect);
                connect.setEnabled(false);
                EditText ipEdit = (EditText)findViewById(R.id.ipEdit);
                EditText portEdit = (EditText)findViewById(R.id.portEdit);
                String ip = null;
                int port = -1;
                try {
                    ip = ipEdit.getText().toString();
                    port = Integer.parseInt(portEdit.getText().toString());

                }
                catch(NullPointerException e)
                {

                }
                Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                serviceIntent.putExtra("ip", ip);
                serviceIntent.putExtra("port", port);
                startService(serviceIntent);



            }
        });

        Button disconnect = (Button)findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopService(new Intent(MainActivity.this, LocationService.class));
                Button connect = (Button)findViewById(R.id.connect);
                connect.setEnabled(true);
            }
        });
    }

    protected void onResume()
    {
        super.onResume ();

        /*
        StringBuffer stringBuffer = new StringBuffer ();
        Criteria criteria = new Criteria ();
        criteria.setAccuracy (Criteria.ACCURACY_COARSE);
        enabledProviders = locationManager.getProviders (criteria, true);


        if(!enabledProviders.isEmpty())
        {
            for (String enabledProvider : enabledProviders) {
                stringBuffer.append(enabledProvider).append(" ");
                try {
                    locationManager.requestLocationUpdates(enabledProvider,0,0, this, null);
                    Log.d("message: ", "after request");
                }
                catch(SecurityException e){

                }
            }
        }*/
        //TextView txt = (TextView)findViewById(R.id.locationText);


    }



    public void onLocationChanged(Location location)
    {
        String locationStr;
        count++;
        locationStr = location.getLatitude() + "/" + location.getLongitude();
        TextView txt = (TextView)findViewById(R.id.locationText);
        txt.setText(locationStr);
        TextView ctxt = (TextView)findViewById(R.id.countText);
        ctxt.setText("" + count);
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

