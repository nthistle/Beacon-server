import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatRoomClient {

	public String myHandle = "default";
	//public static final String host = "localhost";
	public static final String host = "71.62.99.75";
	//public static final String host = "localhost";
	////public static final String host = "71.62.99.75";
	public static final int portNumber = 1337;
	private ConcurrentLinkedQueue<String> messagesToSend;
	private PrintWriter out;
	private int sizeGUI;

	public static void main(String args[]) throws IOException, Exception {
		ChatRoomClient crc = new ChatRoomClient();
		crc.mainChat();
	}
	/*
	 * 0 - valid
	 * 1 - too short
	 * 2 - too long
	 * 3 - bad characters
	 */
	public static int isValidHandle(String s) {
		int slen = s.length();
		if(slen > 0) {
			if(slen <= 12) {
				String allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";
				for(int i = 0; i < slen; i ++) {
					if(allowedChars.indexOf(s.charAt(i)) == -1) {
						return 3;
					}
				}
				return 0;
			}
			else {
				return 2;
			}
		}
		else {
			return 1;
		}
	}

	public ChatRoomClient() {
		messagesToSend = new ConcurrentLinkedQueue<String>();
		HandleChooser hc = new HandleChooser();
		myHandle = hc.getHandle();
		hc.setVisible(false);
		hc.dispose();
		WindowSizeChooser wc = new WindowSizeChooser();
		sizeGUI = wc.getWSize();
		wc.setVisible(false);
		wc.dispose();
	}

	public void mainChat() throws IOException {
			

		//BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));
		//System.out.println("Enter a handle: ");
		//myHandle = clientInput.readLine();

		//System.out.println("Your handle is '" + myHandle + "'");

  		System.out.println("Creating socket to '" + host + "' on port " + portNumber);
        //Socket register = new Socket(host, portNumber);
        //PrintWriter out = new PrintWriter(register.getOutputStream(),true);
        //out.println("");
        //register.close();
		//while (true) {
		Socket socket = new Socket(host, portNumber);
		BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		ChatRoomGUI crg = new ChatRoomGUI(this, sizeGUI);
		crg.addWindowListener(new DisconnectOnClose());

		//System.out.println("Connected to server!");
		//System.out.println("Note: On some operating systems, text doesn't show up as you type it,");
		//System.out.println("but upon pressing 'ENTER' it will appear and send to the chatroom");
		//System.out.println("server says:" + br.readLine());
		//out.println(myHandle + " has joined");

		out.println("join " + myHandle);

		while(true) {
			if(!messagesToSend.isEmpty()) {
				String msg = messagesToSend.poll();
				if(msg.charAt(0) == '/') {
					if(msg.split(" ")[0].equalsIgnoreCase("/nick")) {
						String phandle = msg.split(" ")[1];
						if(ChatRoomClient.isValidHandle(phandle) == 0) {
							myHandle = msg.split(" ")[1];
							out.println(msg.substring(1));
						}
						else {
							crg.postMessage("Invalid handle");
						}
					}
					else {
						out.println(msg.substring(1));
					}
				}
				else {
					out.println("m " + msg);
				}
			}
			if(serverReader.ready()) {
				crg.postMessage(serverReader.readLine());
				//System.out.println(serverReader.readLine()); // must change
			}
		}


		//BufferedReader userInputBR = new BufferedReader(new InputStreamReader(System.in));
		// String uInput = "n/a";
		// while(true) {
		// 	if(clientInput.ready()) {
		// 		uInput = clientInput.readLine();
		// 		if(uInput.length() > 0) {
		// 			if(uInput.charAt(0) == '/') {
		// 				out.println(uInput.substring(1));
		// 				//String[] cmdParams = uInput.substring(1).split(" ");
		// 				//String cmd = cmdParams[0];
		// 				//out.println()
		// 				//if(cmd.equalsIgnoreCase("ping")) {
		// 				//
		// 				//}
		// 				//else if(cmd.equalsIgnoreCase(""))
		// 			}
		// 			else {
		// 				out.println("m " + uInput);
		// 			}
		// 		}
		// 		if(uInput.equalsIgnoreCase("exit")) {
		// 			System.out.println("exiting...");
		// 			out.println(myHandle + " has left");
		// 			socket.close();
		// 			System.out.println("bye!");
		// 			System.exit(0);
		// 		}
		// 		out.println(myHandle + ": " + uInput);
		// 	}
		// 	else {
		// 		if(serverReader.ready()) {
		// 			System.out.println(serverReader.readLine());
		// 		}
		// 	}
		// 	/*String uInput = userInputBR.readLine();
		// 	if(!uInput.equalsIgnoreCase("exit")) {
		// 		out.println(uInput);
		// 	}
		// 	else {
		// 		socket.close();
		// 		System.exit(0);
		// 	}*/
		// }
			//String userInput = userInputBR.readLine();

			//out.println(userInput);

			//System.out.println("server says:" + br.readLine());

			//if ("exit".equalsIgnoreCase(userInput)) {
			//	socket.close();
			//	break;
			//}
		//}
	}

	public void sendMessage(String msg) {
		messagesToSend.offer(msg);
	}
/*
	public void sendMessage(String msg) {
		if(msg.length() > 0) {
			if(msg.charAt(0) == '/') {
				// todo make concurrent crap instead
			}
		}
	}*/

	class DisconnectOnClose extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			out.println("exit");
		}
	}
}

