package ece454p1;

import java.io.IOException;
import java.net.*;

public class PeerServer implements Runnable{

	ServerSocket sSocket;
	int myPort;
	
	public PeerServer(int p) {
		myPort = p;
		try {
			sSocket = new ServerSocket(myPort);
		} catch (IOException e) {
			System.out.println("EER! Port "+myPort+" is not Available!");
			System.exit(1);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			Socket fromPeer = null;
			try {
				fromPeer = sSocket.accept();
			} catch (IOException e) {
			}

	        System.out.println("Connection was made at "+fromPeer.getPort()+"!");
	        System.out.println("Creating new thread for connection at "+fromPeer.getPort()+"!");
	        
	        Thread t = new Thread(new PeerResponder(fromPeer));
	        t.start();
		}
	}

}
