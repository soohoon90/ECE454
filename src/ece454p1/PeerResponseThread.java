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
import java.util.ArrayList;
import java.util.Collections;
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
							System.out.println(">> PeerResponse: inserting " + filename + "...");
							FileManager.parseFileChunkString(filename);
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
							String linee = br.readLine();
							FileManager.parseAllFileChunkString(linee);
							PrintStream ps = new PrintStream(peerSocket.getOutputStream());
							ps.println(FileManager.getAllFileChunkString());
						}
					}
					for (PeerList.PeerInfo pi : Peer.peerList.peers){
						System.out.println(">> \t"+pi.host+":"+pi.port+" is "+ (pi.connected ? "online" : "offline"));
					}
				}else if(line.equals("chunk")){
					System.out.println(">> PeerResponse: chunk request from " + fromHost);
					String fileName = br.readLine();
					int cn = Integer.parseInt(fileName.split(",")[1]);
					fileName = fileName.split(",")[0];
					OutputStream out = peerSocket.getOutputStream();
					PrintStream ps = new PrintStream(out);
					
					System.out.println(">> PeerResponse: We "+ (FileManager.list.get(fileName).get(cn) ? "have " : " don't have ") + fileName+","+cn);
					byte[] fileData = FileManager.fetchFileChunkData(fileName, cn);
					if (FileManager.list.get(fileName).get(cn)){
//						try {
//							Thread.currentThread().sleep(5000);
//						} catch (InterruptedException e) {
//						}
//						ps.println(fileData);
						out.write(fileData);
					}else{
						// don't have it!
						out.write(0);
//						ps.println("");
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
	}
}
