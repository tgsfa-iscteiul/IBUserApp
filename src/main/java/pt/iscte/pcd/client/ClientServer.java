package pt.iscte.pcd.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientServer extends Thread {

	private Socket socket;
	private ServerSocket serverSocket;
	private int port;
	private BlockThreadPool threadPool;

	public ClientServer(int port) {
		this.port = port;
		threadPool = new BlockThreadPool(5);
	}

	@Override
	public void run() {  //TODO rever os try-catch
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Lancou ServerSocket de cliente: " + serverSocket);
		try {
			while (true) {
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Conexao aceite de client na socket: " + socket);
				new ClientDealer(socket, threadPool).start();
			}
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
