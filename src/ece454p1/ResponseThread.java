package ece454p1;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
//					System.out.println(">> PeerResponse: update request from " + ip+":"+port);
					// update request comes will file listing and chunk listing
					Peer.syncManager.parseFileList(br.readLine());
					for (ProxyPeer p : Peer.proxyPeerList){
						if (p.host.getHostAddress().equals(ip) && p.port == port){
							Peer.syncManager.parseChunkList(p, br.readLine());
						}
					}
					// send update back
					ps.println(Peer.localAddress.getHostAddress());
					ps.println(Peer.localPort);
					ps.println(Peer.syncManager.getFileList());
					ps.println(Peer.syncManager.getChunkList());
				}else if(line.equals("chunk")){
					String chunkID = br.readLine();
					System.out.println(">> PeerResponse: chunk request from " + ip+":"+port+" for "+chunkID);					
					
					byte[] b = Peer.syncManager.readChunkData(chunkID);
					
					BufferedOutputStream bos = new BufferedOutputStream(peerSocket.getOutputStream());
//					ps.println(((b==null)?0:b.length));
					
					bos.write(b);
					bos.flush();
					bos.close();
				}else if(line.equals("join")){
					for (ProxyPeer p : Peer.proxyPeerList){
						if (p.host.getHostAddress().equals(ip) && p.port == port){
							System.out.println(">> PeerResponse: "+p.host.getHostName()+":"+p.port+" joined.");
							p.connected = true;
						}
					}
				}else if(line.equals("leave")){
					for (ProxyPeer p : Peer.proxyPeerList){
						if (p.host.getHostAddress().equals(ip) && p.port == port){
							System.out.println(">> PeerResponse: "+p.host.getHostName()+":"+p.port+" left.");
							p.connected = false;
							p.requests.clear();
						}
					}
				}else if(line.equals("testfile")){
					String filename = br.readLine();
					System.out.println(filename);
					
					int filelength = Integer.parseInt(br.readLine());
					byte[] b = new byte[filelength];
					FileOutputStream fos = new FileOutputStream(filename);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					InputStream is = peerSocket.getInputStream();
					
					int totalRead = 0;
					while (totalRead < filelength)
					{
						int read = is.read(b, totalRead, filelength-totalRead);
						totalRead += read;
					}
					
					is.close();
				    bos.write(b);
				    bos.flush();
				    bos.close();
				    fos.close();
				    peerSocket.close();
				    System.out.println(filelength);
					System.out.println("file "+filename+" received");
				}else{
					System.out.println(line);
					System.out.println(ip);
					System.out.println(port);
				}
			}
//			System.out.println(">> PeerResponse: responded to " + ip+":"+port);
		} catch (IOException e) {
		} finally{
			try {
				peerSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
