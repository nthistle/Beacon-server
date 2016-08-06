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


/*
BUG LIST:

"shrucis1 has been upgraded to rank mod"
- 38 characters, tried to put it all on one line, (mo, broken, d)

when kicked, user does not receive disconnect message

*/
public class ChatRoomServer {

	public static final int portNumber = 1337;
	//public static final int portNumber = 2302;

	private int myPort;

	public static void main(String args[]) throws IOException, Exception {
		ChatRoomServer myServ = new ChatRoomServer(portNumber);
		myServ.serverMain();
	}

	public ChatRoomServer(int port) {
		myPort = port;
		System.out.println("ChatRoomServer instantiated w/ port " + port);
	}

	public void serverMain() throws IOException, Exception {
		System.out.println("[ChatRoomServer] Creating server socket on port " + myPort);
		ServerSocket serverSocket;
		Socket curSocket;
		ChatConnectionHandler curCCH;

		serverSocket = new ServerSocket(myPort);

		ChatRoomHandler chatroom = new ChatRoomHandler();
		new Thread(chatroom).start();

		System.out.println("[ChatRoomServer] chatroom handler created");

		int curConnID = 1;
		while(true) {
			//System.out.println("[ChatRoomServer] Listening for connection");
			//System.out.println("[ChatRoomServer] Next connection ID = " + curConnID);
			System.out.println("[ChatRoomServer] Listening, next connection ID = " + curConnID);

			curSocket = serverSocket.accept();  // blocks

			System.out.println("[ChatRoomServer] Received connection, ID = " + curConnID);
			System.out.println("[ChatRoomServer] IP Address: " + curSocket.getRemoteSocketAddress().toString());

			curCCH = new ChatConnectionHandler(chatroom, curSocket, curConnID);
			new Thread(curCCH).start();

			chatroom.addClient(curCCH);

			System.out.println("[ChatRoomServer] Added connID " + curConnID + " to chatroom");

			curConnID ++;

			Thread.sleep(25);
		}

		/*System.out.println("[ChatRoomServer] Waiting for connection 1...");
		curSocket = serverSocket.accept();
		System.out.println("[ChatRoomServer] connection 1 received");
		ChatConnectionHandler connection1 = new ChatConnectionHandler(this, curSocket, 1);
		new Thread(connection1).start();
		System.out.println("[ChatRoomServer] started Handler(connection 1) thread");

		//serverSocket = new ServerSocket(portNumber);
		System.out.println("[ChatRoomServer] Waiting for connection 2...");
		curSocket = serverSocket.accept();
		System.out.println("[ChatRoomServer] connection 2 received");
		ChatConnectionHandler connection2 = new ChatConnectionHandler(this, curSocket, 2);
		new Thread(connection2).start();
		System.out.println("[ChatRoomServer] started Handler(connection 2) thread");
		while(true) {
			if(conn1msg != null) {
				connection2.setMessage(conn1msg);
				conn1msg = null;
			}
			else if(conn2msg != null) {
				connection1.setMessage(conn2msg);
				conn2msg = null;
			}
			System.out.println("[Main] main thread stalling...");
			Thread.sleep(1000);
		}*/
	}

	/*
	public void setConnMsg(int id, String nmsg) {
		if(id == 1) {
			conn1msg = nmsg;
		}
		else if(id == 2) {
			conn2msg = nmsg;
		}
	}
	*/
}

class ChatRoomHandler implements Runnable {

	private ConcurrentLinkedQueue<ChatItem> receivedMessages;
	private List<ChatConnectionHandler> connectedClients;
	private List<ChatConnectionHandler> moderators;

