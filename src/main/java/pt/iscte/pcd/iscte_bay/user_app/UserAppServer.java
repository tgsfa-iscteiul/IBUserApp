package pt.iscte.pcd.iscte_bay.user_app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import pt.iscte.pcd.iscte_bay.user_app.thread.BlockThreadPool;

/**
 *  Comunicações com outros Utilizadores após estes terem perguntado ao directório 
 *  quais os clientes que estão ligados.
 *  
 *  São server sockets, a responder num port e sempre que outra UserApp pretende 
 *  saber que ficheiros existem ou solicitar a transferência de um ficheiros
 *  
 *  Sempre que um pedido de comunicação é efectuado à Server Socket, é lançada 
 *  uma nova thread na qual são dadas as respostas aquele utilizador sem bloquear
 *  outros pedidos de outros utilizadores que entretanto surgam.
 *   
 * @author tomas
 *
 */
public class UserAppServer extends Thread {

	private Socket socket;
	private ServerSocket serverSocket;
	private int port;
	private String folderName;
	private BlockThreadPool threadPool;

	public UserAppServer(int port, String folderName) {
		this.port = port;
		this.folderName = folderName;
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
				new ClientDealer(socket, threadPool, folderName).start();
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
