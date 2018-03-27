package test.client;

import java.net.*;
import java.io.*;

public class Client {

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

			while (true) {
				// Get console input
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
				ClientString = input.readLine();
				if (ClientString.equals("disconnect")) {
					client.close();
					break;
				}

				// Send client string to server
				OutputStream outToServer = client.getOutputStream();
				DataOutputStream out = new DataOutputStream(outToServer);
				out.writeUTF(ClientString + client.getLocalSocketAddress());
				InputStream inFromServer = client.getInputStream();

				// Get the echo from server
				DataInputStream in = new DataInputStream(inFromServer);
				System.out.println("Server Echo: " + in.readUTF());
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