	//// SHAKESPEAREAN INSULT STUFF ////
	private final Random rand = new Random();
	private final String[] insult1 = new String[] {"artless", "bawdy", "beslubbering", "bootless", "churlish", "cockered", "clouted", "craven", "currish", "dankish", "dissembling", "droning", "errant", "fawning", "fobbing", "froward", "frothy", "gleeking", "goatish", "gorbellied", "impertinent", "infectious", "jarring", "loggerheaded", "lumpish", "mammering", "mangled", "mewling", "paunchy", "pribbling", "puking", "puny", "qualling", "rank", "reeky", "roguish", "ruttish", "saucy", "spleeny", "spongy", "surly", "tottering", "unmuzzled", "vain", "venomed", "villainous", "warped", "wayward", "weedy", "yeasty"};
	private final String[] insult2 = new String[] {"base-court", "bat-fowling", "beef-witted", "beetle-headed", "boil-brained", "clapper-clawed", "clay-brained", "common-kissing", "crook-pated", "dismal-dreaming", "dizzy-eyed", "doghearted", "dread-bolted", "earth-vexing", "elf-skinned", "fat-kidneyed", "fen-sucked", "flap-mouthed", "fly-bitten", "folly-fallen", "fool-born", "full-gorged", "guts-griping", "half-faced", "hasty-witted", "hedge-born", "hell-hated", "idle-headed", "ill-breeding", "ill-nurtured", "knotty-pated", "milk-livered", "motley-minded", "onion-eyed", "plume-plucked", "pottle-deep", "pox-marked", "reeling-ripe", "rough-hewn", "rude-growing", "rump-fed", "shard-borne", "sheep-biting", "spur-galled", "swag-bellied", "tardy-gaited", "tickle-brained", "toad-spotted", "unchin-snouted", "weather-bitten"};
	private final String[] insult3 = new String[] {"apple-john", "baggage", "barnacle", "bladder", "boar-pig", "bugbear", "bum-bailey", "canker-blossom", "clack-dish", "clotpole", "coxcomb", "codpiece", "death-token", "dewberry", "flap-dragon", "flax-wench", "flirt-gill", "foot-licker", "fustilarian", "giglet", "gudgeon", "haggard", "harpy", "hedge-pig", "horn-beast", "hugger-mugger", "joithead", "lewdster", "lout", "maggot-pie", "malt-worm", "mammet", "measle", "minnow", "miscreant", "moldwarp", "mumble-news", "nut-hook", "pigeon-egg", "pignut", "puttock", "pumpion", "ratsbane", "scut", "skainsmate", "strumpet", "varlot", "vassal", "whey-face", "wagtail"};
	//// END SHAKESPEARE ////


	public ChatRoomHandler() {
		System.out.println("[ChatRoomHandler] Instantiated");
		receivedMessages = new ConcurrentLinkedQueue<ChatItem>();
		connectedClients = Collections.synchronizedList(new ArrayList<ChatConnectionHandler>());
		moderators = Collections.synchronizedList(new ArrayList<ChatConnectionHandler>());
	}

	public void run() {
		while(true) {
			try {
				if(!receivedMessages.isEmpty()) {
					ChatItem toSend = receivedMessages.poll();
					for(ChatConnectionHandler curClient : connectedClients) {
						if(curClient.getID() != toSend.getSenderID()) {
							curClient.giveMessage(toSend.getMessage());
						} 
					}
				}
				Thread.sleep(50);
			} catch(Exception e) {
				System.out.println("Something went wrong");
				e.printStackTrace();
			}
		}
	}

	public void addClient(ChatConnectionHandler newClient) {
		System.out.println("[ChatRoomHandler] received new client");
		connectedClients.add(newClient);
	}

	private void addMessage(ChatItem ci) {
		receivedMessages.offer(ci);
		System.out.println(ci.getMessage());
	}

