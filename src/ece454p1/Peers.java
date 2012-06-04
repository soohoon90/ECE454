package ece454p1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Peers is a dumb container to hold the peers; the number of peers is fixed,
 * but needs to be set up when a peer starts up; feel free to use some other
 * container class, but that class must have a method that allows it to read the
 * peersFile, since otherwise you have no way of having a calling entity tell
 * your code what the peers are in the system.
 **/
public class Peers {

	/**
	 * The peersFile is the name of a file that contains a list of the peers Its
	 * format is as follows: in plaintext there are up to maxPeers lines, where
	 * each line is of the form: <IP address> <port number> This file should be
	 * available on every machine on which a peer is started, though you should
	 * exit gracefully if it is absent or incorrectly formatted. After execution
	 * of this method, the peers should be present.
	 * 
	 * @param peersFile
	 * @return
	 */
	public int initialize(String peersFile){
		BufferedReader in = null;
		
		try{
			in = new BufferedReader(new FileReader(peersFile));			
		}catch(IOException e){
			System.out.println("ERROR reading "+peersFile+" file");
			return -1;
		}
		
		ips = new ArrayList<String>();
		ports = new ArrayList<Integer>();
		
		if (in != null){
			String line;
			try {
				while( ( line = in.readLine() ) != null ){
					int spaceIndex = line.charAt(' ');
					if (spaceIndex > -1){				
						ips.add(line.substring(0, spaceIndex));
						ports.add(Integer.parseInt(line.substring(spaceIndex)));
					}
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
			}
		}
		
		System.out.println("Finished reading "+peersFile+" file:");
		for (String ip : ips){
			System.out.println(ip);
		}
		
		return 0;
	
	}

	public Peer getPeer(int i){
		if (i >= 0 && i < numPeers) return peers[i];
		return null;
	}

	//TODO You will likely want to add methods such as visit()
	public void visit() {
	}
	
	private int numPeers;
	private Peer[] peers;
	private ArrayList<String> ips;
	private ArrayList<Integer> ports;
}
