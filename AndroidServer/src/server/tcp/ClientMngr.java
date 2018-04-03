/*----------------------------------------------------------------------
-- SOURCE FILE:	ClientMngr.java	    - File that contains a definition of
--									  the client manager class to manage
--									  client I/O.
--
-- PROGRAM:		AndroidServer
--
-- FUNCTIONS:
--				public ClientMngr(Socket socket, AtomicBoolean
--								  clientDisconnected)
--				public int getId(void)
--				public boolean isRunning(void)
--				public void run(void)
--				public void start(void)
--				public void stop(void)
--				private synchronized boolean writeToCsv(int id, String ip,
--				String name, Date time, double lat, double lng)
--
-- DATE:		March 30, 2018
--
-- DESIGNER:	Jeremy Lee
--
-- PROGRAMMER:	Jeremy Lee
--
-- NOTES:
-- This is a class definition for client I/O management.
-- Contains the client manager class. The client manager class is for
-- managing incoming data from a client, sending data to it, and processing
-- received data (writing to a file). The driver function is meant to be
-- run on a separate thread.
----------------------------------------------------------------------*/
package server.tcp;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;

public class ClientMngr implements Runnable {
	private final int TIMEOUT_RECV = 100; // milliseconds
	private final int TIMEOUT_SOCKET = 60000; // milliseconds
	private final String FILE_DATE_FORMAT = "yyMMdd";
	private final String FILE_EXTENSION = ".csv";
	private final String FILE_PATH_DIR = "./data_gps/";
	private final String CSV_DELIMITER = ",";
	private final String CSV_LINEEND = "\r\n";
	private final String CSV_TIME_FORMAT = "yyyyMMdd HH:mm:ss";
	private final String CSV_HEADER_ID = "Client ID";
	private final String CSV_HEADER_IP = "IP";
	private final String CSV_HEADER_NAME = "Name";
	private final String CSV_HEADER_TIME = "Time";
	private final String CSV_HEADER_LAT = "Latitude";
	private final String CSV_HEADER_LNG = "Longitude";
	private final String MSG_DISCONNECT = "sayonara";
	private final String MSG_DELIMITER = "/";

	private Thread thrd;
	private Socket socket;
	private String name;
	private String ip;
	private int id;
	private DataInputStream streamIn;
	private boolean run;
	private Date time;
	private double lat;
	private double lng;
	private volatile AtomicBoolean clientDisconnected;

	/*------------------------------------------------------------------
	-- FUNCTION:	ClientMngr
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public ClientMngr(Socket socket, AtomicBoolean
	--								  clientDisconnected)
	--
	-- ARGUMENT:    socket            	- A socket on which the client is
	--									  connected to the server (a Server
	--									  class instance).
	--              clientDisconnected  - A cross-thread boolean reference
	--									  to indicate this instance's
	--									  disconnection.
	--
	-- RETURNS:	    void
	--
	-- NOTES:
	-- A Client Manager class constructor to instantiate an instance.
	-- Sets values of this instance's member variables.
	------------------------------------------------------------------*/
	public ClientMngr(Socket socket, AtomicBoolean clientDisconnected) {
		this.socket = socket;
		this.name = (socket.getRemoteSocketAddress().toString()).split("/")[0];
		this.ip = (socket.getRemoteSocketAddress().toString()).split("/")[1];
		this.id = -1;
		this.clientDisconnected = clientDisconnected;
	}

	/*------------------------------------------------------------------
	-- FUNCTION:	getId
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public int getId(void)
	--
	-- ARGUMENT:    void
	--
	-- RETURNS:	    int					- This client manager's ID.
	--
	-- NOTES:
	-- Gets the client ID of this Client Manager instance.
	------------------------------------------------------------------*/
	public int getId() {
		return this.id;
	}

