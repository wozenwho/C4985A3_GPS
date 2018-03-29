package server.tcp;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;

import com.opencsv.CSVWriter;

public class ClientMngr implements Runnable {
	private final String FILE_DATE_FORMAT = "yyMMdd";
	private final String FILE_EXTENSION = ".csv";
	private final String FILE_PATH_DIR = "./data_gps/";
	private final String CSV_TIME_FORMAT = "yyyyMMdd HH:mm:ss";
	private final String CSV_HEADER_ID = "Client ID";
	private final String CSV_HEADER_IP = "IP";
	private final String CSV_HEADER_NAME = "Name";
	private final String CSV_HEADER_TIME = "Time";
	private final String CSV_HEADER_LAT = "Latitude";
	private final String CSV_HEADER_LNG = "Longitude";
	private final String MSG_DISCONNECT = "sayonara";
	private final String MSG_REQ_ID = "mydigits";
	private final String MSG_DELIMITER = "/";

	private Thread thrd;
	private Socket socket;
	private int id;
	private String name;
	private String ip;
	private DataInputStream streamIn;
	private DataOutputStream streamOut;
	private boolean run;
	private Date time;
	private double lat;
	private double lng;
	private volatile AtomicBoolean clientDisconnected;

	public ClientMngr(Socket socket, int id, AtomicBoolean clientDisconnected) {
		this.socket = socket;
		this.id = id;
		this.name = (socket.getRemoteSocketAddress().toString()).split("/")[0];
		this.ip = (socket.getRemoteSocketAddress().toString()).split("/")[1];
		this.clientDisconnected = clientDisconnected;
	}

	public int getId() {
		return this.id;
	}
	
	public boolean isRunning() {
		return this.run;
	}
	
	public void run() {
		String msg;
		String[] msgSplit;

		try { // Get input/output stream
			streamIn = new DataInputStream(socket.getInputStream());
			streamOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("ClientMngr::run: " + e.toString());
			this.run = false;
		}

		while (this.run) {
			try {
				msg = streamIn.readUTF().toString(); // receive message

				time = new Date();
				System.out.printf("[Client #%d|%tT]: \"%s\"\n", this.id, time, msg);

				if (msg.equals(MSG_DISCONNECT)) {
					break;
				} else if (msg.equals(MSG_REQ_ID)) {
					streamOut.writeUTF(String.valueOf(this.id)); // send the client ID
				} else if (msg.matches("^-{0,1}\\d+.{0,1}\\d+/{1}-{0,1}\\d+.{0,1}\\d+")) {
					// message is a latitude or longitude
					msgSplit = msg.split(MSG_DELIMITER);
					lat = Double.parseDouble(msgSplit[0]);
					lng = Double.parseDouble(msgSplit[1]);
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
		System.out.printf("[Client #%d|%tT]: started\n", this.id, time);
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
		CSVWriter csvWriter;
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
				csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
				record = new String[] { CSV_HEADER_ID, CSV_HEADER_IP, CSV_HEADER_NAME, CSV_HEADER_TIME, CSV_HEADER_LAT,
						CSV_HEADER_LNG };
				csvWriter.writeNext(record);
				csvWriter.close();
			}

			// Format time stamp of received message
			df = new SimpleDateFormat(CSV_TIME_FORMAT);
			String timeFormatted = df.format(time);

			// Append record
			fileWriter = new FileWriter(filePathFull, true);
			csvWriter = new CSVWriter(fileWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
			record = new String[] { String.valueOf(id), ip, name, timeFormatted, String.valueOf(lat),
					String.valueOf(lng) };
			csvWriter.writeNext(record);

			csvWriter.close();
		} catch (Exception e) {
			System.out.println("ClientMngr::writeToCsv: " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
