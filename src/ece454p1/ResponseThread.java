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

public class ResponseThread extends Thread{
	Socket peerSocket;

	public ResponseThread(Socket s){
		peerSocket = s;
	}

	@Override
	public void run() {

		String fromHost = peerSocket.getInetAddress().toString().substring(1);

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
			PrintStream ps = new PrintStream(peerSocket.getOutputStream());
			String line = br.readLine();
			String ip = br.readLine();
			int port = Integer.parseInt(br.readLine());
			if( line != null ){
				if(line.equals("update")){
					System.out.println(">> PeerResponse: update request from " + ip+":"+port);
					// update request comes will file listing and chunk listing
					Peer.syncManager.parseFileList(br.readLine());
					Peer.syncManager.parseChunkList(ip, port, br.readLine());
					// send update back
					ps.println(Peer.localAddress.getHostAddress());
					ps.println(Peer.localPort);
					ps.println(Peer.syncManager.getFileList());
					ps.println(Peer.syncManager.getChunkList());
				}else if(line.equals("chunk")){
					// chunk request has the format of:
					// 		ip
					// 		port
					// 		chunkid
					String chunkID = br.readLine();
					
					// debug
					System.out.println(">> PeerResponse: chunk request from " + ip+":"+port+" for "+chunkID);
					// OutputStrem is used instead of PrintStream because this is Byte[]
					// TODO: use syncManager readChunkData
					peerSocket.getOutputStream().write(Peer.syncManager.readChunkData(chunkID));
				}else if(line.equals("join")){
					for (ProxyPeer p : Peer.proxyPeerList){
						if (p.host.getHostAddress().equals(ip) && p.port == port){
							System.out.println(">> PeerResponse: "+p.host+":"+p.port+" joined.");
							p.connected = true;
						}
					}
				}else if(line.equals("leave")){
					for (ProxyPeer p : Peer.proxyPeerList){
						if (p.host.getHostAddress().equals(ip) && p.port == port){
							System.out.println(">> PeerResponse: "+p.host+":"+p.port+" left.");
							p.connected = false;
							p.requests.clear();
						}
					}
				}
			}
		} catch (IOException e) {
		}
	}
}