	public void receiveMessage(RawMessage r) {
		ChatConnectionHandler sender = r.getSender();
		int senderID = sender.getID();
		String msg = r.getMessage();
		String[] cmdParams = msg.split(" ");
		String cmd = cmdParams[0];
		if(cmd.equalsIgnoreCase("m")) {
			addMessage(new ChatItem(senderID, sender.getHandle() + ": " + msg.substring(2)));
		}
		else if(cmd.equalsIgnoreCase("join")) {
			if(cmdParams.length >= 2) {
				if(getByHandle(cmdParams[1]) == null) {
					sender.setHandle(cmdParams[1]);
					addMessage(new ChatItem(-1, cmdParams[1] + " has joined"));
				}
				else {
					sender.giveMessage("Someone already has that handle!");
					disconnectClient(sender);
				}
			}
		}
		else if(cmd.equalsIgnoreCase("ping")) {
			sender.giveMessage("pong!");
		}
		else if(cmd.equalsIgnoreCase("who")) {
			String response = "\nCurrently Online:\n";

			for(ChatConnectionHandler curClient : connectedClients) {
				if(moderators.contains(curClient)) {
					response += " - " + curClient.getHandle() + " (MOD)\n";
				}
				else {
					response += " - " + curClient.getHandle() + "\n";
				}
			}
			sender.giveMessage(response);
			//sender.giveMessage("sorry, not implemented yet");
		}
		else if(cmd.equalsIgnoreCase("me")) {
			if(cmdParams.length > 1)
				addMessage(new ChatItem(-1, " * " + sender.getHandle() + " " + msg.substring(3)));
			//todo: proper usage
		}
		else if(cmd.equalsIgnoreCase("nick")) {
			if(cmdParams.length > 1) {
				String newnick = cmdParams[1];
				if(getByHandle(newnick) == null) {
					addMessage(new ChatItem(-1, sender.getHandle() + " is now known as " + cmdParams[1]));
					sender.setHandle(cmdParams[1]);
				}
				else {
					sender.giveMessage("Someone already has that handle!");
					disconnectClient(sender);
				}
			}
		}
		else if(cmd.equalsIgnoreCase("help")) {
			String helpmsg = "\nAvailable Commands:" +
				"\n/ping" +
				"\n/who" +
				"\n/me [msg]" +
				"\n/nick [nick]" +
				"\n/help" +
				"\n/pm [user] [message]" +
				"\n/exit|leave|quit" +
				"\n/mod [user] (mod+)" + 
				"\n/kick [user] (mod+)" +
				"\n/insult [user]" + "\n";
			sender.giveMessage(helpmsg);
		}
		else if(cmd.equalsIgnoreCase("insult")) {
			if(cmdParams.length > 1) {
				String insult = "[Shakespeare] " + cmdParams[1] + ", thou ";
				insult += insult1[rand.nextInt(insult1.length)] + " ";
				insult += insult2[rand.nextInt(insult1.length)] + " ";
				insult += insult3[rand.nextInt(insult1.length)];
				addMessage(new ChatItem(-1, insult));
			}
		}
		else if(cmd.equalsIgnoreCase("mod")) {
			if(cmdParams.length > 1) {
				if(moderators.contains(sender)) { // they want to mod someone else
					ChatConnectionHandler newMod = getByHandle(cmdParams[1]);
					if(newMod != null) {
						moderators.add(newMod);
						addMessage(new ChatItem(-1, sender.getHandle() + " has upgraded " + newMod.getHandle() + " to rank mod"));
					}
					else {
						sender.giveMessage("User with handle " + cmdParams[1] + " not found");
					}
				}
				else if(cmdParams[1].equals("suchs3cure")) { // then they want to mod themself
					addMessage(new ChatItem(-1, sender.getHandle() + " has been upgraded to rank mod"));
					moderators.add(sender);
					// upgrade user to mod
				}
			}
		}
		else if(cmd.equalsIgnoreCase("kick")) {
			if(cmdParams.length > 1) {
				if(moderators.contains(sender)) {
					ChatConnectionHandler toKick = getByHandle(cmdParams[1]);
					if(toKick != null) {
						if(cmdParams.length > 2) {
							toKick.giveMessage("You have been kicked by " + sender.getHandle() + " for " + cmdParams[2]);
							disconnectClient(toKick);
							addMessage(new ChatItem(-1, toKick.getHandle() + " has been kicked by " + sender.getHandle() + " for " + cmdParams[2]));
						}
						else {
							toKick.giveMessage("You have been kicked by " + sender.getHandle());
							disconnectClient(toKick);
							addMessage(new ChatItem(-1, toKick.getHandle() + " has been kicked by " + sender.getHandle()));
						}
					}
					else {
						sender.giveMessage("User with handle " + cmdParams[1] + " not found");
					}
				}
				else {
					sender.giveMessage("You are a not a moderator!");
				}
			}
		}
		else if(cmd.equalsIgnoreCase("pm")) {
			if(cmdParams.length > 2) {
				String pmMsg = cmdParams[2];
				for(int i = 3; i < cmdParams.length; i ++) {
					pmMsg += " " + cmdParams[i];
				}
				String pmTo = cmdParams[1];

				ChatConnectionHandler t = getByHandle(pmTo);
				if(t == null) {
					sender.giveMessage("User with handle " + pmTo + " not found");
				}
				else {
					t.giveMessage("[" + sender.getHandle() + " -> you] " + pmMsg);

					sender.giveMessage("[you -> " + pmTo + "] " + pmMsg);
					System.out.println("[" + sender.getHandle() + " -> " + pmTo + "] " + pmMsg);
				}
			}
		}
		else if(cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("leave") || cmd.equalsIgnoreCase("quit")) {
			disconnectClient(sender);
			//sender.giveMessage("disconnected");
			//connectedClients.remove(sender);
			//sender.disconnect();

			addMessage(new ChatItem(senderID, sender.getHandle() + " has left"));
		}
	}

