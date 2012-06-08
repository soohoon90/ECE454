package ece454p1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class PeerSyncThread extends Thread{
	public Socket peerSocket;
	public Queue<HashMap<String,String>> msgList;
	public boolean running;

	public PeerSyncThread(){
		running = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {	
		System.out.println(">> PeerClient: started running");

		System.out.println(">> PeerSyncThread: joining...");
		for(PeerList.PeerInfo p : Peer.peerList.peers){
			System.out.println(">> PeerSyncThread: sending signal to... "+p.host);
			System.out.println(">> PeerSyncThread: "+FileManager.getAllFileChunkString());
			Socket s;
			try {
				s = new Socket(p.host, p.port);
				p.connected = true;
				PrintStream ps = new PrintStream(s.getOutputStream());
				ps.println("join");
				ps.println(FileManager.getAllFileChunkString());				
				// the Peer will send file+chunk list in form of
				// FN,C1,C2,C3,C4,C5,C6;FN2,C1,C2;FN3,C1
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				FileManager.parseAllFileChunkString(br.readLine());
				s.close();
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
		}

		System.out.println(">> STATUS:");
		for (PeerList.PeerInfo pi : Peer.peerList.peers){
			System.out.println(">> \t"+pi.host+":"+pi.port+" is "+ (pi.connected ? "online" : "offline"));
		}
		int i = 0;
		while(running){
			if (running == false){
				break;
			}else{
				i++;
			}

			// TODO: pick a new chunk to request
			String remoteChunk = FileManager.getFileNotLocal();
			// if request it to connected peers in PeerList
			if (remoteChunk.equals("") == false){
				String fn = remoteChunk.split(",")[0];
				int cn = Integer.parseInt(remoteChunk.split(",")[1]);
				String chunkData = "";
				for(PeerList.PeerInfo p : Peer.peerList.peers){
					if (p.connected == true){
						System.out.println(">> sending file (" + remoteChunk + ") request to... "+p.host+":"+p.port);
						Socket s;
						try {
							s = new Socket(p.host, p.port);
							p.connected = true;
							PrintStream ps = new PrintStream(s.getOutputStream());
							ps.println("chunk");
							ps.println(remoteChunk);
							InputStream in = s.getInputStream();
//							BufferedReader br = new BufferedReader(new InputStreamReader(in));
							byte[] b = new byte[Config.CHUNK_SIZE];
							in.read(b);
							if (b.equals(0) == false) FileManager.writeFileChunkData(fn, cn, b);
//							chunkData = br.readLine();
							s.close();
//							if (chunkData.equals("") == false){
							System.out.println(">> received file (" + fn + "," + cn + ") from "+p.host+":"+p.port);
							
							break;
//							}
						} catch (UnknownHostException e) {
						} catch (IOException e) {
						}
					}
				}
			}else{
				//System.out.println(">> no file to fetch... yawn...");
				try {
					Thread.currentThread().sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		

		System.out.println(">> PeerSyncThread: leaving...");
		for(PeerList.PeerInfo p : Peer.peerList.peers){
			if (p.connected == true){
				try {
					Socket s = new Socket(p.host, p.port);
					PrintStream ps = new PrintStream(s.getOutputStream());
					ps.println("leave");
				} catch (Exception e) {
				}
			}
		}
		System.out.println(">> PeerClient: bye bye");		
	}
}

