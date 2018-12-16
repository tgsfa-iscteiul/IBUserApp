package pt.iscte.pcd.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnector {
	
	private Socket socket;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	private boolean available;
	
	public ClientConnector(Socket s, ObjectInputStream in, ObjectOutputStream out){
		socket = s;
		inStream = in;
		outStream = out;
		available = true;
	}
	
	
	
	public Socket getSocket(){
		return socket;
	}

	public ObjectInputStream getInputStream() {
		return inStream;
	}

	public ObjectOutputStream getOutputStream() {
		return outStream;
	}
	
	public boolean isAvailable() {
		return available;
	}
	
	public void setAvailabe(boolean b) {
		available = b;
	}
	

}
