package ece454p1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class PeerResponder implements Runnable{

	int id;
	Socket peerSocket;
	
	public PeerResponder(Socket s, int n){
		peerSocket = s;
		id = n;
		System.out.println(id+": starting...");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		InputStream in = null;
		try {
			in = peerSocket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while(peerSocket.isConnected()){
			try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (line == null) break;
		    System.out.println(id+": received something:");
			System.out.println(line); // Prints "Hello, Other side of the connection!", in this example (if this would be the other side of the connection.	
		}
		System.out.println(id+": connection lost");
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
		}
		try {
			peerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
		}
		System.out.println(id+": bye bye");
	}
}
