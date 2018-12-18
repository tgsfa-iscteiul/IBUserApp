package pt.iscte.pcd.iscte_bay.user_app;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pt.iscte.pcd.iscte_bay.user_app.ui.GUI;

/**
 *  Classe para dar inicio à aplicação cliente, começando pelo GUI
 *  
 * @author tomas
 *
 */
public class MainUserApp {

	
	public static void main(String[] args){
		try {
			GUI ig = new GUI(InetAddress.getByName(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]) , args[3] );
			ig.open();
		} catch (NumberFormatException | UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	
}
