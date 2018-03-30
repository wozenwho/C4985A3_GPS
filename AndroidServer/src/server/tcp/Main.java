package server.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
	private static final double PROGRAM_VERSION = 1.0;
	private static final String PROGRAM_TERMINATION = "q";
	private static final int PORT_SVR = 42069;
	private static final int TIMEOUT_CONN = 3000; // milliseconds

	private static Server server = new Server();

	public static void main(String[] args) {
		System.out.printf("\n");
		System.out.printf("==========================================\n");
		System.out.printf("==== Android Client GPS Server v.%.2f ====\n", PROGRAM_VERSION);
		System.out.printf("==========================================\n");
		System.out.printf("\n");
		System.out.printf(">> Type \"%s\" to interrupt and terminate.\n", PROGRAM_TERMINATION);

		if (!server.createSocket(PORT_SVR)) {
			return;
		}

		if (!server.setTimeout(TIMEOUT_CONN)) {
			return;
		}

		server.start();

		String inputStr;
		BufferedReader input;

		while (true) {
			try {
				// Get console input
				input = new BufferedReader(new InputStreamReader(System.in));
				inputStr = input.readLine();

				if (inputStr.equals(PROGRAM_TERMINATION)) {
					server.stop();
					System.out.printf(">> Terminating server...\n");
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
