package server.tcp;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;

public class ClientMngr implements Runnable {
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

	public ClientMngr(Socket socket, AtomicBoolean clientDisconnected) {
		this.socket = socket;
		this.name = (socket.getRemoteSocketAddress().toString()).split("/")[0];
		this.ip = (socket.getRemoteSocketAddress().toString()).split("/")[1];
		this.id = -1;
		this.clientDisconnected = clientDisconnected;
	}

	public int getId() {
		return this.id;
	}

	public boolean isRunning() {
		return this.run;
	}

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
				rawMsg = streamIn.readUTF().toString(); // receive message
				
				// Set client ID
				if (this.id == -1) {
					this.id = Integer.parseInt(rawMsg.split(MSG_DELIMITER, 2)[0]);
					System.out.printf("[Client %s|%tT]: assigned with ID #%s\n", this.ip, time, this.id);
				}

				content = rawMsg.split(MSG_DELIMITER, 2)[1];
				
				time = new Date();
				System.out.printf("[Client #%d|%tT]: \"%s\"\n", this.id, time, content);

				if (content.equals(MSG_DISCONNECT)) {
					break;
				} else if (content.matches("^-{0,1}\\d+.{0,1}\\d+/{1}-{0,1}\\d+.{0,1}\\d+")) {
					// message is a latitude or longitude
					contentSplit = content.split(MSG_DELIMITER, 2);
					lat = Double.parseDouble(contentSplit[0]);
					lng = Double.parseDouble(contentSplit[1]);
					if (!writeToCsv(this.id, this.ip, this.name, this.time, this.lat, this.lng)) {
						System.out.printf("[Client #%d|%tT]: Failed to log the GPS\n", this.id, time);
					}
				}

			} catch (EOFException eEOF) {
				System.out.printf("[Client #%d|%tT]: disconnected\n", this.id, time);
				break;
			} catch (SocketException eSocket) {
				System.out.printf("[Client #%d|%tT]: improper disconnection\n", this.id, time);
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

	public void start() {
		this.run = true;
		time = new Date();
		System.out.printf("[Client %s|%tT]: started\n", this.ip, time);
		if (thrd == null) {
			thrd = new Thread(this);
			thrd.start();
		}
	}

	public void stop() {
		this.run = false;
	}

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
