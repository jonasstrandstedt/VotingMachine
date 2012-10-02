
import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;


public class SecureAdditionServer {
	static final int port = 8189;
	static final String KEYSTORE = "ServerKeystore.ks";
	static final String TRUSTSTORE = "ServerTruststore.ks";
	static final String STOREPASSWD = "222222";
	static final String ALIASPASSWD = "444444";

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
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket server = (SSLServerSocket) sslServerFactory.createServerSocket( port );
			server.setNeedClientAuth(false);
			String[] ciphers = server.getSupportedCipherSuites();
			String[] selectedCiphers = { ciphers[9] };
			System.out.println(ciphers[9]);
			server.setEnabledCipherSuites( selectedCiphers );
			
			System.out.println("\n>>>> Secure Server: active ");
			// accept a connection
			SSLSocket serverSocket = (SSLSocket) server.accept();
			System.out.println("\n>>>> Secure Server: connection request");

            BufferedReader in = new BufferedReader( new InputStreamReader( serverSocket.getInputStream() ) );
			PrintWriter out = new PrintWriter( serverSocket.getOutputStream(), true );			
			
			String str;
			while ( !(str = in.readLine()).equals("ciao") ) {
			    System.out.println("\n>>>> Secure Server: receives: " + str);
				
				if (str.length() > 8 && str.substring(0,8).equals("Download"))
				{
					System.out.println("Downloading");
					String file = str.substring(9);
					System.out.println("Downloading " + file);
					BufferedReader infile = new BufferedReader(new FileReader("serverfiles/" + file));
					String line = "";
					while((line = infile.readLine())!=null)
					{
						out.println(line);
					}
					//download pier.txt
				}
				
				else if(str.length() > 6 && str.substring(0,6).equals("Upload"))
				{
					System.out.println("Recieving file");
					
					String file = str.substring(7);
					String filepath ="serverfiles/" +file;
					
					out.println("Uploading " + file);
					
					PrintWriter outfile = new PrintWriter(new BufferedWriter(new FileWriter(filepath)));
					String filecontent = "";
					while(!(str = in.readLine()).equals("##EOF##"))
						outfile.println(str);
					
					outfile.close();
					System.out.println("Done recieving file");
				}
					
				
				else if(str.length() > 6 && str.substring(0,6).equals("Delete"))
				{
					String file = str.substring(7);
					String filepath ="serverfiles/" +file;
				
					File f = new File(filepath);
					
					if(f.exists()) {
						f.delete();
						System.out.println("Deleting " + file);
					}
				}
					
				
				else
				{
					System.out.println("Unknowned command!");
					out.println("Command :)");
				}
					
				
				/*
				double result = 0;
				StringTokenizer st = new StringTokenizer( str );
				try {
					while( st.hasMoreTokens() ) {
						Double d = new Double( st.nextToken() );
						result += d.doubleValue();
					}
					out.println( "S -> C:  The result is " + result );
				}
				catch( NumberFormatException nfe ) {
					out.println( "S -> C:  Sorry C, but your list contains an invalid number. Do you know what a number is?" );
				}*/
			}
			serverSocket.close();
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
