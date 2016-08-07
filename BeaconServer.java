import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;
import javax.imageio.ImageIO;


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

class MainHandler implements Runnable {

	private ArrayList<PhotoWrapper> allRecentPhotos;
	private ArrayList<Beacon> currentBeaconGroups;
	private ConcurrentLinkedQueue<BufferedImage> recentTenPhotos;

	public MainHandler() {
		System.out.println("[Main Handler] instantiated");
		allRecentPhotos = new ArrayList<PhotoWrapper>();
		currentBeaconGroups = new ArrayList<Beacon>();
		recentTenPhotos = new ConcurrentLinkedQueue<BufferedImage>();
	}

	public void run() {
		while(true) {

			Thread.sleep(60000); // every minute
			long curtime = System.nanoTime();
			for(int i = 0; i < allRecentPhotos.size(); i ++) {
				if((curtime - allRecentPhotos.get(i).getTimestamp()) > 3600000000000) {
					// one hour has passed, take it out
					allRecentPhotos.remove(i);
					System.out.println("Removing photo (id " + allRecentPhotos.getID() + "), time delay 1 hour hit");
					i --;
				}
			}

			// update photo groups with clustering
			// 
		}
	}

	public BufferedImage[] getRecentTenPhotos() {
		BufferedImage[] recent10 = new BufferedImage[10];
		int i = 0;
		for(BufferedImage bufimg : recentTenPhotos) {
			recent10[i++] = bufimg;
		}
		return recent10;
	}

}

class ConnectionHandler implements Runnable {

	private MainHandler myParent;
	private Socket mySocketClient;
	private int myID;

	public ConnectionHandler(MainHandler parent, Socket myClient, int nID) {
		myParent = parent;
		mySocketClient = myClient;
		myID = nID;
	}


	public void run() {

		try {
			OutputStream os = clientSocket.getOutputStream();
			PrintWriter pw = new PrintWriter(os, true);
			BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			String request = br.readLine();

			String[] params = request.split(" ");
			int requestType = Integer.parseInt(params[0]);

			switch(requestType) {
				case 1:

				  break;
				case 2:

				  break;
				case 3:

				  break;
				case 4:

				  break;
				default:
				  break;
			}

			// pw.println("xd xd lmfao");
		}
		//catch(Exception e) {
		//}
		catch(IOException ioe) {
			System.out.println("Something went wrong, Handler " + myID);
			ioe.printStackTrace();
		}

		try {
			clientSocket.close();
		}
		catch(IOException ioe) {
			System.out.println("[Handler " + myID + "] could not close connection (???)");
			ioe.printStackTrace();
		}
		System.out.println("[Handler " + myID + "] Connection closed, thread done");




	}
}

class Beacon {

	private ArrayList<PhotoWrapper> myPhotos;
	private int myID;
	private Location myCentroid;
	private BufferedImage myFeatureImage;

	public Beacon(ArrayList<PhotoWrapper> photos, int id, Location centroid, BufferedImage featureImage) {
		myPhotos = photos;
		myID = id;
		myCentroid = centroid;
		myFeatureImage = featureImage;
	}

	public ArrayList<PhotoWrapper> getPhotos() {
		return myPhotos;
	}

	public Location getCentroid() {
		return myCentroid;
	}

	public BufferedImage getFeatureImage() {
		return myFeatureImage;
	}
}

class PhotoWrapper {

	private int myID;
	private String myFilename;
	private Location myOrigin;
	private String[] myTags;
	private long myTimeCreated;

	public PhotoWrapper(String filename, String[] tags, long timeCreated, int id, Location origin) {
		myFilename = filename;
		myID = id;
		myOrigin = origin;
		myTags = tags;
		myTimeCreated = timeCreated;
	}

	public String getFilename() {
		return myFilename;
	}

	public int getID() {
		return myID;
	}

	public Location getOrigin() {
		return myOrigin
	}

	public long getTimestamp() {
		return timeCreated;
	}

	public BufferedImage getImage() {
		return ImageIO.read(new File(myFilename));
	}
}



class Location {

	private double lat;
	private double lon;

	public Location(double latitude, double longitude) {
		lat = latitude;
		lon = longitude;
	}

	public double lat() {
		return lat;
	}

	public double lon() {
		return lon;
	}
}