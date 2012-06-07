package ece454p1;

import java.io.*;
import java.net.*;

public class PeerServer implements Runnable {
	
	private int port;
	private ServerSocket ss;
	
	public PeerServer(int port) {
		this.port = port;
	}
	
	public void start() {
		if (ss != null) {
			System.out.println("Server already started");
			return;
		}
		
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Error creating server socket");
			return;
		}
		
		Thread t = new Thread(this);
		t.start();
		
		System.out.println("Server started on port " + Integer.toString(port));
	}
	
	public void stop() {
		if (ss == null) {
			System.out.println("Server already stopped");
			return;
		}
		
		try {
			ss.close();
		} catch (IOException e) {
			
		}
		ss = null;
		
		System.out.println("Server stopped");
	}
	
	public void run() {
		while (true) {
			Socket s = null;
			try {
				s = ss.accept();
			} catch (IOException e) {
				System.out.println("Error accepting socket");
				break;
			}
		}
	}
}
