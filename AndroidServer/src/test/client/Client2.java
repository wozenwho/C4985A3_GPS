package test.client;

import java.net.*;
import java.util.Random;

import javax.swing.plaf.SliderUI;

import java.io.*;

public class Client2 {

	public static void main(String[] args) {
		String ClientString;

		if (args.length != 2) {
			System.out.println("Usage Error : java jclient <host> <port>");
			System.exit(0);
		}
		String serverName = args[0];
		int port = Integer.parseInt(args[1]);

		try {
			// Connect to the server
			System.out.println("Connecting to " + serverName + " on port " + port);
			Socket client = new Socket(serverName, port); // create socket
			System.out.println("Successful connection to: " + client.getRemoteSocketAddress());
			Random r = new Random();

			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			int id = r.nextInt(1000000);

			int i = 1;
			while (i++ < 15) {
				// Get console input
				// ClientString = input.readLine();
				// if (ClientString.equals("sayonara")) {
				// client.close();
				// break;
				// }

				// ClientString = String.valueOf((new Random()).nextInt(1000));
				// ClientString += "/" + "122." + i;
				// ClientString += "/" + "122." + i++;

				// Send client string to server
				ClientString = id + "/" + "49." + r.nextInt(1000000) + "/-123." + r.nextInt(1000000);
				out.writeUTF(ClientString);

				// if (ClientString.equals("mydigits")) {
				// // Get the echo from server
				// InputStream inFromServer = client.getInputStream();
				// DataInputStream in = new DataInputStream(inFromServer);
				// System.out.println("Server Echo: " + in.readUTF());
				// }

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			client.close();
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

}