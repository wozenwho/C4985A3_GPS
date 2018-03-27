package server.tcp;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.io.*;

public class Server implements Runnable {
	private final int MAX_NUM_CLI = 10000;

	private Thread thrd;
	private HashMap<Integer, ClientMngr> clientPool;
	private ServerSocket sockListen;
	private Random random;
	private int socketTimeout;
	private boolean run;

	public Server() {
		random = new Random();
		clientPool = new HashMap<Integer, ClientMngr>();
		socketTimeout = -1;
	}

	public Boolean createSocket(int port) {
		try {
			this.sockListen = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("ConnManager::createSocket: " + e.toString());
			return false;
		}
		return true;
	}

	public Boolean setTimeout(int millisec) {
		try {
			this.sockListen.setSoTimeout(millisec);
			this.socketTimeout = millisec;
			System.out.println("Socket timeout set: " + this.socketTimeout);
		} catch (Exception e) {
			System.out.println("ConnManager::setTimeout: " + e.toString());
			return false;
		}
		return true;
	}

	public void run() {
		Socket cliSocket;
		int id;
		ClientMngr clientMngr;

		run = true;

		System.out.println("Listening on port: " + sockListen.getLocalPort());

		while (run) {
			try {
				if (clientPool.size() >= MAX_NUM_CLI) {
					continue;
				}
				
				cliSocket = sockListen.accept(); // Accept connection

				// Generate client ID
				while (clientPool.containsKey(id = random.nextInt(MAX_NUM_CLI))) {
				}

				// Create and add client manager for the client
				clientMngr = new ClientMngr(cliSocket, id);
				clientPool.put(id, clientMngr);

				// Start the client
				try {
					clientMngr.start();
				} catch (Exception e) { // Client failed to start
					System.out.println("ConnManager::run: " + e.toString());
					clientMngr.stop(); // Stop the client manager
					clientPool.remove(id); // remove the client manager
				}

			} catch (SocketTimeoutException eSocketTimeout) {
				continue;
			} catch (IOException e) {
				System.out.println("ConnManager::run: " + e.toString());
				break;
			}
		}

		// Disconnect all the clients
		for (Map.Entry<Integer, ClientMngr> entry : clientPool.entrySet()) {
			entry.getValue().stop();
		}
		clientPool.clear(); // Clear connection pool
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
