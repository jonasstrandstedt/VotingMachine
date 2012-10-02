
import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;


class ServerConnectionHandler extends Thread {

    private Socket socket = null;
	
	public void run() {
		try {
	        BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			PrintWriter out = new PrintWriter( socket.getOutputStream(), true );			
			
			String str;
			while ( !(str = in.readLine()).equals("ciao") ) {
			    System.out.println("\n>>>> Secure Server: receives: " + str);
				
				if (str.length() > 8 && str.substring(0,8).equals("Download"))
				{

				}
				
				else if(str.length() > 6 && str.substring(0,6).equals("Upload"))
				{

				}
					
				
				else if(str.length() > 6 && str.substring(0,6).equals("Delete"))
				{

				}
					
				
				else
				{
					System.out.println("Unknowned command!");
				}
				out.println("Command :)");

			}
			socket.close();
		}
        catch(Exception e) {
		
		}
	}
	
	public ServerConnectionHandler(Socket socket) {
	    this.socket = socket;
	}
}

public class SecureAdditionServer {
	static final int port = 8189;
	static final String KEYSTORE = "ServerKeystore.ks";
	static final String TRUSTSTORE = "ServerTruststore.ks";
	static final String STOREPASSWD = "222222";
	static final String ALIASPASSWD = "444444";
	
	private SSLServerSocket initServer() throws Exception {
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
		SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
		
		// create a server socket with infinite number of backlogs
		SSLServerSocket server = (SSLServerSocket) sslServerFactory.createServerSocket( port , 50 );
		server.setNeedClientAuth(false);
		
		String[] ciphers = server.getSupportedCipherSuites();
		String[] selectedCiphers = { ciphers[9] };
		System.out.println(ciphers[9]);
		server.setEnabledCipherSuites( selectedCiphers );
		
		return server;
	}

	public void run() {
		try {
			
			SSLServerSocket server = this.initServer();
			
			System.out.println("\n>>>> Secure Server: active ");
			// accept a connection
			
			while(true) {
				System.out.println("\n>>>> Secure Server: Waiting for connection request");
				SSLSocket serverSocket = (SSLSocket) server.accept();
				System.out.println("\n>>>> Secure Server: connection request accepted");
				ServerConnectionHandler handle = new ServerConnectionHandler(serverSocket);
				System.out.println("\n>>>> Secure Server: about to run");
				handle.start();
			}
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	public static void main( String[] args ) {
		SecureAdditionServer addServer = new SecureAdditionServer();
		addServer.run();
	}
}
