
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

/**
 * CLASS:       MainActivity
 *
 * FUNCTIONS: OnCreate()
 *            OnResume()
 *            OnPause()
 *
 * DATE:        Apr. 29, 2018
 *
 *
 *
 * DESIGNER:    Jeffrey Chou
 *
 * PROGRAMMER:  Jeffrey Chou
 *
 * NOTES:
 * The GUI of the client app that grabs the data input by the user
 * passes it to the to LocationService service
 */

public class MainActivity extends AppCompatActivity  {

    BroadcastReceive receiver;
    public static String androidID;


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
     * Initializes the gui and button listeners of the android app
     *
     */
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
                String ip;
                int port = -1;
                ip = ipEdit.getText().toString();

                if(portEdit.getText().toString().trim().length() == 0)
                {
                    Toast.makeText(MainActivity.this, "No Port Input", Toast.LENGTH_SHORT).show();
                    return;
                }
                port = Integer.parseInt(portEdit.getText().toString());



                if(ip.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "No IP Input", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(port < 0)
                {
                    Toast.makeText(MainActivity.this, "Not a valid Port", Toast.LENGTH_SHORT).show();
                    return;
                }


                Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                serviceIntent.putExtra("ip", ip);
                serviceIntent.putExtra("port", port);
                startService(serviceIntent);
                Toast.makeText(MainActivity.this, "Started Location Service", Toast.LENGTH_SHORT).show();
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

    /**
     * FUNCTION:    onResume
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Jeffrey Chou
     *
     * PROGRAMMER:  Jeffrey Chou
     *
     * INTERFACE:   onResume()
     *
     * RETURNS:     void
     *
     * NOTES:
     * Initializes the BroadcastReceiver to listen to messages from the location
     * service
     *
     */
    protected void onResume()
    {
        super.onResume ();

        IntentFilter filter = new IntentFilter("COM_ERROR");
        receiver = new BroadcastReceive();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

    }

    /**
     * FUNCTION:    onPause
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Jeffrey Chou
     *
     * PROGRAMMER:  Jeffrey Chou
     *
     * INTERFACE:   onPause()
     *
     * RETURNS:     void
     *
     * NOTES:
     * Unregisters the broadcast receiver when the app loses focus
     *
     */
    protected void onPause()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onPause();
    }


    private class BroadcastReceive extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            Toast.makeText(context, intent.getAction() + intent.getStringExtra("msg"), Toast.LENGTH_SHORT).show();
            Button connect = (Button)findViewById(R.id.connect);
            connect.setEnabled(true);
        }
    }



}

