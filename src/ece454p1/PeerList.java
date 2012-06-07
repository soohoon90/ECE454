package ece454p1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class PeerList {

	class PeerInfo{
		String host;
		int port;
		Boolean connected;
		
		public PeerInfo(String h, Integer p, Boolean c){
			host = h;
			port = p;
			connected = c;
		}
	}
	
	public String myHost;
	public int myPort;
	public ArrayList<PeerInfo> peers;
	
	public PeerList(String peersFile){
		BufferedReader in = null;
		
	    String hostAddress = null;
	    try {
			InetAddress addr = InetAddress.getLocalHost();
			hostAddress = addr.getHostAddress();
			System.out.println(hostAddress);
	    } catch (UnknownHostException e) {
	    }
		
		try{
			in = new BufferedReader(new FileReader(peersFile));			
		}catch(IOException e){
			System.err.println("ERROR reading "+peersFile+" file");
		}
		
		peers = new ArrayList<PeerInfo>();
		
		if (in != null){
			String line;
			try {
				while((line = in.readLine()) != null ){
					int spaceIndex = line.indexOf(' ');
					if (spaceIndex > -1){
						String ip = line.substring(0, spaceIndex);
						int port = Integer.parseInt(line.substring(spaceIndex+1));
						if (ip.equals(hostAddress) == false || myHost != null){
							peers.add(new PeerInfo(line.substring(0, spaceIndex), port, false));
						}else{
							myHost = hostAddress;
							myPort = port;
						}
					}
				}
			} catch (NumberFormatException e) {
			} catch (IOException e) {
			} finally{
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}	
}
