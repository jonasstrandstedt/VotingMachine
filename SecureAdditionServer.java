
import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.util.*;
import java.util.concurrent.Semaphore;

class ServerInputOutput extends Thread {
	ServerConnectionHandler sch;
	BufferedReader incoming;
	PrintWriter outgoing;
	Boolean wait_for_command = true;
		
     ServerInputOutput(ServerConnectionHandler conn, BufferedReader in, PrintWriter out) {
     	this.sch = conn;
        this.incoming = in;
        this.outgoing = out;
     }
 
    public void run() {
		try {
			String inputLine;
			while ( wait_for_command && (inputLine = this.incoming.readLine()) != null)
			{
				wait_for_command = sch.handle_command(inputLine);
			}
		} 
		catch(IOException ioe) {
		
		}
		sch.print_cmd("Thread done.");
		sch.terminate();
	}
}


class ServerConnectionHandler extends Thread {
	public static int connection_handler_next_id= 0;

	private SecureAdditionServer main = null;
	private int handler_id;
    private Socket socket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
	
	public void run() {
		try {
	        in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			out = new PrintWriter( socket.getOutputStream(), true );			
			
			ServerInputOutput sio = new ServerInputOutput(this, in, out);
			sio.start();
			
			
		}
        catch(Exception e) {
		
		}
	}


	public Boolean handle_command(String command) {
		try {
			this.print_cmd("Recieved: " + command);

			CommandParser cmd = new CommandParser(command);

			if (cmd.isEqual("quit", "q")) {
				out.println("Terminating connection on request, thank you!");
				return false;
			}
			
			if (cmd.isEqual("fetch_question", "fetch_poll")) {
				out.println("response_question \"What is your favorite course?\"");
			}
			
			if (cmd.isEqual("fetch_alternatives")) {
				out.println("response_alternatives \"TNCG15 Advanced global Illumination\" \"TNM031 Network programming\" \"TNM090 Software Engineering\"");
			}

			if(cmd.isEqual("add") && cmd.count_arguments() > 0) {
				String num = cmd.next_argument();
				main.add(num);
			}

			if(cmd.isEqual("argtest")) {
				this.print_cmd("Command: " + cmd);
				this.print_cmd("Arg1: " + cmd.next_argument());
				this.print_cmd("Arg2: " + cmd.next_argument());
				this.print_cmd("Arg3: " + cmd.next_argument());
				this.print_cmd("Arg4: " + cmd.next_argument());
			}
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
			return false;
		}

		return true;
	}

	public void send_command(String command) {
		out.println(command);
	}

	public void handle_notification() {
		out.println("Shit just got real");
	}

	public void print_cmd(String msg) {
		System.out.println("("+handler_id+") >> " + msg);
	}

	public void terminate() {
		try {
			main.remove_handle(this);
			socket.close();
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	public ServerConnectionHandler(SecureAdditionServer m, Socket socket) {
		handler_id = connection_handler_next_id;
		connection_handler_next_id++;

		this.main = m;
	    this.socket = socket;
	}
}

public class SecureAdditionServer {
	static final int port = 8189;
	static final String KEYSTORE = "ServerKeystore.ks";
	static final String TRUSTSTORE = "ServerTruststore.ks";
	static final String STOREPASSWD = "222222";
	static final String ALIASPASSWD = "444444";
	
	Boolean spawn = true;

	private static Semaphore mutex = new Semaphore(1);
	private static int additionresult = 0;
	private static Vector<ServerConnectionHandler> connectionList = null;
	
	
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
	
			this.additionresult = 0;
			this.connectionList = new Vector<ServerConnectionHandler>();
			
			SSLServerSocket server = this.initServer();
			
			this.print_cmd("Active");
			while(spawn) {
				this.print_cmd("Waiting for connection request");
				SSLSocket serverSocket = (SSLSocket) server.accept();

				this.print_cmd("Connection request accepted");

				ServerConnectionHandler handle = new ServerConnectionHandler(this, serverSocket);
				this.add_handle(handle);
				handle.start();
			}
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}

	public void add(String nr) {
		int number = 0;
		try {
			number = Integer.parseInt(nr);
		}
		catch (NumberFormatException nfo) {}

		this.print_cmd("Trying to acquire the mutex");
		try {
			mutex.acquire();
			this.print_cmd("Mutex aquired");
			additionresult += number;
			
			this.notify_all_connections("number is now " + additionresult);
			Thread.sleep(4000);

			mutex.release();
			this.print_cmd("Mutex released");
		}
		catch(InterruptedException ie) {

		}

	}

	public void notify_all_connections(String msg) {
		for (int i = 0; i < this.connectionList.size(); i++) {
			ServerConnectionHandler h = this.connectionList.get(i);
			h.send_command(msg);
		}
	}

	private void add_handle(ServerConnectionHandler h) {
		this.connectionList.add(h);
		this.print_cmd(this.connectionList.size() + " active connections");
	}

	public void remove_handle(ServerConnectionHandler h) {
		this.connectionList.remove(h);
		this.print_cmd(this.connectionList.size() + " active connections");
	}

	private void print_cmd(String msg) {
		System.out.println(">>>> Secure Server: " + msg);
	}
	
	public static void main( String[] args ) {
		SecureAdditionServer addServer = new SecureAdditionServer();
		addServer.run();
	}
}