	/*------------------------------------------------------------------
	-- FUNCTION:	isRunning
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public boolean isRunning(void)
	--
	-- ARGUMENT:    void
	--
	-- RETURNS:	    boolean				- True if this client manager's
	--									  run function is running. False
	--									  otherwise.
	--
	-- NOTES:
	-- Gets the value of the flag (boolean) to stop this Client Manager
	-- instance.
	------------------------------------------------------------------*/
	public boolean isRunning() {
		return this.run;
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
	-- Driver for the Client Manager class. Keeps on receiving client
	-- data on the client socket.
	-- This function is meant to be run concurrently on a separate thread
	-- for continuous data reception from a client.
	------------------------------------------------------------------*/
	public void run() {
		String rawMsg;
		String content;
		String[] contentSplit;

		try { // Get input/output stream
			streamIn = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("ClientMngr::run: " + e.toString());
			this.run = false;
		}

		while (this.run) {
			try {
				if (streamIn.available() > 0) {
					rawMsg = streamIn.readUTF().toString(); // receive message
				} else if (((new Date()).getTime() - this.time.getTime()) > TIMEOUT_SOCKET) {
					break;
				} else {
					Thread.sleep(TIMEOUT_RECV);
					continue;
				}

				// Set client ID
				if (this.id == -1) {
					this.id = Integer.parseInt(rawMsg.split(MSG_DELIMITER, 2)[0]);
					System.out.printf("[Client %s|%tT]: assigned with ID #%s\n", this.ip, this.time, this.id);
				}

				content = rawMsg.split(MSG_DELIMITER, 2)[1];

				System.out.printf("[Client #%d|%tT]: \"%s\"\n", this.id, new Date(), content);

				if (content.equals(MSG_DISCONNECT)) {
					break;
				} else if (content.matches("^-{0,1}\\d+.{0,1}\\d+/{1}-{0,1}\\d+.{0,1}\\d+")) {
					// message is a latitude or longitude
					this.time = new Date();
					contentSplit = content.split(MSG_DELIMITER, 2);
					lat = Double.parseDouble(contentSplit[0]);
					lng = Double.parseDouble(contentSplit[1]);
					if (!writeToCsv(this.id, this.ip, this.name, this.time, this.lat, this.lng)) {
						System.out.printf("[Client #%d|%tT]: Failed to log the GPS\n", this.id, this.time);
					}
				}

			} catch (EOFException eEOF) {
				System.out.printf("[Client #%d|%tT]: disconnected\n", this.id, this.time);
				break;
			} catch (SocketException eSocket) {
				System.out.printf("[Client #%d|%tT]: improper disconnection\n", this.id, this.time);
				break;
			} catch (Exception e) {
				System.out.println("ClientMngr::run: " + e.toString());
				e.printStackTrace();
				break;
			}
		}

		try {
			socket.close();
		} catch (Exception e) {
			System.out.println("ClientMngr::run: " + e.toString());
			e.printStackTrace();
		}

		this.run = false;
		this.clientDisconnected.set(true);
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
	-- A function to be called by a Server instance to start this Client
	-- Manager instance. Invokes the run function on a separate thread.
	------------------------------------------------------------------*/
	public void start() {
		this.run = true;
		this.time = new Date();
		System.out.printf("[Client %s|%tT]: started\n", this.ip, this.time);
		if (thrd == null) {
			thrd = new Thread(this);
			thrd.start();
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
	-- Sets the flag to stop this Client Manager instance to false.
	-- Call this function to stop a client manager instance.
	------------------------------------------------------------------*/
	public void stop() {
		this.run = false;
	}

	/*------------------------------------------------------------------
	-- FUNCTION:	writeToCsv
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	private synchronized boolean writeToCsv(int id,
	--				String ip, String name, Date time, double lat, double
	--				lng)
	--
	-- ARGUMENT:    id            		- ID of a Client Manager instance.
	--				ip            		- IP address of a client.
	--				name            	- Name of a client device.
	--				time            	- Time stamp of data reception.
	--				lat            		- Latitude in received GPS data.
	--				lng            		- Longitude in received GPS data.
	--
	-- RETURNS:	    boolean				- True if the input is written
	--									  on the file successfully. False
	--									  otherwise.
	--
	-- NOTES:
	-- Writes the given information (arguments) to a file in CSV format.
	-- Note that the CSV file is not supposed to be opened or this function
	-- will fail to write to that file and return false.
	------------------------------------------------------------------*/
	private synchronized boolean writeToCsv(int id, String ip, String name, Date time, double lat, double lng) {
		DateFormat df;
		Date today;
		String filePathFull;
		FileWriter fileWriter;
		File dir;
		File file;
		String[] record;

		// Get and format today's date
		df = new SimpleDateFormat(FILE_DATE_FORMAT);
		today = Calendar.getInstance().getTime();
		filePathFull = FILE_PATH_DIR + df.format(today) + FILE_EXTENSION;

		try {
			dir = new File(FILE_PATH_DIR);
			// Creates directory if it doesn't exist
			if (!dir.exists()) {
				dir.mkdir();
			}

			file = new File(filePathFull);
			// Creates file and add header if the file doesn't exist
			if (!(file.exists() && !file.isDirectory())) {
				file.createNewFile();
				fileWriter = new FileWriter(filePathFull, true);
				record = new String[] { CSV_HEADER_ID, CSV_HEADER_IP, CSV_HEADER_NAME, CSV_HEADER_TIME, CSV_HEADER_LAT,
						CSV_HEADER_LNG };

				// Append headers
				for (int i = 0; i < record.length; i++) {
					fileWriter.append(record[i]);
					fileWriter.append((i == record.length - 1) ? CSV_LINEEND : CSV_DELIMITER);
				}
				fileWriter.close();
			}

			// Format time stamp of received message
			df = new SimpleDateFormat(CSV_TIME_FORMAT);
			String timeFormatted = df.format(time);

			// Append record
			fileWriter = new FileWriter(filePathFull, true);
			record = new String[] { String.valueOf(id), ip, name, timeFormatted, String.valueOf(lat),
					String.valueOf(lng) };
			for (int i = 0; i < record.length; i++) {
				fileWriter.append(record[i]);
				fileWriter.append((i == record.length - 1) ? CSV_LINEEND : CSV_DELIMITER);
			}
			fileWriter.close();

		} catch (Exception e) {
			System.out.println("ClientMngr::writeToCsv: " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
