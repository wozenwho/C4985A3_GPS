package server.tcp;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.io.*;

public class ConnManager implements Runnable {
	private final int MAX_NUM_CLI = 10000;

	private Thread thrd;
	private Random random;
	private ServerSocket sockListen;
	private HashMap<Integer, CliManager> clientPool;
	private int socketTimeout;
	private boolean run;

	public ConnManager() {
		random = new Random();
		clientPool = new HashMap<Integer, CliManager>();
		socketTimeout = -1;
	}

	public void run() {
		int tempCliId;
		Socket cliSocket;
		CliManager cliManager;

		run = true;

		System.out.println("Listening on port: " + sockListen.getLocalPort());

		while (run) {
			try {
				if (clientPool.size() >= MAX_NUM_CLI) {
					continue;
				}

				// Accept connection
				cliSocket = sockListen.accept();
				System.out.println("Connection from: " + cliSocket.getRemoteSocketAddress());

				// Get the client string
//				DataInputStream in = new DataInputStream(cliSocket.getInputStream());
//				System.out.println(in.readUTF());

				// Generate client ID
				while (clientPool.containsKey(tempCliId = random.nextInt(MAX_NUM_CLI))) {
				}
				System.out.println("Client ID:" + tempCliId);

				// Create and add client manager for the client
				cliManager = new CliManager(cliSocket, tempCliId);
				clientPool.put(tempCliId, cliManager);

				// Start the client
				try {
					cliManager.start();
				} catch (Exception e) { // Client failed to start
					System.out.println("ConnManager::run\n" + e.toString());
					cliManager.stop(); // Stop the client manager
					clientPool.remove(tempCliId); // remove the client manager
				}

			} catch (SocketTimeoutException eSocketTimeout) {
				System.out.println("Socket timed out (" + socketTimeout + " ms)");
				continue;
			} catch (IOException e) {
				System.out.println("ConnManager::run\n" + e.toString());
				break;
			}
		}

		// Disconnect all the clients
		for(Map.Entry<Integer, CliManager> entry : clientPool.entrySet()) {
		    entry.getValue().stop();
		}
		clientPool.clear(); // Clear connection pool
	}

	public Boolean createSocket(int port) {
		try {
			this.sockListen = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("ConnManager::createSocket\n" + e.toString());
			return false;
		}
		return true;
	}

	public Boolean setTimeout(int millisec) {
		try {
			this.sockListen.setSoTimeout(millisec);
			this.socketTimeout = millisec;
			System.out.println("Socket timeout set:" + this.socketTimeout);
		} catch (Exception e) {
			System.out.println("ConnManager::setTimeout\n" + e.toString());
			return false;
		}
		return true;
	}

	public void start() {
		System.out.println("Starting server");
		if (thrd == null) {
			thrd = new Thread(this);
			thrd.start();
		}
	}

	public void stop() {
		run = false;
	}
}
