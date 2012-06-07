package ece454p1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

public class PeerResponseThread extends Thread{
	Socket peerSocket;
	
	public PeerResponseThread(Socket s){
		peerSocket = s;
	}
	
	public static String implode(String[] ary, String delim) {
	    String out = "";
	    for(int i=0; i<ary.length; i++) {
	        if(i!=0) { out += delim; }
	        out += ary[i];
	    }
	    return out;
	}
	
	@Override
	public void run() {
		
		String fromHost = peerSocket.getInetAddress().toString().substring(1);
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
			String line = br.readLine();
			if( line != null ){
				if (line.equals("insert")){
					String filename = br.readLine();
					if (filename.length() > 0){
						System.out.println(">> PeerResponse: INSERT request for " + filename);
						if (FileManager.list.containsKey(filename) == false){
							System.out.println(">> PeerResponse: inserted!");
							FileManager.list.put(filename, false);
//							Peer.peerSyncThread.notifyAll();
						}else{
							System.out.println(">> PeerResponse: already exists!");
						}
					}
				}else if(line.equals("join")){
					System.out.println(">> PeerResponse: JOIN request from " + fromHost);
					for (PeerList.PeerInfo pi : Peer.peerList.peers){
						if (pi.host.equals(fromHost)){
							System.out.println(">> PeerResponse: "+pi.host+":"+pi.port+" joined.");
							pi.connected = true;
							for (String fn : br.readLine().split(",")){
								if(FileManager.list.containsKey(fn) == false){
									FileManager.list.put(fn, false);
//									Peer.peerSyncThread.notifyAll();
								}
							}
							PrintStream ps = new PrintStream(peerSocket.getOutputStream());
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
						}
					}
					for (PeerList.PeerInfo pi : Peer.peerList.peers){
						System.out.println(">> \t"+pi.host+":"+pi.port+" is "+ (pi.connected ? "online" : "offline"));
					}
				}else if(line.equals("leave")){
					for (PeerList.PeerInfo pi : Peer.peerList.peers){
						if (pi.host.equals(fromHost)){
							System.out.println(">> PeerResponse: "+pi.host+":"+pi.port+" left.");
							pi.connected = false;
						}
					}
					for (PeerList.PeerInfo pi : Peer.peerList.peers){
						System.out.println(">> \t"+pi.host+":"+pi.port+" is "+ (pi.connected ? "online" : "offline"));
					}
				}
			}
		} catch (IOException e) {
		}
		

//		ObjectOutputStream oout = null;
//		try {			
//			oout = new ObjectOutputStream (peerSocket.getOutputStream());
//			oout.flush();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		while(peerSocket.isConnected()){
//			HashMap<String, String> dictionary = new HashMap<String, String>();
//			// TODO: check if there is new file
//			
//			// TODO: wait for request
//			try {
//				ObjectInputStream oin = new ObjectInputStream (peerSocket.getInputStream());
//				if (oin.available() > 0){
//					dictionary = (HashMap<String, String>) oin.readObject();
//				}
//			} catch (IOException e1) {
//			} catch (ClassNotFoundException e) {
//			}
//			
//			if (dictionary.containsKey("type")){
//				if (dictionary.get("type").equals("ping")){
//					System.out.println(">> PeerResponder got "+dictionary.get("type")+dictionary.get("from")+dictionary.get("body"));
//					dictionary.put("type", "pong");	
//					dictionary.put("body", "body of a pong");
//					dictionary.put("from", peerSocket.getLocalAddress().toString());
//					// send back a response
//					try {
//						System.out.println(">> PeerResponder sending a rrr");
//						oout.writeObject(dictionary);
//					} catch (IOException e) {
//						System.out.println(">> PeerResponder: oout ERR");
//					}
//				}else if (dictionary.get("type").equals("die")){
//					break;
//				}else{
//					// TODO: invalid response
//				}
//			}else{
//			}
//		}
//		
//		System.out.println("Connection lost");
//		try {
//			peerSocket.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//		}
//		System.out.println("Bye bye");
	}
}
