package ece454p1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class PeerList {

	public String myHost = "localhost";
	public int myPort = 8889;
	public ArrayList<String> hosts;
	public ArrayList<Integer> ports;
	
	public PeerList(String peersFile){
		BufferedReader in = null;
		
	    String hostAddress = null;
	    try {
			InetAddress addr = InetAddress.getLocalHost();
			hostAddress = addr.getHostAddress();
	    } catch (UnknownHostException e) {
	    }
		
		try{
			in = new BufferedReader(new FileReader(peersFile));			
		}catch(IOException e){
			System.err.println("ERROR reading "+peersFile+" file");
		}
		
		hosts = new ArrayList<String>();
		ports = new ArrayList<Integer>();
		
		if (in != null){
			String line;
			try {
				while((line = in.readLine()) != null ){
					int spaceIndex = line.indexOf(' ');
					if (spaceIndex > -1){
						String ip = line.substring(0, spaceIndex);
						int port = Integer.parseInt(line.substring(spaceIndex+1));
						if (ip.equals(hostAddress) == false){
							hosts.add(line.substring(0, spaceIndex));
							ports.add(port);
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