	public void disconnectClient(ChatConnectionHandler c) {
		c.giveMessage("disconnected");
		if(moderators.contains(c)) {
			moderators.remove(c);
		}
		connectedClients.remove(c);
		c.disconnect();
	}

	/*private class ClientConnection {
		private ChatConnectionHandler clientConn;
		private String clientHandle;
		//private ArrayList<String> messagesToSend;

		public ClientConnection(ChatConnectionHandler cConn, String cHandle) {
			clientConn = cConn;
			clientHandle = cHandle;
			//messagesToSend = new ArrayList<String>();
		}
	}*/

	private ChatConnectionHandler getByHandle(String handle) {
		for(ChatConnectionHandler curClient : connectedClients) {
			if(curClient.getHandle().equals(handle)) {
				return curClient;
				//curClient.giveMessage("[" + sender.getHandle() + " -> you] " + pmMsg);
			}
		}
		return null;
	}

	private ChatConnectionHandler getByID(int id) {
		for(ChatConnectionHandler curClient : connectedClients) {
			if(curClient.getID() == id) {
				return curClient;
			}
		}
		return null;
	}

	private String getHandleFromID(int id) {
		for(ChatConnectionHandler curClient : connectedClients) {
			if(curClient.getID() == id) {
				return curClient.getHandle();
			}
		}
		return null;
	}



}

class ChatConnectionHandler implements Runnable
{
	private final static int TIMEOUT_LENGTH = 36000;
	private Socket clientSocket;
	private int myID;
	private ConcurrentLinkedQueue<String> messagesToSend;
	private ChatRoomHandler parent;

	private String myHandle;
	private boolean connectionActive;
	private int inactiveTicks = 0;

	public ChatConnectionHandler(ChatRoomHandler myParent, Socket myClient, int nID) {
		clientSocket = myClient;
		myID = nID;
		parent = myParent;
		messagesToSend = new ConcurrentLinkedQueue<String>();
		myHandle = "!UNKNOWN_" + nID;
		connectionActive = true;
		System.out.println("[Handler " + nID + "] initialized");
	}

	public void run() {
		String tmsg = "none";
		try {
			OutputStream os = clientSocket.getOutputStream();
			PrintWriter pw = new PrintWriter(os, true);
			BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			while(connectionActive) {
				try {
					if(br.ready()) {
						inactiveTicks = 0;
						tmsg = br.readLine();
						//System.out.println("[Handler ID " + myID + "] received " + tmsg);
						processMessage(tmsg);
						//parent.addMessage(new ChatItem(myID, tmsg));
						//parent.setConnMsg(myID, tmsg);
					}
					else {
						inactiveTicks += 1;
					}
					if(!messagesToSend.isEmpty()) {
						pw.println(messagesToSend.poll());
						//pw.println(messageToSend);
						//messageToSend = null;
					}
					if(inactiveTicks > TIMEOUT_LENGTH) {
						pw.println("Connection timed out");
						parent.disconnectClient(this);
						//connectionActive = false;
						// disconnect cleanly
					}
					Thread.sleep(25);
				} catch(Exception e) {
					System.out.println("Something went wrong");
					e.printStackTrace();
				}
			}
			// send him the rest of his messages
			int i = messagesToSend.size();
			for(int j = 0; j < i; j ++) {
				if(!messagesToSend.isEmpty())
					pw.println(messagesToSend.poll());
			}
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

	public void disconnect() {
		connectionActive = false;
	}

	// todo: maybe do stuff in here?
	private void processMessage(String msg) {
		parent.receiveMessage(new RawMessage(this, msg));
	}

	public void giveMessage(String msg) {
		messagesToSend.offer(msg);
		//if(messageToSend != null) {
		//	System.out.println("Warning! Overwriting a message!");
		//}
		//messageToSend = msg;
	}

	public int getID() {
		return myID;
	}

	public String getHandle() {
		return myHandle;
	}

	public void setHandle(String newHandle) {
		myHandle = newHandle;
	}
}


class ChatItem {
	private int senderID;
	private String message;

	public ChatItem(int sID, String msg) {
		senderID = sID;
		message = msg;
	}

	public int getSenderID() {
		return senderID;
	}

	public String getMessage() {
		return message;
	}
}


class RawMessage {
	private ChatConnectionHandler sender;
	private String message;

	public RawMessage(ChatConnectionHandler s, String msg) {
		sender = s;
		message = msg;
	}

	public ChatConnectionHandler getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}
}

// cert 4/21/2016