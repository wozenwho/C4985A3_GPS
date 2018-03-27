package server.tcp;

import java.net.*;
import java.util.Date;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import java.io.File;

public class ClientMngr implements Runnable {
	private final String MSG_DISCONNECT = "disconnect";
	private final String MSG_DELIMITER = "/";
	private final String FILE_PATH = "./gps.xml";
	private final String XML_ELEMENT_PARENT = "clients";
	private final String XML_ELEMENT_CHILD = "client";
	private final String XML_ATTR_CHILD_ID = "ID";
	private final String XML_ATTR_CHILD_IP = "IP";
	private final String XML_ATTR_CHILD_NAME = "name";
	private final String XML_ELEMENT_CONTENT = "GPS";
	private final String XML_ATTR_CONTENT_TIME = "time";
	private final String XML_CONTENT_DELIMITER = "/";

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

	public ClientMngr(Socket socket, int id) {
		this.socket = socket;
		this.id = id;
		this.name = (socket.getRemoteSocketAddress().toString()).split("/")[0];
		this.ip = (socket.getRemoteSocketAddress().toString()).split("/")[1];
	}

	public void run() {
		String msg;
		String[] msgSplit;

		try { // Get input/output stream
			streamIn = new DataInputStream(socket.getInputStream());
			streamOut = new DataOutputStream(socket.getOutputStream());
			run = true;
		} catch (IOException e) {
			System.out.println("ClientMngr::run: " + e.toString());
			run = false;
		}

		while (run) {
			try {
				msg = streamIn.readUTF().toString(); // receive message

				time = new Date();
				System.out.printf("[Client #%d|%tT]: %s\n", this.id, time, msg);

				if (msg.equals(MSG_DISCONNECT)) {
					break;
				}

				msgSplit = msg.split(MSG_DELIMITER);
				System.out.printf("%s %s\n", msgSplit[0], msgSplit[1]);
				lat = Double.parseDouble(msgSplit[0]);
				lng = Double.parseDouble(msgSplit[1]);

				writeToXml(this.id, this.name, this.ip, this.time, this.lat, this.lng);
				streamOut.writeUTF(msg); // echo message
			} catch (EOFException eEOF) {
				System.out.printf("Terminating client #%d (%s)\n", this.id, this.ip);
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
	}

	public void start() {
		System.out.printf("Starting client #%d (%s/%s)\n", this.id, this.name, this.ip);
		if (thrd == null) {
			thrd = new Thread(this);
			thrd.start();
		}
	}

	public void stop() {
		run = false;
	}

	public boolean writeToXml(int id, String name, String ip, Date time, double lat, double lng) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();

			// root element
			Element rootElement = doc.createElement(XML_ELEMENT_PARENT);
			doc.appendChild(rootElement);

			// child element
			Element childElement = doc.createElement(XML_ELEMENT_CHILD);
			rootElement.appendChild(childElement);

			// set attribute to element: ID
			Attr attrId = doc.createAttribute(XML_ATTR_CHILD_ID);
			attrId.setValue(String.valueOf(id));
			childElement.setAttributeNode(attrId);

			// set attribute to element: name
			Attr attrName = doc.createAttribute(XML_ATTR_CHILD_NAME);
			attrName.setValue(name);
			childElement.setAttributeNode(attrName);

			// set attribute to element: IP
			Attr attrIp = doc.createAttribute(XML_ATTR_CHILD_IP);
			attrIp.setValue(ip);
			childElement.setAttributeNode(attrIp);

			// GPS element
			Element contentElement = doc.createElement(XML_ELEMENT_CONTENT);
			Attr attrTime = doc.createAttribute(XML_ATTR_CONTENT_TIME);
			attrTime.setValue(String.valueOf(time));
			contentElement.setAttributeNode(attrTime);
			String gps = String.valueOf(lat) + XML_CONTENT_DELIMITER + String.valueOf(lng);
			contentElement.appendChild(doc.createTextNode(gps));
			childElement.appendChild(contentElement);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(FILE_PATH));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);

			// Output to console for testing
			StreamResult consoleResult = new StreamResult(System.out);
			transformer.transform(source, consoleResult);
		} catch (Exception e) {
			System.out.println("ClientMngr::writeToXml: " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
