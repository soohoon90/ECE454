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
		
//		ObjectOutputStream oout = null;
//		try {			
//			oout = new ObjectOutputStream (peerSocket.getOutputStream());
//			oout.flush();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		System.out.println(">> PeerSyncThread: joining...");
		for(PeerList.PeerInfo p : Peer.peerList.peers){
			System.out.println(">> sending signal to... "+p.host);
			Socket s;
			try {
				s = new Socket(p.host, p.port);
				p.connected = true;
				PrintStream ps = new PrintStream(s.getOutputStream());
				ps.println("join");
				String fileList = "";
				Set<String> fileSet = FileManager.list.keySet();
				System.out.println(">> PeerResponse: " +fileSet.size()+" files");
				if(fileSet.size() > 0){
					StringBuffer sb = new StringBuffer();
				    sb.append(fileSet.toArray()[0]);
				    for (int i=1;i<fileSet.toArray().length;i++) {
				        sb.append(",");
				        sb.append(fileSet.toArray()[i]);
				    }
				    fileList = sb.toString();
				    System.out.println(">> PeerResponse: "+fileList);
				}
				ps.println(fileList);
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String line = br.readLine();
				for (String fn : line.split(",")){
					if(FileManager.list.containsKey(fn) == false){
						FileManager.list.put(fn, false);
					}
				}
				s.close();
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
		}
		
		System.out.println(">> STATUS:");
		for (PeerList.PeerInfo pi : Peer.peerList.peers){
			System.out.println(">> \t"+pi.host+":"+pi.port+" is "+ (pi.connected ? "online" : "offline"));
		}
		
		while(running){
			if (running == false){
				break;
			}

			int countRemote = 0;
			for(Entry<String, Boolean> entry : FileManager.list.entrySet()){
				if(entry.getValue() == false) countRemote++;
			}
			
			if (countRemote == 0){
				try {
					Thread.currentThread();
				} catch (InterruptedException e) {
				}
			}
			
//			System.out.println(">> PeerClient: sending a new request...");
//			HashMap<String,String> dictionary = new HashMap<String,String>();
//			
//			// TODO: first, send ACK to new client
//			
//			// TODO: pick a new chunk to request
//			
//			// TODO: then send request
//			dictionary.put("type","ping");
//			dictionary.put("from",peerSocket.getLocalAddress().toString());
//			dictionary.put("body",peerSocket.getLocalAddress().toString());
//			try {
//				oout.writeObject(dictionary);
//				dictionary = null;
//			} catch (IOException e) {
//			}
//
//			// TODO: wait to receive
//			System.out.println(">> PeerClient: waiting for a response...");
//			try {
//				ObjectInputStream oin = new ObjectInputStream (peerSocket.getInputStream());
//				dictionary = (HashMap<String, String>) oin.readObject();
//				if (dictionary == null) {
//					System.out.println(">> PeerClient: received null response...");
//					break;
//				}
//			} catch (IOException e1) {
//			} catch (ClassNotFoundException e) {
//			}		
//			System.out.println(">> PeerClient: received a response");
//			System.out.println(dictionary.get("type"));
//			System.out.println(dictionary.get("from"));
//			System.out.println(dictionary.get("body"));
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

