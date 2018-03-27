package server.tcp;

public class Main {
	private static final int PORT_SVR = 42069;
	private static final int TIMEOUT_CONN = 10000;

	public static void main(String[] args) {
		Server server = new Server();

		if (!server.createSocket(PORT_SVR)) {
			return;
		}

		if (!server.setTimeout(TIMEOUT_CONN)) {
			return;
		}

		server.start();
	}
	
	private void inputListener() {
		
	}
}
