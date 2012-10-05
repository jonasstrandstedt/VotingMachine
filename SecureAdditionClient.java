//package start;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;

class ExtraThread extends Thread{
		BufferedReader incoming;
         ExtraThread(BufferedReader in) {
             this.incoming = in;
         }
 
         public void run() {
			 try {
				 String inputLine;
	 			while ((inputLine = this.incoming.readLine()) != null && !inputLine.equals("Bye.")) {
			
	 				System.out.println(inputLine);
	 			}
				 } catch(IOException ioe) {
				 }
			 
         }
     }



public class SecureAdditionClient {

	public SecureAdditionClient () {

		}

	//GUI 
	private static void createAndShowGUI()
	{
		//Window
		JFrame frame = new JFrame("TestarGUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Layout
		JPanel contentPane = new JPanel(new BorderLayout());

		//Label
		JLabel label = new JLabel("Hello Voting People!");
		contentPane.add(label, BorderLayout.PAGE_START);

		//Buttons
		JRadioButton voteButton1 = new JRadioButton("Vote for Kristina ;D");
		
		contentPane.add(voteButton1, BorderLayout.CENTER);

		frame.setContentPane(contentPane);		
		

		//Container
		
		//frame.getContentPane().setLayout(new BorderLayout());
		//frame.getContentPane().add(pnlEast, BorderLayout.CENTER);

		//Display the window
		frame.pack();
		frame.setVisible(true);
	}

	//End of GUI
	private InetAddress host;
	// This is not a reserved port number 
	static final int port = 8189;
	static final String KEYSTORE = "ClientKeystore.ks";
	static final String TRUSTSTORE = "ClientTruststore.ks";
	static final String STOREPASSWD = "111111";
	static final String ALIASPASSWD = "333333";
  
	
	public SecureAdditionClient( InetAddress host ) {
		this.host = host;
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
			
			BufferedReader in;
			in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
			PrintWriter out = new PrintWriter( clientSocket.getOutputStream(), true );
			
			ExtraThread incThread = new ExtraThread(in);
			incThread.start();
			
			String str;
			String outStr = "";
			while (!outStr.equals("ciao") ) {
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				outStr = reader.readLine();
				out.println(outStr);

			}
			

		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	
	public static void main( String[] args ) {
		SecureAdditionClient client = new SecureAdditionClient(); 
		client.createAndShowGUI();

		/*try {
			InetAddress host = InetAddress.getLocalHost();
			if ( args.length > 0 ) {
				host = InetAddress.getByName( args[0] );
			}
			SecureAdditionClient addClient = new SecureAdditionClient( host );
			addClient.run();
		}
		catch ( UnknownHostException uhx ) {
			System.out.println( uhx );
			uhx.printStackTrace();
		}*/
	}
}
