/*----------------------------------------------------------------------
-- SOURCE FILE:	Main.java		    - File that contains an external
--									  main driver of the program.
--
-- PROGRAM:		AndroidServer
--
-- FUNCTIONS:
--				public static void main(String[] args)
--
-- DATE:		March 30, 2018
--
-- DESIGNER:	Jeremy Lee
--
-- PROGRAMMER:	Jeremy Lee
--
-- NOTES:
-- Contains the external driver of the program.
-- Main method is the entry point of the program. The Server and the
-- ClientMngr classes are used as core engines.
-- The program starts a server to listen to client connections and a
-- client manager is assigned each connection to receive data from
-- corresponding client.
----------------------------------------------------------------------*/
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

	/*------------------------------------------------------------------
	-- FUNCTION:	main
	--
	-- DATE:		March 30, 2018
	--
	-- DESIGNER:	Jeremy Lee
	--
	-- PROGRAMMER:	Jeremy Lee
	--
	-- INTERFACE:	public static void main(String[] args)
	--
	-- ARGUMENT:    args				- Command line argument. Not
	--									  used for this program.
	--
	-- RETURNS:	    void
	--
	-- NOTES:
	-- The entry point of the program.
	-- Creates and sets up a connection listener socket, and then starts
	-- a Server class instance as well as listens to the user input to
	-- stop the Server instance.
	-- The Server instance creates and starts a client manager instance
	-- once a client connects to it.
	-- Note that without setting a time out for the listener socket,
	-- the program will not be able to terminate properly even if a user
	-- enters the termination command.
	------------------------------------------------------------------*/
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
