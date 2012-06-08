package ece454p1;

import java.io.*;
import java.util.*;
import java.net.*;

public class PeerServer implements Runnable {
	
	private ServerSocket ss;
	//private ArrayList<Handler> handlers = new ArrayList<Handler>();
	
	public void start() {
		if (ss != null) {
			System.out.println("Server already started");
			return;
		}
		
		try {
			ss = new ServerSocket(Main.localPort);
		} catch (IOException e) {
			System.out.println("Error creating server socket");
			return;
		}
		
		new Thread(this).start();
		
		System.out.println("Server started on port " + Integer.toString(ss.getLocalPort()));
	}
	
	public void stop() {
		if (ss == null) {
			System.out.println("Server already stopped");
			return;
		}
		
		// Close server port
		try {
			ss.close();
		} catch (IOException e) {
			
		}
		ss = null;
		
		// Close all currently handled connections
//		for (Handler handler : handlers) {
//			try {
//				handler.getSocket().close();
//			} catch (IOException e) {
//				System.out.println("Error closing handler socket");
//			}
//		}
		
		System.out.println("Server stopped");
	}
	
	public void run() {
		while (true) {
			// Wait for socket
			Socket s = null;
			try {
				s = ss.accept();
			} catch (IOException e) {
				System.out.println("Error accepting socket");
				break;
			}
			
			// Spawn new connection handler
			System.out.println("Accepted connection " + s.getLocalAddress().getHostAddress() + ":" + Integer.toString(s.getLocalPort()) + " <-> " + s.getInetAddress().getHostAddress() + ":" + Integer.toString(s.getPort()));
			
			Handler handler = new Handler(s);
//			synchronized (this) {
//				handlers.add(handler);
//			}
			new Thread(handler).start();
		}
	}
	
	private class Handler implements Runnable {
		private Socket socket;
		
		public Handler(Socket socket) {
			this.socket = socket;
		}
		
		public Socket getSocket() {
			return socket;
		}
		
//		private void unregisterFromServer() {
//			synchronized (PeerServer.this) {
//				handlers.remove(this);
//			}
//		}
		
		public void run() {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				System.out.println("Error getting socket stream");
				return;
			}
			
			String line = null;
			try {
				line = br.readLine();
			} catch (IOException e) {
				System.out.println("Error reading socket stream");
				return;
			}
			System.out.println(line);
			//this.unregisterFromServer();
		}
	}
}
