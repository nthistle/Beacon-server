import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;


public class BeaconServer 
{
	public static final int PORT_NUMBER = 25565;

	public static void main(String[] args) {
		try {
			BeaconServer myServ = new BeaconServer(PORT_NUMBER);
			myServ.serverMain();
		}
	}

	private int myPort;

	public BeaconServer(int port) {
		myPort = port;
		System.out.println("Beacon Server Initiated on Port #" + myPort);
	}

	public void serverMain() {
		System.out.println("[Beacon Server] starting main");
		ServerSocket serverSocket;
		Socket curSocket;
		ConnectionHandler curHandler;

		serverSocket = new ServerSocket(myPort);

		MainHandler mainHandler = new MainHandler();
		new Thread(mainHandler).start();

		System.out.println("[Beacon Server] main handler created");

		int curConnID = 1;
		while(true) {
			System.out.println("[ChatRoomServer] Listening, next connection ID = " + curConnID);

			curSocket = serverSocket.accept();  // blocks

			System.out.println("[ChatRoomServer] Received connection, ID = " + curConnID);
			System.out.println("[ChatRoomServer] IP Address: " + curSocket.getRemoteSocketAddress().toString());

			curHandler = new ConnectionHandler(mainHandler, curSocket, curConnID);
			new Thread(curHandler).start();
			

			curConnID ++;

			Thread.sleep(25);
		}

	}




}