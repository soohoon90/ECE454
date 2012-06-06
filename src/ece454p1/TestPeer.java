package ece454p1;

import java.util.*;
import java.io.*;
import java.net.*;

public class TestPeer {
		
	public static void main(String [ ] args)
	{
		int ports[] = {8889, 8890, 8891, 8892, 8893};
		int connected[] = {0,0,0,0,0};
		String peersFile = "ece454p1/peers.txt";
		try {
		    InetAddress addr = InetAddress.getLocalHost();
		    String hostAddress = addr.getHostAddress();
		    System.out.println("myHostAddress:" + hostAddress);
		} catch (UnknownHostException e) {
		}
		
		System.out.println("Deciding which port to use:");
		ServerSocket sSocket = null;
		for( int port : ports){
			System.out.println("checking if port " + port + " is open...");
			try{
				sSocket = new ServerSocket(port);
				break;
			}catch(IOException e){
				System.out.println("port " + port + " is already in use!");
			}
		}
		
		if (sSocket == null){
			System.out.println("all the ports are in use!");
			System.exit(1);
		}

		System.out.println("Listening on port "+ sSocket.getLocalPort());
	
		int peerCount = 0;
		
		class PeerConnector implements Runnable{
			int port;
			int myport;
			Socket peerSocket;
			
			public PeerConnector(int p, int m){
				port = p;
				myport = m;
			}
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					peerSocket = new Socket("localhost", port);
				}catch(IOException e){
					System.out.println(myport+": Couldn't connect to port " + port);
					return;
				}
				
				System.out.println(myport+": Connected to "+port);

				InputStream in = null;
				try {
					in = peerSocket.getInputStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				
				OutputStream out = null;
				try {
					out = peerSocket.getOutputStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				
				PrintStream ps = new PrintStream(out, true); // Second param: auto-flush on write = true
				while(true){
					Random r = new Random();
					int n = r.nextInt(50);
				    System.out.println(myport+": Sending ACK "+ n +"to "+port);
				    ps.println("ACK " +n);
				    try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		
		class PeerResponder implements Runnable{
			int id;
			Socket peerSocket;
			
			public PeerResponder(Socket s, int n){
				peerSocket = s;
				id = n;
				System.out.println(id+": Starting...");
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
				System.out.println(id+": Connection lost");
				
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
		
		// launch connections to all the other peers.
		// in case of time out, just quit
		// when they connect to you, launch this peer again
		// should be similar to join()
		for (int port : ports){
			if (port != sSocket.getLocalPort()){
				Thread t = new Thread(new PeerConnector(port, sSocket.getLocalPort()));
				t.start();
			}
		}		
		
		while(true){
			System.out.println("waiting for connections...");
			Socket fromPeer = null;
			try {
				fromPeer = sSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        System.out.println("Connection was made at "+fromPeer.getPort()+"!");
	        System.out.println("Creating new thread for connection at "+fromPeer.getPort()+"!");
	        
	        Thread t = new Thread(new PeerResponder(fromPeer, peerCount));
	        t.start();
	        
	        peerCount += 1;
		}
	}

}
