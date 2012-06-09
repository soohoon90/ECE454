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
				if(line.equals("update")){
					// update request comes will file listing and chunk listing
					

				}else if(line.equals("chunk")){
					// chunk request has the format of:
					// 		ip
					// 		port
					// 		chunkid
					String ip = br.readLine();
					int port = Integer.parseInt(br.readLine());
					String chunkID = br.readLine();
					
					// debug
					System.out.println(">> PeerResponse: chunk request from " + ip+":"+port+" for "+chunkID);
					
					PrintStream ps = new PrintStream(peerSocket.getOutputStream());
//					out.write(FileManager.fetchFileChunkData(fileName, cn));
				}else if(line.equals("leave")){
					// leave request has the format of:
					// ip\nport
					String ip = br.readLine();
					int port = Integer.parseInt(br.readLine());
					for (ProxyPeer p : Peer.proxyPeerList){
						if (p.host.getHostAddress().equals(ip) && p.port == port){
							System.out.println(">> PeerResponse: "+p.host+":"+p.port+" left.");
							p.connected = false;
						}
					}
				}
			}
		} catch (IOException e) {
		}
	}
}
