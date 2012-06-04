package ece454p1;

import java.util.*;
import java.io.*;
import java.net.*;

public class TestServer {

	static int MY_PORT = 8889;
		
	public static void main(String [ ] args)
	{
		System.out.println("hello world! I am "+MY_PORT+", the server");
		
		ServerSocket server = null;
		try {
			server = new ServerSocket(MY_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} // 8888 is the port the server will listen on.
		
		int peerCount = 0;
		
		class PeerResponder implements Runnable{

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
				    System.out.println(id+": "+line);
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
		
		while(true){
			System.out.println("waiting for connections...");
			Socket fromPeer = null;
			try {
				fromPeer = server.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        System.out.println("connection was made at "+fromPeer.getPort()+"!");
	        System.out.println("creating new thread for connection at "+fromPeer.getPort()+"!");
	        
	        Thread t = new Thread(new PeerResponder(fromPeer, peerCount));
	        t.start();
	        
	        peerCount += 1;
		}
		        

	}

}
