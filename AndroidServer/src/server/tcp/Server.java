/*----------------------------------------------------------------------
-- SOURCE FILE:	Server.java		    - File that contains a definition of
--									  the server class to manage multiple
--									  client connections.
--
-- PROGRAM:		AndroidServer
--
-- FUNCTIONS:
--				public Server(void)
--				public Boolean createSocket(int port)
--				public Boolean setTimeout(int millisec)
--				public void run(void)
--				public void start(void)
--				public void stop(void)
--				private void cleanPool(void)
--
-- DATE:		March 30, 2018
--
-- DESIGNER:	Jeremy Lee
--
-- PROGRAMMER:	Jeremy Lee
--
-- NOTES:
-- This is a class definition of the server class to manage multiple
-- client connections.
-- The Server class is for managing client connections and grouping them
-- into a client connection pool. The driver function is meant to be run
-- on a separate thread.
----------------------------------------------------------------------*/
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

	/*------------------------------------------------------------------
	-- FUNCTION:	Server
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public Server(void)
	--
	-- ARGUMENT:    void
	--
	-- RETURNS:	    void
	--
	-- NOTES:
	-- A Server class constructor to instantiate an instance.
	-- Sets values of this instance's member variables.
	------------------------------------------------------------------*/
	public Server() {
		this.random = new Random();
		this.clientPool = new HashMap<Integer, ClientMngr>();
		this.socketTimeout = -1;
		this.clientDisconnected = new AtomicBoolean(false);
	}

	/*------------------------------------------------------------------
	-- FUNCTION:	createSocket
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public Boolean createSocket(int port)
	--
	-- ARGUMENT:    port            	- A port number to accept client
	--									  connections on.
	--
	-- RETURNS:	    Boolean				- True if a socket is successfully
	--									  created. False otherwise.
	--
	-- NOTES:
	-- A wrapper function to create a socket to listen to client
	-- connections on a specified port. This function must be called to
	-- create a socket before running this client instance (the run
	-- function).
	------------------------------------------------------------------*/
	public Boolean createSocket(int port) {
		try {
			this.sockListen = new ServerSocket(port);
		} catch (Exception e) {
			System.out.println("ConnManager::createSocket: " + e.toString());
			return false;
		}
		return true;
	}

	/*------------------------------------------------------------------
	-- FUNCTION:	setTimeout
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public Boolean setTimeout(int millisec)
	--
	-- ARGUMENT:    millisec            - The amount of time to wait
	--									  until a connection request
	--									  from a client is received. In
	--									  milliseconds
	--
	-- RETURNS:	    Boolean				- True is time out is successfully
	--									  set. False otherwise.
	--
	-- NOTES:
	-- A wrapper function to set timeout for the listener socket. Without
	-- this function, the server will not pass the blocking code that
	-- listens to client connections.
	------------------------------------------------------------------*/
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

	/*------------------------------------------------------------------
	-- FUNCTION:	run
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public void run(void)
	--
	-- ARGUMENT:    void
	--
	-- RETURNS:	    void
	--
	-- NOTES:
	-- Driver for the Server class. Keeps on listening to client
	-- connections on the listener socket. Creates and Starts a Client
	-- Manager instance every time it accepts a connection.
	------------------------------------------------------------------*/
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

	/*------------------------------------------------------------------
	-- FUNCTION:	start
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public void start(void)
	--
	-- ARGUMENT:    void
	--
	-- RETURNS:	    void
	--
	-- NOTES:
	-- A function to be called by external methods to start a Server
	-- instance. Starts a client pool cleaner thread as well.
	------------------------------------------------------------------*/
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

	/*------------------------------------------------------------------
	-- FUNCTION:	stop
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public void stop(void)
	--
	-- ARGUMENT:    void
	--
	-- RETURNS:	    void
	--
	-- NOTES:
	-- Sets the flag to stop the Server instance to false.
	------------------------------------------------------------------*/
	public void stop() {
		this.run = false;
	}

	/*------------------------------------------------------------------
	-- FUNCTION:	cleanPool
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	private void cleanPool(void)
	--
	-- ARGUMENT:    void
	--
	-- RETURNS:	    void
	--
	-- NOTES:
	-- Checks if any Client Manager is disconnected. If there is any,
	-- checks the client pool and removes all disconnected Client Manager
	-- instances. The cross-thread boolean variable must be set to true
	-- by a client manager to trigger a pool check.
	------------------------------------------------------------------*/
	private void cleanPool() {
		ClientMngr clientMngr;

		while (this.run) {
			if (this.clientDisconnected.get()) {
				this.clientDisconnected.set(false);
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
