package pt.iscte.pcd.client;

import java.io.Serializable;

public class WordSearchMessage implements Serializable {

	private String keyWord;
	
	
	
	
	public WordSearchMessage(String keyWord){
		this.keyWord = keyWord;
	}
	
	
	
	
	
	public String getKeyWord(){
		return keyWord;
	}
	
	
	
}
