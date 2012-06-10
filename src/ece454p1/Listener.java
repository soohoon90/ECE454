package ece454p1;

import java.io.*;
import java.util.*;
import java.net.*;

public class Listener implements Runnable {
	
    private ServerSocket serverSocket;
	
	public synchronized void start() {
        if (serverSocket != null) {
            System.out.println("Server already started");
            return;
        }
		
		// Open server port
        try {
            serverSocket = new ServerSocket(Peer.localPort);
        } catch (IOException e) {
            System.out.println("Error creating server socket");
            return;
        }
		
        new Thread(this).start();
    }
	
    public synchronized void stop() {
        if (serverSocket == null) {
            System.out.println("Server already stopped");
            return;
        }
		
        // Close server port
        try {
            serverSocket.close();
        } catch (IOException e) {
			System.out.println("Error stopping server socket");
        }
		serverSocket = null;
    }
	
    public void run() {
		// Synchronously get our server socket
		ServerSocket ss = null;
		synchronized (this) {
			ss = serverSocket;
		}
		if (ss == null) {
			System.out.println("Server socket is null on startup");
			return;
		}
		
		System.out.println("Server started on port " + Integer.toString(ss.getLocalPort()));
		while (true) {
			// Wait for socket
			Socket s = null;
			try {
				s = ss.accept();
			} catch (IOException e) {
				break;
			}
			
			// Spawn new connection handler
			System.out.println("Accepted connection " + s.getLocalAddress().getHostAddress() + ":" + Integer.toString(s.getLocalPort()) + " <-> " + s.getInetAddress().getHostAddress() + ":" + Integer.toString(s.getPort()));
			
			new ResponseThread(s).start();
		}
		System.out.println("Server stopped");
    }
}
