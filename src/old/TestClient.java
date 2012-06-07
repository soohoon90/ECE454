package old;

import java.util.*;
import java.io.*;
import java.net.*;

public class TestClient {

	static int PEER_PORT = 8889;
	
	public static void main(String [ ] args) throws Exception
	{
		System.out.println("hello world! I am the client");
		
		System.out.println("trying to connect to "+PEER_PORT);
		Socket toPeer = null;
		while (toPeer == null){
			try {
				toPeer = new Socket("localhost", PEER_PORT);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
			} // First param: server-address, Second: the port
		}
		
		System.out.println("connected to "+PEER_PORT);

		OutputStream out = null;
		try {
			out = toPeer.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// PrintStream ps = new PrintStream(out, true); // Second param: auto-flush on write = true
		ObjectOutputStream  oos = new ObjectOutputStream(out);
		while(true){
			HashMap<String,String> dictionary = new HashMap<String,String>();
			
			// TODO: first, send ACK to new client
			
			// TODO: pick a new chunk to request
			
			// TODO: then send request
			dictionary.put("type","request");
			dictionary.put("body","CHUNK_ID");
			oos.writeObject(dictionary);
			
			// TODO: wait to receive 
			
			
			
		}
	}
}
