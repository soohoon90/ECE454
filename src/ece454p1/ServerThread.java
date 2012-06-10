package ece454p1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ServerThread extends Thread{

	public ServerSocket sSocket;
	public int myPort;
	ArrayList<Thread> tList;
	
	public ServerThread(int p) {
		tList = new ArrayList<Thread>();
		myPort = p;
		try {
			sSocket = new ServerSocket(myPort);
		} catch (IOException e) {
			System.out.println("EER! Port "+myPort+" is not Available!");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			Socket fromPeer = null;
//			System.out.println(">> PeerServer: listening at "+sSocket.getLocalPort());
			try {
				fromPeer = sSocket.accept();
			} catch (IOException e) {
				break;
			}			
			
//	        System.out.println(">> PeerServer: Spawning to handle new Request from "+fromPeer.getInetAddress()+":"+fromPeer.getPort()+"!");
	        Thread t = new ResponseThread(fromPeer);
	        tList.add(t);
	        t.start();
		}
	}
}
