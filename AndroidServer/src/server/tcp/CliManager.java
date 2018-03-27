package server.tcp;

import java.net.*;
import java.io.*;

public class CliManager implements Runnable {
	Thread thrd;
	private Socket socket;
	private int id;
	private DataInputStream streamIn;
	private DataOutputStream streamOut;
	private boolean run;

	public CliManager(Socket socket, int id) {
		this.socket = socket;
		this.id = id;
	}

	public void run() {
		// Get input/output stream
		try {
			streamIn = new DataInputStream(socket.getInputStream());
			streamOut = new DataOutputStream(socket.getOutputStream());
			run = true;

		} catch (IOException e) {
			System.out.println("CliManager::run\n" + e.toString());
			run = false;
		}

		while (run) {
			try {
				String tempMsg = streamIn.readUTF();
				System.out.println("[Cli#" + this.id + "]: " + tempMsg);
				streamOut.writeUTF(tempMsg);
			} catch (Exception e) {
				System.out.println("CliManager::run\n" + e.toString());
				break;
			}
		}

		try {
			socket.close();

		} catch (Exception e) {
			System.out.println("CliManager::run\n" + e.toString());
		}
	}

	public void start() {
		System.out.println("Starting client #" + this.id);
		if (thrd == null) {
			thrd = new Thread(this);
			thrd.start();
		}
	}

	public void stop() {
		run = false;
	}
}
