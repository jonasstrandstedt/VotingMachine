//package start;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

class ExtraThread extends Thread {
	BufferedReader incoming;
	SecureAdditionClient client;
	Boolean wait_for_command;
	
	ExtraThread(BufferedReader in, SecureAdditionClient c) {
		this.incoming = in;
		this.client = c;
		wait_for_command = true;
	}
	
	public void run() {
		try {
			String inputLine;
			while (wait_for_command) {
				inputLine = this.incoming.readLine();
				wait_for_command = client.handle_command(inputLine);
			}
		} catch(IOException ioe) {
		} 
	}
}



public class SecureAdditionClient implements ActionListener {
	private InetAddress host;
	// This is not a reserved port number 
	static final int port = 8189;
	static final String KEYSTORE = "ClientKeystore.ks";
	static final String TRUSTSTORE = "ClientTruststore.ks";
	static final String STOREPASSWD = "111111";
	static final String ALIASPASSWD = "333333";
	private BufferedReader in = null;
	private PrintWriter out = null;
	private Vector<JRadioButton> buttonList = null;
	private Vector<String> alternativeList = null;
	private Vector<JLabel> nrOfVotes = null;
	private JPanel alternatives;
	private JRadioButton alt1;
	private JButton voteButton;
	private JPanel results;
	private ButtonGroup buttonGroup;
	private int token;
	
	public SecureAdditionClient () {
	}
	
	public SecureAdditionClient( InetAddress host ) {
		this.host = host;
	}

	//GUI 
	private void createAndShowGUI()
	{
		String inStr = "";
		int nrOfArguments = 0;
		buttonList = new Vector<JRadioButton>();
		alternativeList = new Vector<String>();
		nrOfVotes = new Vector<JLabel>();

		try {
		//Window
		JFrame frame = new JFrame("VotingMachine");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Layout
		JPanel contentPane = new JPanel(new BorderLayout());

		//Label
		this.out.println("fetch_question"); //ask for a question from server
		inStr = this.in.readLine();
		CommandParser question = new CommandParser(inStr);
		JLabel label = new JLabel(question.next_argument());
		contentPane.add(label, BorderLayout.PAGE_START);

		//Buttons
		this.out.println("fetch_alternatives");
		inStr= this.in.readLine(); 
		CommandParser command = new CommandParser(inStr);

		//Statistics
		this.out.println("fetch_statistics");
		inStr= this.in.readLine();
		CommandParser stats = new CommandParser(inStr);
	
		//buttongroup
		buttonGroup = new ButtonGroup();
		alternatives = new JPanel();
		alternatives.setLayout(new GridLayout(command.count_arguments(),1));
		print_cmd("stats = " + stats.count_arguments());
		
		System.out.println(command.count_arguments());
		for (int i = 0; i<command.count_arguments(); i++)
		{

			//alternatives
			String arg = command.next_argument();
			alternativeList.add(arg);
			System.out.println(arg);
			JRadioButton temp = new JRadioButton(arg);
			buttonList.add(temp);
			buttonGroup.add(temp);
			alternatives.add(temp);
		}

		contentPane.add(alternatives , BorderLayout.CENTER);
			
				
		//VoteButton
		voteButton = new JButton("Vote!");
		voteButton.addActionListener(this);
		contentPane.add(voteButton, BorderLayout.EAST);
				
		
		//VotingResults
		results = new JPanel();
		results.setLayout(new GridLayout(alternativeList.size(),2)); //alternative, number of votes
		for(int i=0; i<alternativeList.size(); i++)
		{
			JLabel label1 = new JLabel(alternativeList.get(i));
			
			JLabel label2 = new JLabel(stats.next_argument());
			nrOfVotes.add(label2);
			results.add(label1);
			results.add(label2);
			label1 = null;
			label2 = null;

		}
		contentPane.add(results, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);		

		//Display the window
		frame.pack();
		frame.setVisible(true);
		
		// listen for commands
		ExtraThread incThread = new ExtraThread(in, this);
		incThread.start();
		
		}
		
		catch(Exception e)
		{

		}
	}
	//End of GUI
	
	//Lyssnarmetod
	public void actionPerformed(ActionEvent e) {

		 if(e.getSource() == voteButton)
		{	
			System.out.println("Du har röstat!");
			
			for(int i=0; i<buttonList.size(); i++)
			{
				if(buttonList.get(i).isSelected()){
					this.out.println("vote " + this.token + " " + i);
				}
			}
		}
	}

	public Boolean handle_command(String command) {
		try {
			this.print_cmd("Recieved: " + command);

			CommandParser cmd = new CommandParser(command);

			if (cmd.isEqual("quit", "q")) {
				return false;
			}
			if (cmd.isEqual("statistics")) { //update labels for statistics
				for(int i=0; i<nrOfVotes.size(); i++){
				nrOfVotes.get(i).setText(cmd.next_argument());
				}
			}
			
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
			return false;
		}

		return true;
	}
  // The method used to start a client object
	public void run() {
		try {
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), STOREPASSWD.toCharArray() );
			
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), ALIASPASSWD.toCharArray() );
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, STOREPASSWD.toCharArray() );
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
			SSLSocket clientSocket =  (SSLSocket)sslFact.createSocket(host, port);
			//clientSocket.setEnabledCipherSuites( clientSocket.getSupportedCipherSuites() );
			String[] ciphers = clientSocket.getSupportedCipherSuites();
			String[] selectedCiphers = { ciphers[9] };
			System.out.println(ciphers[9]);
			clientSocket.setEnabledCipherSuites( selectedCiphers );
			
			
			this.in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
			this.out = new PrintWriter( clientSocket.getOutputStream(), true );
			
			
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	public void print_cmd(String msg) {
		System.out.println(">>>> Secure Client: " + msg);
	}
	
	public Boolean get_token() {
		
		Authenticate auth = new Authenticate();
		String name = null;
		String password = null;
		
		while(name == null || name.equals("")) {
			name = JOptionPane.showInputDialog(null, "Enter username: ", "", 1);
		}
		while(password == null || password.equals("")) {
			password = JOptionPane.showInputDialog(null, "Enter password: ", "", 1);
		}
		
		
		this.token = auth.get_token(name, password);
		
		return this.token != -1;
	}
	
	
	public static void main( String[] args ) {
		SecureAdditionClient client = null;
		
		try {
			InetAddress host = InetAddress.getLocalHost();
			if ( args.length > 0 ) {
				host = InetAddress.getByName( args[0] );
			}
			client = new SecureAdditionClient( host );
		}
		catch ( UnknownHostException uhx ) {
			client = new SecureAdditionClient(); 
		}
		
		

		if (client.get_token()) {
			client.run();
			client.createAndShowGUI();
		}
		

		
	}
}
