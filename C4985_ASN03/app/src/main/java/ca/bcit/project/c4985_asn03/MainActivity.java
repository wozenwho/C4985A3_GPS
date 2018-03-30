package ca.bcit.project.c4985_asn03;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity  {

    BroadcastReceive receiver;
    public static String androidID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},1);


        Button connect = (Button)findViewById(R.id.connect);

        androidID = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                Button connect = (Button)findViewById(R.id.connect);
                connect.setEnabled(false);



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

        IntentFilter filter = new IntentFilter("CONN_FAILED");
        receiver = new BroadcastReceive();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

    }

    protected void onPause()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onPause();
    }


    private class BroadcastReceive extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT).show();
            Button connect = (Button)findViewById(R.id.connect);
            connect.setEnabled(true);
        }
    }



}

