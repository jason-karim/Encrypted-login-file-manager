//This java class had been heavily modified, don't replace with original ClassServer.java
import java.io.*;
import java.net.*;


/**
 * Based on ClassServer.java in tutorial/rmi
 */
public abstract class ClassServer implements Runnable {

	private String docroot="";
	private static boolean logedIn=false;
	private static boolean writing=false;
	static Socket socket;
	private static OutputStream rawOut;
	private static BufferedReader in;
	private static String user="";
	private static String pass="";
	
	private static ServerSocket server = null;
	/**
	 * Constructs a ClassServer based on <b>ss</b> and
	 * obtains a file's bytecodes using the method <b>getBytes</b>.
	 *
	 */
	protected ClassServer(ServerSocket ss,String docroot)
	{
		server = ss;
		this.docroot=docroot;
		run();
	}

	/**
	 * Returns an array of bytes containing the bytes for
	 * the file represented by the argument <b>path</b>.
	 *
	 * @return the bytes for the file
	 * @exception FileNotFoundException if the file corresponding
	 * to <b>path</b> could not be loaded.
	 * @exception IOException if error occurs reading the class
	 */
	
	public byte[] getFileBytes(String path)throws IOException
	{
		System.out.println("@ getBytes reading: " + docroot + File.separator + path+"hon");
		System.out.println(docroot + "\\" + path);
		File f = new File(docroot + File.separator + path);					//connection to file on system
		int length = (int)(f.length());
		if (length == 0) {
			throw new IOException("File length is zero: " + path);
		} else {
			FileInputStream fin = new FileInputStream(f);
			BufferedInputStream in = new BufferedInputStream(fin);			//incoming data stream

			byte[] bytecodes = new byte[length];							//byte array of data read from file
			int bytesRead=0;
			while ((bytesRead = in.read(bytecodes)) != -1) {				//writing to bytescodes
			}
			fin.close();
			in.close();
			return bytecodes;
		}
	}
	public byte[] createDir(String user)throws IOException, FileNotFoundException //creates a directory
	{	
		File file = new File(docroot+"\\"+user);		//location + name of directory (user's username hash)
		if (!file.exists()) {
		      //Creating the directory
		      boolean bool = file.mkdir();	//checks if directory is already is available
		      if(bool){
		         System.out.println("Directory created successfully");
		      }else{
		         System.out.println("couldn’t create specified directory");
		      }
		}
		return null;
	}
	public String getDocRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	public void run()			//main functionality of the class CLassServer
	{
		while(true) {
			while(logedIn&&!writing) {
				try {
					getOrStore();	//if logged in, get or store a file or show directory function
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(!logedIn) {
				while(!logedIn) {
					logedIn=logOrReg();			//if not logged in, login or register
				};
				System.out.println("login success");
			}
		}
	}

	public byte[] sendFile(String path) throws FileNotFoundException, IOException {		//returns the requested file data in a byte array

		// retrieve bytecodes
		byte[] bytecodes = getFileBytes(user+"\\\\"+path);
		//send bytecodes in response
		return bytecodes;
	}
	public boolean logOrReg() {
		try {
			socket = server.accept();										//start handshake
		} catch (IOException e) {
			System.out.println("Class Server died: " + e.getMessage());
			e.printStackTrace();
		}

		try {
			rawOut = socket.getOutputStream();			//creating output stream

			try {
				BufferedReader in =
						new BufferedReader(				//creating input stream
								new InputStreamReader(socket.getInputStream()));
				String input= in.readLine();
				
				boolean status=false;					//status of login
				
				if(input.contains("login")) {
					status=login(input.substring(6));	//if the client requests to login
				}else if(input.contains("register")) {
					status=register(input.substring(9));	//if the client requests to register
				}
				byte[] bytecodes=null;
				if(status) {
					bytecodes = "success".getBytes();		//login or registration success reply
				}else {
					bytecodes = "error".getBytes();			//login or registration error reply
				}
				try {
					rawOut.write(bytecodes);				//inserting the byte array response into output stream
					rawOut.flush();							//sending response
				} catch (IOException ie) {
					ie.printStackTrace();
				}
				return status;

			} catch (Exception e) {
				e.printStackTrace();
				// write out error response
				rawOut.write("error @login()".getBytes());
				rawOut.flush();
			}

		} catch (IOException ex) {
			System.out.println("error writing response: " + ex.getMessage());
			ex.printStackTrace();
			return false;

		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
		return false;
	}

	public boolean login(String input) {
		getUserCred(input);								//gets the user and password hashes
		//byte[] bytecodes=sendFile(input);
		byte[] bytecodes=null;
		return checkCred();								//checks if the user and password credentials exists in database

	}
	public static void getUserCred(String input) {		//sets the user and the pass to their respective hash

		String line = input;
		user="";
		pass="";
		if (line.startsWith("user:")) {
			user=line.substring(5,line.indexOf("pass:")).trim();
			pass=line.substring(line.indexOf("pass:")+5, line.length()).trim();
		}else {
			System.out.println("error @getUserCred");
		}
		Hashing h=new Hashing();						//creating an instance of the Hashing class
		user=h.hash(user);								//returns the hash of user
		pass=h.hash(pass);								//returns the hash of pass
	}
	public boolean checkCred() {					//checks if the user and password credentials exists in database
		try {
			in = new BufferedReader(new FileReader(docroot+"\\\\Database.txt"));
			boolean status=false;						//login error until proven otherwise
			String line = in.readLine();
			String storedU="";
			String storedP="";
			System.out.println(line);
			while (line != null) {						//compares pass with stored pass, if the user exists				
				storedU=line.substring(5,line.indexOf("pass:")).trim();
				storedP=line.substring(line.indexOf("pass:")+5, line.length()).trim();
				if(user.equals(storedU)&&pass.equals(storedP)) {
					status=true;
					break;								//login success
				}
				line = in.readLine();
			}
			in.close();
			return status;								//returns login status
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error@checkCred()");
			return false;
		}		
	}
	public boolean register(String input) throws IOException {		//adds user and pass to database and creates Directory for user
		getUserCred(input);											//gets user and pass from string sent by client
		BufferedWriter writer = new BufferedWriter(new FileWriter(docroot+"\\\\Database.txt",true));	//appends user credentials to database
		writer.append("user:"+user+" pass:"+pass+"\r\n");
		writer.close();
		createDir(user);											//creates user directory
		return true;
	}
	public boolean writeFile(String name) throws IOException {		//writes a file sent by the client to the user's directory
		
		try {
			socket = server.accept();								//new connection (handshake, continuation of the session)
		} catch (IOException e) {
			System.out.println("Class Server died: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		try {
			rawOut = socket.getOutputStream();						//creating output stream

			try {
				BufferedInputStream in = new BufferedInputStream(socket.getInputStream());	//creating input stream
				//write file under name(name) in user directory(user) in server file directory(docroot)
				FileOutputStream fos = new FileOutputStream(docroot+"\\"+user+"\\"+name);
				int c=0;
				while ((c = in.read()) != -1) {
					fos.write(c);					//read bytes from input stream and write to file
				}
				fos.close();
				in.close();
				System.out.println("done");			//done writing file
				return true;			
				
			} catch (Exception e) {
				e.printStackTrace();
				// write out error response
				rawOut.write("error @run".getBytes());
				rawOut.flush();
				return false;
			}

		} catch (IOException ex) {
			System.out.println("error writing response: " + ex.getMessage());
			ex.printStackTrace();
			return false;

		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
	public byte[] getDir() {			//returns byte array of file names in user directory
		
		File file = new File(docroot+"\\"+user);
		String[] S=file.list();			//get directory as string array
		String Dir="";
		for(int i=0;i<S.length;i++) {	//Concatenate string array into one big string
			Dir+=S[i]+"\r";
		}
		byte[] b=Dir.getBytes();		//turn string to byte array
		return b;						//return byte array
		
	}
	
	public void getOrStore() throws IOException {	//get file, store file, or get directory
		// accept a connection
		try {
			socket = server.accept();				//new connection(resuming session)
		} catch (IOException e) {
			System.out.println("Class Server died: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		String input="";
		String name="";

		try {
			rawOut = socket.getOutputStream();		//creating output stream

			try {
				// get path to class file from header
				BufferedReader in =
						new BufferedReader(
								new InputStreamReader(socket.getInputStream()));		//creating input stream
				input= in.readLine();				//read client string request
				boolean status=false;
				byte[] bytecodes=null;
				
				if(input.contains("GET")) {			//if starts with "GET", get a file or directory
					if(input.contains("Dir")) {		//if continues with "Dir" send directory
						bytecodes=getDir();			//get directory in byte array
					}else {							//if not send file
						bytecodes=sendFile(input.substring(4));	//get file in byte array
					}
				}else if(input.contains("store")) {	//if starts with "store", store a file
					System.out.println("saving name");
					name=input.substring(6);		//get name for file to be saved as
					status=true;
					if(status) {
						bytecodes = "success".getBytes(); //successfully read file name 
					}else {
						bytecodes = "error".getBytes();
					}
				}
												
				try {
					rawOut.write(bytecodes);
					rawOut.flush();					//send response (file/directory/success or error response)
				} catch (IOException ie) {
					ie.printStackTrace();
					return;
				}

			} catch (Exception e) {
				e.printStackTrace();
				// write out error response
				rawOut.write("error @run".getBytes());
				rawOut.flush();
			}

		} catch (IOException ex) {
			System.out.println("error writing response: " + ex.getMessage());
			ex.printStackTrace();

		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
		
		if(input.contains("store")) writeFile(name);
		
	}
}
