import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;

/*The ClassFileServer implements a ClassServer that
 * reads files from the file system.
 */

public class FileServer extends ClassServer {

	private String docroot;

	private static int DefaultServerPort = 2001;		//default port should a port not be defined

	/**
	 * Constructs a ClassServer.
	 */
	public FileServer(ServerSocket ss, String docroot) throws IOException
	{
		super(ss,docroot);
		this.docroot = docroot;
	}

	/**
	 * Main method to create the class server that reads
	 * files. This takes 3 command line arguments, the
	 * port on which the server accepts requests, the
	 * root of the path and if the connection is TLS. To start up the server: 
	 *
	 */
	public static void main(String args[])
	{
		System.out.println(
				"USAGE: java ClassFileServer port docroot [TLS]");
		System.out.println("");
		System.out.println(
				"If the third argument is TLS, it will start as\n" +
						"a TLS/SSL file server, otherwise, it will be\n" +
						"an ordinary file server. \n" +
						"If the fourth argument is true,it will require\n" +
				"client authentication as well.");

		int port = DefaultServerPort;
		String docroot = "";

		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}

		if (args.length >= 2) {
			docroot = args[1];
		}
		String type = "PlainSocket";
		if (args.length >= 3) {
			type = args[2];
		}
		try {
			ServerSocketFactory ssf =
					FileServer.getServerSocketFactory(type);		//creates server socket factory
			ServerSocket ss = ssf.createServerSocket(port);			//assign factory to server socket ss
			new FileServer(ss, docroot);							//create the ClassServer Instance
		} catch (IOException e) {
			System.out.println("Unable to start ClassServer: " +
					e.getMessage());
			e.printStackTrace();
		}
	}

	private static ServerSocketFactory getServerSocketFactory(String type) {		//responsible of managing the certificate
		if (type.equals("TLS")) {
			SSLServerSocketFactory ssf = null;
			try {
				// set up key manager to do server authentication
				SSLContext ctx;
				KeyManagerFactory kmf;
				KeyStore ks;
				char[] passphrase = "passphrase".toCharArray();

				ctx = SSLContext.getInstance("TLS");								//sets connection protocol to TLS
				kmf = KeyManagerFactory.getInstance("SunX509");
				ks = KeyStore.getInstance("JKS");

				ks.load(new FileInputStream("testkeys"), passphrase);				//uses (testkeys) truststore with self-signed local host certificate
				kmf.init(ks, passphrase);
				ctx.init(kmf.getKeyManagers(), null, null);

				ssf = ctx.getServerSocketFactory();
				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;
	}
	
	public String getDocRoot() {					//sends the docroot to the ClassServer instance
		// TODO Auto-generated method stub
		return docroot;
	}
}