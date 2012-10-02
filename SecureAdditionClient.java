import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;

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
				
				if(outStr.length() > 6 && outStr.substring(0,6).equals("Upload")) {
					String file = outStr.substring(7);
					String filepath ="clientfiles/" +file;
					File f = new File(filepath);
					if(f.exists()) {
						BufferedReader infile = new BufferedReader(new FileReader(filepath));
						String line = "";
						while((line = infile.readLine())!=null)
						{
							out.println(line);
						}
						infile.close();
						out.println("##EOF##");
						
					}
				}
			}
			
			/*
			String numbers = "1.2 3.4 5.6";
			System.out.println( "\nC -> S:  Sending the numbers " + numbers );
			out.println( numbers );
			System.out.println( in.readLine() );

			numbers = "100 200";
			System.out.println( "\nC -> S:  Sending the numbers " + numbers );
			out.println( numbers );
			System.out.println( in.readLine() );

			out.println ( "ciao" );
			*/
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	
	public static void main( String[] args ) {
		try {
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
		}
	}
}
