package ece454p1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Peer and Status are the classes we really care about Peers is a container;
 * feel free to do a different container
 */
public class Peer {

	public static InetAddress localAddress;
	public static int localPort;
	public static State currentState;
	public static ArrayList<ProxyPeer> proxyPeerList;
	public static ServerThread serverThread;
	public static SyncThread syncThread;
	public static SyncManager syncManager;

	public Peer(ArrayList<ProxyPeer> ppl) {
		currentState = State.disconnected;
		proxyPeerList = ppl;
		syncManager = new SyncManager();
	}

	public int insert(String filename){
		System.out.println("Peer was told to insert " + filename);
		// TODO: use the proper FileManager to insert new file
		int returnCode = syncManager.addFileToSystem(filename);		
		if (returnCode == -1){
			System.out.println("Error: the file "+ filename + " doesn't exist");
		}else if(returnCode == 1){
			System.out.println("Error: the file "+ filename + " is already in the system");
		}else{
			System.out.println("added "+ filename);
		}
		// PeerInsertNotifierThread p = new PeerInsertNotifierThread(filename);
		return 0;
	}

	public int query(Status status){
		// Use the file manager to create Status
		status = new Status();
		return 0;
	}

	/*
	 * Note that we should have the peer list, so it is not needed as a
	 * parameter
	 */
	public int join(){
		if (currentState == State.connected){
			return ReturnCodes.ERR_UNKNOWN_WARNING;
		}		

		serverThread = new ServerThread(localPort);
		serverThread.start();
		syncThread = new SyncThread();
		syncThread.start();

		//		for (ProxyPeer p : proxyPeerList){
		//			p.send("join");
		//		}

		currentState = State.connected;
		return ReturnCodes.ERR_OK;
	}

	public void send(String filename){
		System.out.println("send called for "+filename);
		for (ProxyPeer p : proxyPeerList){
			if (p.connected){
				try{
					System.out.println("send to "+p.host.getHostAddress()+p.port);
					Socket s = new Socket(p.host, p.port);
					PrintStream ps = new PrintStream(s.getOutputStream());
					System.out.println("begin!");
					ps.println("testfile");
					ps.println(Peer.localAddress.getHostAddress());
					ps.println(Peer.localPort);
					ps.println(filename);
					File f = new File(filename);
					int filelength = (int) f.length();
					ps.println(filelength);
					System.out.println("trying to send"+filename+"");
					System.out.println("trying to send"+filelength+"bytes");
					byte[] b = new byte[filelength];
					FileInputStream fis = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(fis);
					BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
					
					int totalRead = 0;
					while (totalRead < filelength)
					{
						int read = bis.read(b, totalRead, filelength-totalRead);
						totalRead += read;
					}
					
					bos.write(b);
					bos.flush();
					bos.close();
					fis.close();
					bis.close();
					s.close();
				}catch(Exception e){
				}
			}
		}
	}

	public int leave(){
		if (currentState == State.disconnected){
			return ReturnCodes.ERR_UNKNOWN_WARNING;
		}

		//		for (ProxyPeer p : proxyPeerList){
		//			p.leave();
		//		}

		try {
			serverThread.sSocket.close();
		} catch (IOException e) {
		}

		syncThread.running = false;

		currentState = State.disconnected;
		return ReturnCodes.ERR_OK;
	}

	/*
	 * TODO: Feel free to hack around with the private data, since this is part of
	 * your design This is intended to provide some exemplars to help; ignore it
	 * if you don't like it.
	 */

	public enum State {
		connected, disconnected
	};

	//	public static PeerSyncThread peerSyncThread;

}
