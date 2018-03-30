package server.tcp;

import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.io.*;

public class Server implements Runnable {
	private final int MAX_NUM_CLI = 10000;
	private final int CLEANER_DELAY = 3000; // milliseconds

	private Thread thrd;
	private Thread thrdCleaner;
	private HashMap<Integer, ClientMngr> clientPool;
	private ServerSocket sockListen;
	private Random random;
	private int socketTimeout;
	private boolean run;
	private volatile AtomicBoolean clientDisconnected;

	public Server() {
		this.random = new Random();
		this.clientPool = new HashMap<Integer, ClientMngr>();
		this.socketTimeout = -1;
		this.clientDisconnected = new AtomicBoolean(false);
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
			System.out.printf(">> Socket timeout set: %d\n", this.socketTimeout);
		} catch (Exception e) {
			System.out.println("ConnManager::setTimeout: " + e.toString());
			return false;
		}
		return true;
	}

	public void run() {
		Socket cliSocket;
		int key;
		ClientMngr clientMngr;

		System.out.printf(">> Listening on port: %d\n", this.sockListen.getLocalPort());

		while (this.run) {
			try {
				if (this.clientPool.size() >= MAX_NUM_CLI) {
					continue;
				}

				cliSocket = this.sockListen.accept(); // Accept connection

				// Generate a hash key
				while (this.clientPool.containsKey(key = this.random.nextInt(MAX_NUM_CLI))) {
				}

				// Create and add client manager for the client
				clientMngr = new ClientMngr(cliSocket, clientDisconnected);
				this.clientPool.put(key, clientMngr);

				// Start the client
				try {
					System.out.printf(">> Connection accepted (%d clients)\n", this.clientPool.size());
					clientMngr.start();
				} catch (Exception e) { // Client failed to start
					System.out.println("ConnManager::run: " + e.toString());
					clientMngr.stop(); // Stop the client manager
					this.clientPool.remove(key); // remove the client manager
				}

			} catch (SocketTimeoutException eSocketTimeout) {
				continue;
			} catch (IOException e) {
				System.out.println("ConnManager::run: " + e.toString());
				break;
			}
		}

		this.run = false;

		// Disconnect all the clients
		for (Map.Entry<Integer, ClientMngr> entry : this.clientPool.entrySet()) {
			entry.getValue().stop();
		}
		this.clientPool.clear(); // Clear connection pool

		System.out.printf(">> Server terminated\n");
	}

	public void start() {
		this.run = true;
		System.out.printf(">> Server started\n");
		if (this.thrdCleaner == null) {
			this.thrdCleaner = new Thread() {
				public void run() {
					cleanPool();
				}
			};
			this.thrdCleaner.start();
		}
		if (this.thrd == null) {
			this.thrd = new Thread(this);
			this.thrd.start();
		}
	}

	public void stop() {
		this.run = false;
	}

	private void cleanPool() {
		ClientMngr clientMngr;

		while (this.run) {
			if (clientDisconnected.get()) {
				clientDisconnected.set(false);
				System.out.printf(">> Removing disconnected clients...\n");

				for (Iterator<Map.Entry<Integer, ClientMngr>> it = this.clientPool.entrySet().iterator(); it
						.hasNext();) {
					clientMngr = it.next().getValue();

					if (!clientMngr.isRunning()) {
						int id = clientMngr.getId();
						it.remove();
						System.out.printf(">> Client removed: #%d.\t(%d remaining)\n", id, this.clientPool.size());
					}
				}

				try {
					Thread.sleep(CLEANER_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