/*
 * Credit to Ajay Fewell for much of this GUI code, although it was
 * reworked by Neil Thistlethwaite to fit with the chat client itself
 * (and in places so it would look prettier)
 */

class ChatRoomGUI extends JFrame
{
	private JPanel mainPanel;
	private JPanel southPanel;
	private JTextArea chatbox;
	private JTextField messagebox;
	private JButton sendButton;
	private JScrollPane cscrollpane;
	private SendButtonListener sendListener;
	private ChatRoomClient parent;
	private int mySize;

	public ChatRoomGUI(ChatRoomClient myParent, int sizeGUI) {
		super("Chatroom");
		mySize = sizeGUI;
		parent = myParent;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		southPanel = new JPanel(new FlowLayout()); // holds message box, button
		sendButton = new JButton("Send");
		sendListener = new SendButtonListener();
		sendButton.addActionListener(sendListener);
		if(sizeGUI == WindowSizeChooser.SMALL) {
			chatbox = new JTextArea(14,40);
			chatbox.setFont(new Font("Monospaced", Font.PLAIN, 12));
			messagebox = new JTextField(26);
			messagebox.setFont(new Font("Monospaced", Font.PLAIN, 12));
		}
		else if(sizeGUI == WindowSizeChooser.MEDIUM) {
			chatbox = new JTextArea(21,55);
			chatbox.setFont(new Font("Monospaced", Font.PLAIN, 14));
			messagebox = new JTextField(44);
			messagebox.setFont(new Font("Monospaced", Font.PLAIN, 14));
		}
		else {
			chatbox = new JTextArea(28,60);
			chatbox.setFont(new Font("Monospaced", Font.PLAIN, 16));
			messagebox = new JTextField(62);
			messagebox.setFont(new Font("Monospaced", Font.PLAIN, 16));
		}
		//chatbox = new JTextArea(14,40);
		//messagebox = new JTextField(26);
		//messagebox.requestFocusInWindow()
		messagebox.addKeyListener(new EnterListener());
		//messagebox.setFont(new Font("Monospaced", Font.PLAIN, 12));

		//DefaultCaret caret = (DefaultCaret) chatbox.getCaret();
		//caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		chatbox.setEditable(false);
		//chatbox.setFont(new Font("Monospaced", Font.PLAIN, 12));
		chatbox.setLineWrap(true);
		cscrollpane = new JScrollPane(chatbox);
		cscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		mainPanel.add(BorderLayout.CENTER, cscrollpane);
		southPanel.add(messagebox);
		southPanel.add(sendButton);
		mainPanel.add(BorderLayout.SOUTH, southPanel);

		Container contentPane = this.getContentPane();
		contentPane.add(mainPanel);
		//contentPane.add(superPanel);

		this.setResizable(false);
		this.setSize(470,300); // somewhat irrelevant
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
	}

	public void postMessage(String msg_raw) {
		int splitLength = -1; // 37
		if(mySize == WindowSizeChooser.SMALL) {
			splitLength = 39;
		}
		else if(mySize == WindowSizeChooser.MEDIUM) {
			splitLength = 54;
		}
		else {
			splitLength = 67;
		}

		String[] splitByLine = msg_raw.split("\n");
		for(String msg : splitByLine) {
			// line len is 39
			//chatbox.append(msg + "\n");
			if(msg.length() <= splitLength) {
				chatbox.append(msg + "\n");
			}
			else {
				String[] intoparts = msg.split(" ");
				//System.out.print("parts: " );
				//for(String p : intoparts) {
				//	System.out.print(p + ",");
				//}
				//System.out.println();
				String curmsg = intoparts[0];
				for(int i = 1; i < intoparts.length; i ++) {
					//System.out.println("curmsg: " + curmsg);
					//System.out.println("part(i): " + intoparts[i] + " (len=" + intoparts[i].length() + ")");
					if(curmsg.length() + 1 + intoparts[i].length() <= splitLength) {
						curmsg += " " + intoparts[i];
					}
					else {
						chatbox.append(curmsg + "\n");
						curmsg = intoparts[i];
					}
				}
				chatbox.append(curmsg + "\n");
			}
			chatbox.setCaretPosition(chatbox.getDocument().getLength());
			//JScrollBar vertical = cscrollpane.getVerticalScrollBar();
			//vertical.setValue(vertical.getMaximum());
		}
	}

