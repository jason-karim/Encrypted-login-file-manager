
import java.io.*;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.util.Scanner;

public class Client {

	private static BufferedInputStream in;
	private static OutputStream rawOut;
	private static Scanner sc = new Scanner(System.in);
	private static SSLSocket socket;
	private static SSLSocketFactory factory;
	private static BufferedReader Bin;

	public static void main(String[] args) throws Exception {
		String host = null;
		int port = 2001;													//default port if not specified
		for (int i = 0; i < args.length; i++)
			System.out.println(args[i]);

		if (args.length < 2) {
			System.out.println(
					"USAGE: java Client " +
					"host port");
			System.exit(-1);
		}

		try {
			host = args[0];
			port = Integer.parseInt(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("USAGE: java Client " +
					"host port requestedfilepath");
			System.exit(-1);
		}

		//main
		try {
			factory = null;
			try {															//set up the certificate manager
				SSLContext ctx;
				KeyManagerFactory kmf;
				KeyStore ks;
				char[] passphrase = "passphrase".toCharArray();

				ctx = SSLContext.getInstance("TLS");
				kmf = KeyManagerFactory.getInstance("SunX509");
				ks = KeyStore.getInstance("JKS");

				ks.load(new FileInputStream("testkeys"), passphrase);		//uses same (testkeys) truststore as the server to find the localhost certificate of the server for authentication

				kmf.init(ks, passphrase);
				ctx.init(kmf.getKeyManagers(), null, null);

				factory = ctx.getSocketFactory();
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}

			String path="";

			while(true) {
				String resp="";				
				while(!(resp.equals("success"))) {							//loop until logged in or registered (response "success" from server)
					socket = (SSLSocket)factory.createSocket(host, port);
					socket.startHandshake();								//establishing the TLS connection to server
					System.out.println("Handshake done");

					rawOut = socket.getOutputStream();						//creating output stream

					in = new BufferedInputStream(socket.getInputStream());	//creating input stream
					String choice="";
					String log="";
					while(!(choice.contains("1")||choice.contains("2"))) {
						System.out.println("(1)Login\r\n(2)Register\r\n");
						choice = sc.next();
						System.out.println("user:");
						String user = sc.next();
						sc.nextLine();
						System.out.println("pass:");
						String pass = sc.next();
						if(choice.contains("1")) {							//login
							log="login user:" + user+"pass:"+pass+"\r\n";
						}else if(choice.contains("2")){						//register
							log="register user:" + user+"pass:"+pass+"\r\n";
						}
					}
					resp=inter(log);										//send server request, and return response
					System.out.println(resp);
					socket.close();
				}

				while(true) {												//send or receive files, or receive file directory
					socket = (SSLSocket)factory.createSocket(host, port);
					socket.startHandshake();								//establishing the TLS connection to server
					System.out.println("Handshake done");

					rawOut = socket.getOutputStream();						//creating output stream

					in = new BufferedInputStream(socket.getInputStream());	//creating input stream

					String choice="";
					while(!(choice.contains("1")||choice.contains("2")||choice.contains("3"))) {
						System.out.println("(1)Get File\r\n(2)Store\r\n(3)Get Directory");
						choice=sc.next();
						if(choice.contains("1")) {
							System.out.println("File directory: \r\n");
							getDir();										//get directory
						}else if(choice.contains("2")){
							System.out.println("save as:");
							resp=sendFileName("store "+ sc.next()+"\r\n");	//store file into server
							System.out.println(resp);
						}else if(choice.contains("3")) {
							System.out.println("File directory: \r\n");
							getDir();										//get directory
						}
					}

					in.close();
					rawOut.close();
					socket.close();

					if(choice.contains("1")){
						System.out.println("Which file? (example: a.txt)");	//if choice was 1, get file from server
						path = "GET "+ sc.next()+"\r\n";
						getFile(path,host,port);
					}else if(choice.contains("2")) {						//if choice was 2, send file to be stored
						System.out.println("File Location: (example: D:\\\\example.jpg)");
						System.out.println(sendFile(sc.next(),host,port));
					}


				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getDir() throws IOException {						//get dir from server
		String input = "GET Dir\r\n";
		rawOut.write((input).getBytes());
		rawOut.flush();

		BufferedReader in =
				new BufferedReader(
						new InputStreamReader(socket.getInputStream()));	//creating input stream
		String read="";
		while ((read = in.readLine()) != null) {							//display directory to terminal
			System.out.println(read);
		}
		System.out.println();
	}

	public static String inter(String input) throws IOException {			//interact with server

		rawOut.write((input).getBytes());
		rawOut.flush();														//send server request

		System.out.println("waiting response");
		Bin =new BufferedReader(
				new InputStreamReader(socket.getInputStream()));			//creating input stream
		return Bin.readLine();												//reading response
	}

	public static void getFile(String input,String host,int port) throws IOException, InterruptedException {
		socket = (SSLSocket)factory.createSocket(host, port);
		socket.startHandshake();											//establishing the TLS connection to server
		System.out.println("Handshake done");

		rawOut = socket.getOutputStream();									//creating output stream

		in = new BufferedInputStream(socket.getInputStream());				//creating input stream
		rawOut.write((input).getBytes());
		rawOut.flush();														//sending file name to get

		System.out.println("Where to save on system: (example: D:\\\\example.txt)");
		String saveTo = sc.next();											//path of where to save the file gotten
		FileOutputStream fos = new FileOutputStream(new File(saveTo));
		int c=0;
		while ((c = in.read()) != -1) {
			fos.write(c);													//writing the file
		}
		fos.close();

	}
	public static String sendFileName(String input) throws IOException, InterruptedException {

		rawOut.write(input.getBytes());	
		rawOut.flush();														//send file name to be stored

		System.out.println("waiting response");
		Bin =new BufferedReader(
				new InputStreamReader(socket.getInputStream()));			//creating input stream
		return Bin.readLine();												//return server response

	}
	public static String sendFile(String input,String host,int port) throws IOException, InterruptedException {

		socket = (SSLSocket)factory.createSocket(host, port);
		socket.startHandshake();											//establishing the TLS connection to server
		System.out.println("Handshake done");

		rawOut = socket.getOutputStream();									//creating output stream

		in = new BufferedInputStream(socket.getInputStream());				//creating input stream

		try {
			rawOut.write(getFileBytes(input));								//fill output stream with file bytes
			rawOut.flush();													//send file to be stored bytes
			rawOut.close();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		return "success";

	}
	public static byte[] getFileBytes(String path)throws IOException
	{	
		System.out.println(path);
		File f = new File(path);											//connection to file on system
		int length = (int)(f.length());
		if (length == 0) {
			throw new IOException("File length is zero: " + path);
		} else {
			FileInputStream fin = new FileInputStream(f);
			BufferedInputStream in = new BufferedInputStream(fin);

			byte[] bytecodes = new byte[length];							//byte array of data read from file
			int bytesRead=0;
			while ((bytesRead = in.read(bytecodes)) != -1) {				//writing to bytescodes
			}
			fin.close();
			in.close();
			return bytecodes;
		}
	}
}

