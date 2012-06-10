package ece454p1;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class SyncThread extends Thread{
	public Socket peerSocket;
	public boolean running;

	public SyncThread(){
		running = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {	
		System.out.println(">> PeerSyncThread: joining...");
		for(ProxyPeer p : Peer.proxyPeerList){
			Socket s;
			try {
				s = new Socket(p.host, p.port);
				p.connected = true;
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintStream ps = new PrintStream(s.getOutputStream());
				ps.println("join");
				ps.println(Peer.localAddress.getHostAddress());
				ps.println(Peer.localPort);
				s.close();
			} catch (UnknownHostException e) {
				p.connected = false;
			} catch (IOException e) {
				p.connected = false;
			}
		}

		for(ProxyPeer p : Peer.proxyPeerList){
			Socket s;
			try {
				s = new Socket(p.host, p.port);
				p.connected = true;
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintStream ps = new PrintStream(s.getOutputStream());
				ps.println("update");
				ps.println(Peer.localAddress.getHostAddress());
				ps.println(Peer.localPort);
				ps.println(Peer.syncManager.getFileList());
				ps.println(Peer.syncManager.getChunkList());
				// TODO: use FileManager's parseFileList() and parseChunkList();
				String ip = br.readLine();
				int port = Integer.parseInt(br.readLine());
				Peer.syncManager.parseFileList(br.readLine());
				Peer.syncManager.parseChunkList(ip, port, br.readLine());
				s.close();
			} catch (UnknownHostException e) {
				p.connected = false;
			} catch (IOException e) {
				p.connected = false;
			}	
		}

		int i = 0;
		while(running){
			if (running == false){
				break;
			}else{
				i++;
			}

			// TODO: pick a new chunk to request
			String chunkToBeRequested = Peer.syncManager.getChunkToBeRequested();
//			String chunkToBeRequested = null;
			// if request it to connected peers in PeerList
			if (chunkToBeRequested != null){
				String chunkData = "";
				int attempts = 0;

				while(attempts < Peer.proxyPeerList.size()){
					Random r = new Random();
					ProxyPeer p  = Peer.proxyPeerList.get(r.nextInt(Peer.proxyPeerList.size()));
					if (p.connected == true && p.chunks.contains(chunkToBeRequested)){
						attempts++;
						Socket s;
						try {
							s = new Socket(p.host, p.port);
							p.connected = true;
							PrintStream ps = new PrintStream(s.getOutputStream());
							ps.println("chunk");
							ps.println(Peer.localAddress.getHostAddress());
							ps.println(Peer.localPort);
							ps.println(chunkToBeRequested);
							//s.setSoTimeout(0);
							BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
							int chunkSize = Integer.parseInt(br.readLine());
							byte[] b = new byte[chunkSize];
							
							InputStream in = s.getInputStream();
							
							int bytesRead;
							int current = 0;
							do {
								bytesRead = in.read(b, current, (b.length-current));
								if (bytesRead >= 0) current += bytesRead;
							}while (bytesRead >= 0);
							
							in.close();
							br.close();
														
							Peer.syncManager.writeChunkData(chunkToBeRequested, b);
							s.close();
							break;
						} catch (UnknownHostException e) {
							p.connected = false;
						} catch (IOException e) {
							p.connected = false;
						}
					}					
				}
			}else{
				try {
					for(ProxyPeer p : Peer.proxyPeerList){
						Socket s;
						try {
							s = new Socket(p.host, p.port);
							p.connected = true;
							BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
							PrintStream ps = new PrintStream(s.getOutputStream());
							ps.println("update");
							ps.println(Peer.localAddress.getHostAddress());
							ps.println(Peer.localPort);
							ps.println(Peer.syncManager.getFileList());
							ps.println(Peer.syncManager.getChunkList());
							// TODO: use FileManager's parseFileList() and parseChunkList();
							String ip = br.readLine();
							int port = Integer.parseInt(br.readLine());
							Peer.syncManager.parseFileList(br.readLine());
							Peer.syncManager.parseChunkList(ip, port, br.readLine());
							s.close();
						} catch (UnknownHostException e) {
							p.connected = false;
						} catch (IOException e) {
							p.connected = false;
						}	
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}		

		System.out.println(">> PeerSyncThread: leaving...");
		for(ProxyPeer p : Peer.proxyPeerList){
			if (p.connected == true){
				try {
					Socket s = new Socket(p.host, p.port);
					PrintStream ps = new PrintStream(s.getOutputStream());
					ps.println("leave");
					ps.println(Peer.localAddress.getHostAddress());
					ps.println(Peer.localPort);
					s.close();
				} catch (Exception e) {
				}
			}
		}
		System.out.println(">> PeerClient: bye bye");		
	}
}