	public void sendOutMessage() {
		String ut = messagebox.getText();
		if(ut.length() > 0) {
			parent.sendMessage(ut);
			if(ut.charAt(0) != '/')
				postMessage(parent.myHandle + ": " + ut);
				//chatbox.append(parent.myHandle + ": " + ut + "\n");
			messagebox.setText("");
		}
		messagebox.requestFocusInWindow();
	}


	class EnterListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			if(code == KeyEvent.VK_ENTER) {
				sendOutMessage();
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

	}

	class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			sendOutMessage();
		}
	}

}

class WindowSizeChooser extends JFrame
{
	private JLabel topLabel;
	private JPanel superPanel;
	private JPanel mainPanel;
	private JPanel bottomButtons;

	private JButton launchSmall;
	private JButton launchMed;
	private JButton launchLarge;

	public final static int NOT_SELECTED = 0;
	public final static int SMALL = 1;
	public final static int MEDIUM = 2;
	public final static int LARGE = 3;

	private int wsize = NOT_SELECTED;

	public WindowSizeChooser() {
		super("");
		superPanel = new JPanel();
		superPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		mainPanel = new JPanel();
		GridLayout mainLayout = new GridLayout(2,1);
		mainLayout.setHgap(10);
		mainLayout.setVgap(10);
		mainPanel.setLayout(mainLayout);
		topLabel = new JLabel("Launch what size window?");
		mainPanel.add(topLabel);
		bottomButtons = new JPanel();
		GridLayout buttonLayout = new GridLayout(1,3);
		buttonLayout.setHgap(5);
		buttonLayout.setVgap(5);
		bottomButtons.setLayout(buttonLayout);
		launchSmall = new JButton("Small");
		launchMed = new JButton("Medium");
		launchLarge = new JButton("Large");

		launchSmall.addActionListener(new SizeButtonListener(SMALL));
		launchMed.addActionListener(new SizeButtonListener(MEDIUM));
		launchLarge.addActionListener(new SizeButtonListener(LARGE));

		bottomButtons.add(launchSmall);
		bottomButtons.add(launchMed);
		bottomButtons.add(launchLarge);
		mainPanel.add(bottomButtons);
		superPanel.add(mainPanel);
		Container contentPane = this.getContentPane();
		contentPane.add(superPanel);
		this.setResizable(false);
		this.setSize(500,500); // irrelevant
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.pack();
	}

	// intentionally blocks
	public int getWSize() {
		while(wsize == NOT_SELECTED) {
			try {
				Thread.sleep(50);
			} catch(Exception e) {
				// nothing
			}
		}
		return wsize;
	}

	class SizeButtonListener implements ActionListener {
		private int myvalue;

		public SizeButtonListener(int mv) {
			super();
			myvalue = mv;
		}
		public void actionPerformed(ActionEvent e) {
			wsize = myvalue;
		}
	}
}//gridlayout 1,3


class HandleChooser extends JFrame
{
	private JPanel superPanel;
	private JPanel mainPanel;
	private JPanel topPanel;
	private JLabel handleLabel;
	private JButton enterServerButton;
	private JTextField handleField;
	private String handle = null;

	public HandleChooser() {
		super("");//"Choose Handle");
		superPanel = new JPanel();
		superPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		mainPanel = new JPanel();
		GridLayout mainLayout = new GridLayout(2,1);
		mainLayout.setHgap(10);
		mainLayout.setVgap(10);
		mainPanel.setLayout(mainLayout);
		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(2,1));
		handleLabel = new JLabel("Enter a Handle:");
		handleField = new JTextField(12);
		handleField.setFont(new Font("Monospaced", Font.PLAIN, 12));
		handleField.addKeyListener(new HCEnterListener());
		topPanel.add(handleLabel);
		topPanel.add(handleField);
		enterServerButton = new JButton("Enter Chatroom");
		enterServerButton.addActionListener(new EnterChatRoomButtonListener());
		mainPanel.add(topPanel);
		mainPanel.add(enterServerButton);
		superPanel.add(mainPanel);

		Container contentPane = this.getContentPane();
		contentPane.add(superPanel);

		this.setResizable(false);
		this.setSize(500,500); // somewhat irrelevant
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.pack();
	}

	// intentionally blocks
	public String getHandle() {
		while(handle == null) {
			try {
				Thread.sleep(50);
			} catch(Exception e) {
				// nothing
			}
		}
		return handle;
	}

	public void doHandle() {
		if(handle == null) {
			String possHandle = handleField.getText();
			int result = ChatRoomClient.isValidHandle(possHandle);

			if(result == 0) {
				handle = possHandle;
			}
			else if(result == 1) {
				JOptionPane.showMessageDialog(null, "Handle must be longer than 0 characters!");
				return;
			}
			else if(result == 2) {
				JOptionPane.showMessageDialog(null, "Handle cannot be longer than 12 characters!");
				return;
			}
			else if(result == 3) {
				JOptionPane.showMessageDialog(null, "Handle must be alphanumeric!");
				return;
			}
		}
	}

	class HCEnterListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				doHandle();
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

	}

	class EnterChatRoomButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			doHandle();
		}
	}
}
