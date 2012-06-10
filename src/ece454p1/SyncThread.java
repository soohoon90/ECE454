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
				ps.close();
				br.close();
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
				Peer.syncManager.parseChunkList(p, br.readLine());
				ps.close();
				br.close();
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

			String chunkToBeRequested = Peer.syncManager.getChunkToBeRequested();
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
							InputStream is = s.getInputStream();
//							BufferedReader br = new BufferedReader(new InputStreamReader(is));
//							int chunkSize = Integer.parseInt(br.readLine());
							
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							
							byte b[] = new byte[Config.CHUNK_SIZE];
							for(int ss; (ss=is.read(b)) != -1; )
							{
								baos.write(b, 0, ss);
							}
							is.close();

							byte result[] = baos.toByteArray();
							System.out.println(result.length +"bytes!" + Config.CHUNK_SIZE);
							Peer.syncManager.writeChunkData(chunkToBeRequested, result);
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
							Peer.syncManager.parseChunkList(p, br.readLine());
							s.close();
						} catch (UnknownHostException e) {
							p.connected = false;
						} catch (IOException e) {
							p.connected = false;
						}	
					}
					Thread.sleep(5000);
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

