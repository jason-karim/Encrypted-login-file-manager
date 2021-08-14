import java.net.*;            
import java.io.*;

public class Server
{
	public static void main(String args[]) throws Exception
	{
		while(true) {
			ServerSocket sersock21 = new ServerSocket(7000);											//Creates a socket server that waits for a client to connect

			Socket sock2 = sersock21.accept();															//listens for a connection to be made to the server

			OutputStream outstream = sock2.getOutputStream();              								//outputs to the socket, in other words this class is used to send a message to the client

			BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(outstream));					//through the buffered writer we can send a string through the DataOutputStream to the socket to the client

			bw1.write("What Unit (\"rad\" or \"deg\")? then the expression (\"cos#\" or \"sin#\")");	//sending the message to the client

			System.out.println("message sent");

			bw1.close(); sersock21.close();

			ServerSocket sersock12 = new ServerSocket(5000);
			Socket sock12 = sersock12.accept();

			InputStream instream = sock12.getInputStream();												//creating an InputStream to receive the request 
			BufferedReader dstream1 = new BufferedReader(new InputStreamReader(instream));				//Through the BufferedReader we can receive data (a String)

			String unit = dstream1.readLine();

			System.out.println(unit);

			sersock12.close(); sock12.close();

			ServerSocket sersock13 = new ServerSocket(6000);											//I created another socket because I couldn't get the code to run on one or two sockets, I would get a "Socket closed" error
			Socket sock13 = sersock13.accept();

			InputStream instream2 = sock13.getInputStream();
			BufferedReader dstream2 = new BufferedReader(new InputStreamReader(instream2));

			String message = dstream2.readLine();

			System.out.println(message);

			sersock13.close(); sock13.close();

			ServerSocket sersock22 = new ServerSocket(7000);											//again same as before, sending the answer to the client's request

			Socket sock22 = sersock22.accept();

			OutputStream outstream2 = sock22.getOutputStream();

			BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(outstream2));

			String ans= "error check input";
			try {
				ans=GetAnswer(unit,message);
				bw2.write(ans);
			}
			catch(StringIndexOutOfBoundsException e) {
				bw2.write(ans);
			}
			catch(NumberFormatException i) 
			{
				bw2.write(ans);
			}


			System.out.println("Answer sent");

			bw2.close();

			sersock22.close(); sock22.close();

		}
	}

	public static String GetAnswer(String u,String e) {													//Method that processes the client's request
		if (u.equals("rad"))
		{
			if(e.substring(0,3).equals("cos"))
			{
				return Double.toString(Math.cos(Double.valueOf((String) e.substring(3,e.length()))));
			}
			else
			{
				return Double.toString(Math.sin(Double.valueOf((String) e.substring(3,e.length()))));
			}
		}
		else
		{
			if(e.substring(0,3).equals("cos"))
			{
				return Double.toString(Math.cos(Math.toRadians(Double.valueOf((String) e.substring(3,e.length())))));
			}
			else
			{
				return Double.toString(Math.sin(Math.toRadians(Double.valueOf((String) e.substring(3,e.length())))));
			}
		}
	}
}