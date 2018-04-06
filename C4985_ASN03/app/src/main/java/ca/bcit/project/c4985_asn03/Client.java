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
 * CLASS:       Client
 *
 * FUNCTIONS:   Client()
 *              boolean connect(String ipAddr)
 *              boolean send(String[] coordinates)
 *              boolean closeSocket()
 *              boolean isConnected()
 *
 * DATE:        Apr. 29, 2018
 *
 * REVISIONS:
 *
 * DESIGNER:    Wilson Hu
 *
 * PROGRAMMER:  Wilson Hu
 *
 * NOTES:
 * The Client class holds the connection methods used to connect the app to
 * the remote server.
 *
 * This class contains the socket and stream variables used to write data
 * to the socket, which is to be read by the server.
 */
public class Client {

    public static final int PORT_NO = 42069;

    Socket socket;
    OutputStream opToServer;
    DataOutputStream out;
    Context context;
    Integer android_id;
    boolean connected = false;


    /**
     * FUNCTION:    Client constructor
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Wilson Hu
     *
     * PROGRAMMER:  Wilson Hu
     *
     * INTERFACE:   Client()
     *
     * RETURNS:
     *
     * NOTES:
     * This function is the client class' constructor.
     * It sets the android_id variable to a hashed value of
     * the device's unique AndroidID. The absolute value of the
     * hash is used to prevent sending negative ID values.
     *
     */
    public Client()
    {
        android_id = Math.abs(MainActivity.androidID.hashCode());
    }


    /**
     * FUNCTION:    connect
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Wilson Hu
     *
     * PROGRAMMER:  Wilson Hu
     *
     * INTERFACE:   connect(String ipAddr)
     *                  String ipAddr: the IP address String of the server
     *
     * RETURNS:     boolean - indicates if close socket operation succeeds or fails
     *
     * NOTES:
     * This function is used to establish a TCP connection between the client app and
     * the server program.
     *
     * This function creates the socket as well as the datastreams used to transmit
     * data through the socket.
     */
    public boolean connect(String ipAddr, int portNo)
    {
        try
        {
            socket = new Socket(ipAddr, portNo);
            opToServer = socket.getOutputStream();
            out = new DataOutputStream(opToServer);
            connected = true;
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * FUNCTION:    send
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Wilson Hu
     *
     * PROGRAMMER:  Wilson Hu
     *
     * INTERFACE:   boolean send(String[] coordinates)
     *
     * RETURNS:     boolean - indicating if the send operation succeeds or fails
     *                      - returns false if the function throws an IOException
     *
     * NOTES:
     * This function is used to send the device's coordinates to the server.
     * It accepts coordinates as a String array where index 0 stores the Lat
     * and index 1 stores the Lng.
     *
     * The unique hashed AndroidID is prepended to the output String, and
     * each element (ID, Lat, Lng) is separated by a '/'.
     */
    public boolean send(String[] coordinates)
    {
        try
        {
            String outStr = android_id.toString() + '/' + coordinates[0] + '/' + coordinates[1];
            out.writeUTF(outStr);
            return true;

        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * FUNCTION:    closeSocket
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Wilson Hu
     *
     * PROGRAMMER:  Wilson Hu
     *
     * INTERFACE:
     *
     * RETURNS:     boolean - indicates if close socket operation succeeds or fails
     *
     * NOTES:
     * This function is used to close the socket when the connection terminates. The
     * app should call this when the user wants to disconnect from the server.
     */
    public boolean closeSocket()
    {
        try
        {
            if(socket != null)
                socket.close();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * FUNCTION:    isConnected
     *
     * DATE:        Apr. 29, 2018
     *
     * REVISIONS:
     *
     * DESIGNER:    Wilson Hu
     *
     * PROGRAMMER:  Wilson Hu
     *
     * INTERFACE:   boolean isConnected()
     *
     * RETURNS:     boolean - indicating if the client app is connected.
     *
     * NOTES:
     * This function returns true if the client app is connected.
     */
    public boolean isConnected()
    {
        return connected;
    }

}
