package ca.bcit.project.c4985_asn03;
import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.provider.Settings.Secure;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;



/**
 * Created by BlackberryCosmos on 2018-03-27.
 */

public class Client {

    public static final int PORT_NO = 42069;

    Socket socket;
    OutputStream opToServer;
    DataOutputStream out;
    Context context;
    int android_id;

    public Client()
    {
//        try
//        {
//            //opToServer = socket.getOutputStream();
//            //out = new DataOutputStream(opToServer);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
        android_id = MainActivity.androidID.hashCode();
    }

    public boolean connect(String ipAddr)
    {
        try
        {
            socket = new Socket(ipAddr, PORT_NO);
            opToServer = socket.getOutputStream();
            out = new DataOutputStream(opToServer);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean send(String[] coordinates)
    {
        try
        {
            out.writeUTF(android_id + '/' + coordinates[0] + '/' +  coordinates[1]);
            return true;

        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeSocket()
    {
        try
        {
            socket.close();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

    }

}
