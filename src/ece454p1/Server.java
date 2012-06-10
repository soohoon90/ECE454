package ece454p1;

import java.io.*;
import java.net.*;

public class Server implements Runnable {
	Socket socket;

	public Server(Socket s){
		socket = s;
	}

	public void run() {
		try {
			BufferedReader in = null;
			PrintStream out = null;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintStream(socket.getOutputStream());
			} catch (IOException e) {
				System.out.println("Error opening streams on " + this.toString());
				return;
			}
			
			ProxyPeer proxy = null;
			try {
				String command = in.readLine();
				String ip = in.readLine();
				int port = Integer.parseInt(in.readLine());
				
				// Find corresponding proxy
				for (ProxyPeer p : Peer.proxyPeerList) {
					if (p.getAddress().getHostAddress().equals(ip) && p.getPort() == port) {
						proxy = p;
						break;
					}
				}
				//System.out.println("Accepted connection from " + proxy.toString());
				
				if (proxy == null) {
					System.out.println("Failed to match proxy on " + this.toString());
					socket.close();
					return;
				}
				
				System.out.println("Responding to '" + command + "' from " + proxy.toString());
				if (command.equals("join")) {
					// Request
					String fileList = in.readLine();
					String chunkList = in.readLine();
					
					Peer.syncManager.parseFileList(fileList);
					Peer.syncManager.parseChunkList(proxy, chunkList);
					
					// Psuedo-reply
					proxy.update();
				} if (command.equals("update")) {
					// Request
					String fileList = in.readLine();
					String chunkList = in.readLine();
					
					Peer.syncManager.parseFileList(fileList);
					Peer.syncManager.parseChunkList(proxy, chunkList);
				} else if (command.equals("chunk")) {
					// Request
					String chunkName = in.readLine();
					
					// Reply
					byte[] data = Peer.syncManager.readChunkData(chunkName);
					out.write(data);
				} else if (command.equals("echo")) {
					System.out.println("echo from " + proxy.toString());
				} else {
					System.out.println("Unknown protocol command: " + command);
				}
				//System.out.println("Responded to '" + command + "' from " + proxy.toString());
			} catch (IOException e) {
				System.out.println("Stream exception in connection " + this.toString());
				return;
			}
			
			if (proxy != null) {
				//System.out.println("Closed connection from " + proxy.toString());
			}
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("Error closing socket on " + this.toString());
			}
		}
	}
	
	public String toString() {
		return socket.getLocalAddress().getHostAddress() + ":" + Integer.toString(socket.getLocalPort()) + " <-> " + socket.getInetAddress().getHostAddress() + ":" + Integer.toString(socket.getPort());
	}
}
