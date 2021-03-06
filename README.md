# Encrypted-login-file-manager

A client and server architecture run on CMD, having a server that manages files according to encrypted user login credentials remotely accessed by a client.
This project is not final, it was a simple project for a computer security course where we were tasked to create a small application that implements security principals.
For the sake of simplicity and time constraints, the server and the client were implemented using the loopback interface despite the goal being a client communicating with an offsite server securly. SSL was configured using the loopback certificate.



--DETAILS:

I chose to implement the following security services among others:
• Authentication
• Access control
• Data confidentiality

Both username and password are hashed and stored in the "[directory]\files\database.txt" file, files of a user are stored under the name of the hashed username.
Usernames are hashed as to not trace back files to certain accounts, as well as adding security by requiring both a hashed password and a hashed username be compatible to allow access to user files.




--HOW TO RUN:

!! CMD must be at the directory of the java files!!

!! Testkey is the truststore containing the localhost certificate necessary for handshake, needs to be in same directory of codes!!

!! User Files and database will be saved at the path specified in CMD command!!

To compile Server:

	[Directory]\code>javac ClassServer.java
	
	[Directory]\code>javac FileServer.java

To initiate Server:	java FileServer [port] [files directory (eg: D:\project\files)] [Encryption (TLS)] [Authentication required (True/false)]

	[Directory]\code>java FileServer 2020 [Directory]\files TLS false

To compile Client:

	[Directory]\code>javac Client.java

To run the Client:	java Client [Server IP address] [port]

	[Directory]\code>java Client 127.0.0.1 2020
	
if client not working, try:

	[Directory]\code>start java -Djavax.net.ssl.trustStore=testkeys Client [Server IP address] [port]



--TO DEMONSTRATE SSL ENCRYPTION:

RawCap.exe a program to capture packets on loopback network, output file could be read using wireshark

link to RawCap for windows
https://www.netresec.com/?page=Blog&month=2020-01&post=RawCap-Redux

By analysing captured packets, it is apparent that the connection is encrypted as it cannot be understood as plaintext.
