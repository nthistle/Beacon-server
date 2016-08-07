import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;
import java.util.*;
import java.lang.*;
import java.lang.Math;
import java.io.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BeaconServer
{
	public static final int PORT_NUMBER = 25565;

	public static void main(String[] args) {
		try {
			BeaconServer myServ = new BeaconServer(PORT_NUMBER);
			myServ.serverMain();
		}
		catch(Exception e) {

		}
	}

	private int myPort;

	public BeaconServer(int port) {
		myPort = port;
		System.out.println("Beacon Server Initiated on Port #" + myPort);
	}

	public void serverMain() throws Exception {
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
		try {
			while(true) {

				Thread.sleep(60000); // every minute
				long curtime = System.nanoTime();
				for(int i = 0; i < allRecentPhotos.size(); i ++) {
					if((curtime - allRecentPhotos.get(i).getTimestamp()) > 3600000000000L) {
						// one hour has passed, take it out
						allRecentPhotos.remove(i);
						System.out.println("Removing photo (id " + allRecentPhotos.get(i).getID() + "), time delay 1 hour hit");
						i --;
					}
				}

				// update photo groups with clustering
				// TODO
			}
		}
		catch(Exception e) {

		}
	}

	// ---------------
	// credit for this method goes to http://www.geodatasource.com/developers/java
	// ---------------
	public static double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		return (dist);
	}
	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	public static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
	// ---------------

	public BufferedImage[] getRecentTenPhotos() {
		BufferedImage[] recent10 = new BufferedImage[10];
		int i = 0;
		for(BufferedImage bufimg : recentTenPhotos) {
			recent10[i++] = bufimg;
		}
		return recent10;
	}

	public ArrayList<Beacon> getBeaconLocations(double radius, double mylat, double mylon) {
		ArrayList<Beacon> inRange = new ArrayList<Beacon>();
		for(Beacon possible : currentBeaconGroups) {
			if(distance(mylat, mylon, possible.getCentroid().lat(), possible.getCentroid().lon()) < radius) {
				inRange.add(possible);
			}
		}
		return inRange;
	}

	public Beacon getBeaconById(int id) {
		for(Beacon possible : currentBeaconGroups) {
			if(possible.getID() == id) {
				return possible;
			}
		}
		return null;
	}

	public void addPhoto(BufferedImage newPhoto, String[] tags, Location origin) {
		try {
			if(recentTenPhotos.size() >= 10) {
				recentTenPhotos.poll();
			}
			recentTenPhotos.add(newPhoto);
			int id = PhotoWrapper.last_ID;
			PhotoWrapper.last_ID ++;
			String filename = "img_" + id + ".png";
			ImageIO.write(newPhoto, "png", new File(filename));
			PhotoWrapper newPhotoWrapper = new PhotoWrapper(filename, tags, System.nanoTime(), id, origin);
			allRecentPhotos.add(newPhotoWrapper);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	// TODO: return events

}

class ConnectionHandler implements Runnable {

	private MainHandler myParent;
	private Socket mySocketClient;
	private int myID;

	public ConnectionHandler(MainHandler parent, Socket myClient, int nID) {
		myParent = parent;
		mySocketClient = myClient;
		myID = nID;
		System.out.println("New connection handler created!");
	}


	public void run() {

		try {
			OutputStream os = mySocketClient.getOutputStream();
			PrintWriter pw = new PrintWriter(os, true);
			BufferedReader br = new BufferedReader(new InputStreamReader(mySocketClient.getInputStream()));

			String request = br.readLine();

			System.out.println("Here is raw request line: " + request);

			String[] params = request.split(" ");
			System.out.println(params);
			int requestType = Integer.parseInt(params[0]);
			System.out.println("Identified request type as " + requestType);
			double lat;
			double lon;
			switch(requestType) {
				case 1:
				  // startup, give us 10 buffered images and ???
				  // technically they gave us location, but we're just sending last 10 soz
				  BufferedImage[] recentTen = myParent.getRecentTenPhotos();
				  for(BufferedImage bufimg : recentTen) {
				  	ImageIO.write(bufimg, "png", os);
				  }

				  break;

				case 2: // case 2, given location and radius, send back all the beacons, their coords, their ids, 
				        // and all their feature images

				  // reading what user sent me
				  lat = Double.parseDouble(params[1]);
				  lon = Double.parseDouble(params[2]);
				  double radius = Double.parseDouble(params[3]);

				  ArrayList<Beacon> nearbyBeacons = myParent.getBeaconLocations(radius, lat, lon);


				  int numNearby = nearbyBeacons.size(); // how many beacons stuff we have to send
				  String responseString = "" + numNearby + " "; // first thing that gets sent, will be followed by
				                                          // by numNearby doubles (lats), another numNearby doubles (longs),
				                                          // another numNearby ints (ids), and then numNearby BufferedImages
				  for(Beacon tmp : nearbyBeacons) {
				  	responseString += tmp.getCentroid().lat() + " "; // append all the latitudes (we have exactly (numNearby) of them )
				  }
				  for(Beacon tmp : nearbyBeacons) {
				  	responseString += tmp.getCentroid().lon() + " "; // longs
				  }
				  for(Beacon tmp : nearbyBeacons) {
				  	responseString += tmp.getID() + " "; // ids
				  }
				  // response string is now done, sending it,
				  pw.println(responseString);
				  
				  // now we send the images (after string has been received and parsed)
				  for(Beacon tmp : nearbyBeacons) {
				  	            // (Buffered Image)   ,  
				  	ImageIO.write(tmp.getFeatureImage(), "png", os);
				  }

				  break;

				case 3:
				  int beaconID = Integer.parseInt(params[1]);
				  Beacon target = myParent.getBeaconById(beaconID);
				  ArrayList<PhotoWrapper> photos = target.getPhotos();
				  String response = "" + photos.size();
				  pw.println(response);
				  for(int i = 0; i < photos.size(); i ++) {
				  	ImageIO.write(photos.get(i).getImage(), "png", os);
				  }
				  break;
				case 5:
				  // guy is sending us stuff, format:
				  // 5 [lat] [lon] [tag1] [tag2] ... [tag3]
				  lat = Double.parseDouble(params[1]);
				  lon = Double.parseDouble(params[2]);
				  System.out.println("Parsed lat and lon as " + lat + ", " + lon);
				  String[] tags = new String[(params.length - 3)];
				  for(int i = 0; i < params.length - 3; i ++) {
				  	tags[i] = params[3+i];
				  }

				  // receive actual image (just one)
				  System.out.println("Now attempting to read image");
				  BufferedImage img = ImageIO.read(mySocketClient.getInputStream());
				  System.out.println("Image read");
				  System.out.println("Now formatting nice photo and calling parent whatchaminiot");
				  // done
				  myParent.addPhoto(img, tags, new Location(lat, lon));

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
			mySocketClient.close();
		}
		catch(IOException ioe) {
			System.out.println("[Handler " + myID + "] could not close connection (???)");
			ioe.printStackTrace();
		}
		System.out.println("[Handler " + myID + "] Connection closed, thread done");

	}
}

/*
 * 
 OutputStream os = clientSocket.getOutputStream();
 BufferedIMage bufimg;
 ImageIO.write(bufimg, "png", os);
 (sender)

 BufferedImage img = ImageIO.read(socket.getInputStream());
 (receiver)


 *
 */




class Beacon {

	public static int last_ID = 1000;
	// doesn't matter if intersects with photowrapper id values, just distinguishing for debugging

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

	public int getID() {
		return myID;
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

	public static int last_ID = 1;
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
		return myOrigin;
	}

	public long getTimestamp() {
		return myTimeCreated;
	}

	public BufferedImage getImage() {
		try {
			return ImageIO.read(new File(myFilename));
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
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