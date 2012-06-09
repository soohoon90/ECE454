package ece454p1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ServerThread extends Thread{

	public ServerSocket sSocket;
	public int myPort;
	public boolean running;
	ArrayList<Thread> tList;
	
	public ServerThread(int p) {
		tList = new ArrayList<Thread>();
		running = true;
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
		while(running){
			Socket fromPeer = null;
//			System.out.println(">> PeerServer: listening at "+sSocket.getLocalPort());
			try {
				fromPeer = sSocket.accept();
			} catch (IOException e) {
			}			
			
//	        System.out.println(">> PeerServer: Spawning to handle new Request from "+fromPeer.getInetAddress()+":"+fromPeer.getPort()+"!");
	        Thread t = new ResponseThread(fromPeer);
	        tList.add(t);
	        t.start();
		}
		try{
			sSocket.close();
		}catch(IOException ie){
			System.out.println(">> PeerServer: Couldn't close server socket!");
		}
		System.out.println(">> PeerServer: bye bye");
	}
}
