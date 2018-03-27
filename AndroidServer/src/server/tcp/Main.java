package server.tcp;

public class Main {

	public static void main(String[] args) {
		ConnManager connManager = new ConnManager();
		
		if (!connManager.createSocket(42069))
		{
			return;
		}
		
		if (!connManager.setTimeout(10000)) {
			return;
		}
		
		connManager.start();
	}

}
