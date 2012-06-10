package ece454p1;

import java.io.*;
import java.net.*;

public class Server implements Runnable {
	Socket peerSocket;

	public Server(Socket s){
		peerSocket = s;
	}

	public void run() {
		System.out.println("Accepted connection " + this.toString());

		BufferedReader in = null;
		PrintStream out = null;
		try {
			in = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
			out = new PrintStream(peerSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error opening streams on " + this.toString());
			return;
		}
		
		try {
			String command = in.readLine();
			String ip = in.readLine();
			int port = Integer.parseInt(in.readLine());
			
			// Find corresponding proxy
			ProxyPeer proxy = null;
			for (ProxyPeer p : Peer.proxyPeerList) {
				if (p.host.getHostAddress().equals(ip) && p.port == port) {
					proxy = p;
					break;
				}
			}
			if (proxy == null) {
				System.out.println("Failed to match proxy on " + this.toString());
				return;
			}
			
			System.out.println("Responding to " + command + " from " + proxy.toString());
			if (command.equals("join")) {
				// Request
				String fileList = in.readLine();
				String chunkList = in.readLine();
				
				proxy.update();
			}if (command.equals("update")) {
				// Request
				String fileList = in.readLine();
				String chunkList = in.readLine();
				
				Peer.syncManager.parseFileList(fileList);
				Peer.syncManager.parseChunkList(proxy, chunkList);
			} else if(command.equals("chunk")) {
				// Request
				String chunkName = in.readLine();
				
				// Reply
				// OutputStrem is used instead of PrintStream because this is Byte[]
				// TODO: use syncManager readChunkData
				peerSocket.getOutputStream().write(Peer.syncManager.readChunkData(chunkName));
			} else if(command.equals("leave")){
				for (ProxyPeer p : Peer.proxyPeerList){
					if (p.host.getHostAddress().equals(ip) && p.port == port){
						System.out.println(">> PeerResponse: "+p.host+":"+p.port+" left.");
					}
				}
			} else if (command.equals("echo")) {
				System.out.println("echo from " + ip + ":" + Integer.toString(port));
			}
		} catch (IOException e) {
			System.out.println("Stream exception in connection " + this.toString());
			return;
		}
		
		System.out.println("Closed connection " + this.toString());
	}
	
	public String toString() {
		return peerSocket.getLocalAddress().getHostAddress() + ":" + Integer.toString(peerSocket.getLocalPort()) + " <-> " + peerSocket.getInetAddress().getHostAddress() + ":" + Integer.toString(peerSocket.getPort());
	}
}
