package ece454p1;

import java.util.*;
import java.util.Map.Entry;
import java.net.*;
import java.io.*;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";

		System.out.println("This ECE454 Project 1");
		System.out.println("Distributed File Replicator");

		System.out.print("Please specify Peers file: ");
		try {
			input = br.readLine();
		} catch (IOException e1) {
		}
		PeerList peerList = new PeerList(input);
		try{
			int i = Integer.parseInt(input);
			peerList = new PeerList("ece454p1/peers"+i+".txt");
		} catch(Exception e){}
		System.out.println("\tIP:"+peerList.myHost+"\tPORT:"+peerList.myPort+" <=me");
		if (peerList.peers.size() > 0){
	    	  for(PeerList.PeerInfo peerInfo : peerList.peers){
		    	  System.out.println("\tIP:"+peerInfo.host+"\tPORT:"+peerInfo.port);
	    	  }
		}else{
	    	  System.out.println("no peer recongized!");
	    	  System.exit(1);
		}

		String hostAddress = null;
		
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostAddress = addr.getHostAddress();
		} catch (UnknownHostException e) {
		}
		
		System.out.println("Creating Peer...");
		Peer peer = new Peer(peerList);
		while(true){
	    	  System.out.print("COMMANDS (insert, query, join, leave) PROMPT> ");
			try {
			   input = br.readLine();
			} catch (IOException ioe) {
			}
			if (input.toLowerCase().equals("show")){
				for(PeerList.PeerInfo pi : Peer.peerList.peers){
					System.out.println(pi.host+":"+pi.port+" is "+(pi.connected ? "online" : "offline"));
				}
			}else if (input.toLowerCase().equals("set")){
		    	  System.out.println("Change myPort to...");
		    	  String input2 = "";
		    	  try {
		    		  input2 = br.readLine();
		    		  Peer.peerList.myPort = Integer.parseInt(input2);
		    	  } catch (IOException ioe) {
				  }		
			}else if (input.toLowerCase().equals("insert")){
		    	  System.out.println("what is the path of the file you want to insert?");
		    	  String input2 = "";
		    	  try {
		    		  input2 = br.readLine();
			    	  System.out.println("Telling peer to insert "+input2);
			    	  peer.insert(input2);
		    	  } catch (IOException ioe) {
				  }
			}else if(input.toLowerCase().equals("query")){
		    	  System.out.println("Telling peer to query...");
		    	  Status status = new Status();
		    	  peer.query(status);
		    	  System.out.println("Status contains info of "+status.numberOfFiles()+" files");
		    	  for(Entry<String, Boolean> entry : FileManager.list.entrySet()){
		    		  System.out.println("\t"+entry.getKey()+(entry.getValue()?" local":" remote"));
		    	  }
			}else if(input.toLowerCase().equals("join")){
		    	  if(peer.join()== ReturnCodes.ERR_UNKNOWN_WARNING){
		    		  System.out.println("Peer is already connected. Leave first.");
		    	  }else{
		    		  System.out.println("Telling peer to join...");
		    	  }
			}else if(input.toLowerCase().equals("leave")){
		    	  if(peer.leave()== ReturnCodes.ERR_UNKNOWN_WARNING){
		    		  System.out.println("Peer is already not connected. Join first.");
		    	  }else{
		    		  System.out.println("Telling peer to leave...");
		    	  }
			}else if(input.toLowerCase().equals("exit")){
		    	  break;
			}else{
		    	  System.out.println("Invalid command!");
			}
		}
	}

}
