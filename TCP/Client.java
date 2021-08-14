//same comments as in server.java

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client
{
	public static void main(String args[]) throws Exception
	{
		while(true) {

			Socket sock2=null;
			try {
				sock2 = new Socket("127.0.0.1", 7000);										//A simple client socket that tries to connect to the server at 127.0.0.1 on port 7000
			}catch(ConnectException i) {
				System.out.println("Couldn't connect to server, check that the sever is running then run the client again");
				return;
			}
			InputStream istream = sock2.getInputStream();
			BufferedReader br1 = new BufferedReader(new InputStreamReader(istream));

			System.out.println("server is ready");

			String s1 = br1.readLine();

			System.out.println(s1);

			// br1.close();    istream.close();   sock2.close( );

			br1.close(); sock2.close();

			Scanner u=new Scanner(System.in);
			String unit = u.next() ;

			Socket sock12 = new Socket("127.0.0.1",5000);

			OutputStream ostream = sock12.getOutputStream();
			DataOutputStream dosUnit = new DataOutputStream(ostream);
			dosUnit.writeBytes(unit);
			dosUnit.close();

			Scanner m=new Scanner(System.in);
			String message = m.next() ;

			sock12.close();

			Socket sock13 = new Socket("127.0.0.1",6000);

			OutputStream ostream2 = sock13.getOutputStream();
			DataOutputStream dosMessage = new DataOutputStream(ostream2);
			dosMessage.writeBytes(message);
			sock13.close();

			Socket sock22 = new Socket("127.0.0.1",7000);
			InputStream istream2 = sock22.getInputStream();

			BufferedReader br2 = new BufferedReader(new InputStreamReader(istream2));

			String Ans = br2.readLine();
			sock22.close();

			System.out.println(Ans);


			br2.close();    istream.close();
			//ostream.close(); m.close(); u.close();
		}

	}

}