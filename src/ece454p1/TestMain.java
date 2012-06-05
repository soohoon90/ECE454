package ece454p1;

import java.util.*;
import java.net.*;
import java.io.*;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	      System.out.println("This ECE454 Project 1");
	      System.out.println("Distributed File Replicator");
	      System.out.println("Attempting to read the Peer list...");
	      PeerList peerList = new PeerList("src/ece454p1/peers.txt");
	      
	      System.out.println("\tIP:"+peerList.myHost+"\tPORT:"+peerList.myPort+" <=me");
	      if (peerList.hosts.size() > 0){
	    	  for(int i=0; i < peerList.hosts.size(); i++){
		    	  System.out.println("\tIP:"+peerList.hosts.get(i)+"\tPORT:"+peerList.ports.get(i));
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
	      
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	      String input = "";
	      ArrayList<String> commands = new ArrayList<String>();
	      while(true){
	    	  
	    	  System.out.println("Available commands: insert, query, join, leave, exit");
	    	  System.out.print("COMMAND PROMPT> ");
	    	  
		      try {
		         input = br.readLine();
		      } catch (IOException ioe) {
		      }
		      if (input.toLowerCase().equals("insert")){
		    	  System.out.println("what is the path of the file you want to insert?");
		    	  String input2 = "";
		    	  try {
			         input2 = br.readLine();
			      } catch (IOException ioe) {
				  }
		    	  System.out.println("Telling peer to insert "+input2);
		    	  peer.insert(input2);
		      }else if(input.toLowerCase().equals("query")){
		    	  System.out.println("Telling peer to query...");
		    	  Status status = new Status();
		    	  peer.query(status);
		    	  System.out.println("Status contains info of "+status.numberOfFiles()+" files");
		      }else if(input.toLowerCase().equals("join")){
		    	  System.out.println("Telling peer to join...");
		    	  if(peer.join()== ReturnCodes.ERR_UNKNOWN_WARNING){
		    		  System.out.println("Peer is already connected. Leave first.");
		    	  }
		      }else if(input.toLowerCase().equals("leave")){
		    	  System.out.println("Telling peer to leave...");
		    	  if(peer.leave()== ReturnCodes.ERR_UNKNOWN_WARNING){
		    		  System.out.println("Peer is already not connected. Leave first.");
		    	  }
		      }else if(input.toLowerCase().equals("exit")){
		    	  break;
		      }else{
		    	  System.out.println("Invalid command!");
		      }
	      }
	}

}
