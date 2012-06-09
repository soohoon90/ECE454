package ece454p1;

import java.io.IOException;
import java.io.PrintStream;
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
	public static FileManager fileManager;
	public static State currentState;
	public static ArrayList<ProxyPeer> proxyPeerList;
	public static ServerThread serverThread;
	
	public Peer(ArrayList<ProxyPeer> ppl) {
		currentState = State.disconnected;
		proxyPeerList = ppl; 
	}

	public int insert(String filename){
		System.out.println("Peer was told to insert " + filename);
		// TODO: use the proper FileManager to insert new file
//		fileManager.importFile(filename);
		
		
		
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

		// launch the server thread that will spawn a response thread
		// response thread will handle the request sent to the server
		// types of req/res include:  request to join w/file list
		//									get file list response
		//							  request to leave
		//							  request for file list update
		//									get file list response
		//							  request for file insert
		//									ACK
		//							  request for file chunk
		//									get file chunk response
		//										will request update w/file list
		serverThread = new ServerThread(localPort);
		serverThread.start();
		
		currentState = State.connected;
		return ReturnCodes.ERR_OK;
	}

	public int leave(){
		if (currentState == State.disconnected){
			return ReturnCodes.ERR_UNKNOWN_WARNING;
		}

		// Set the ServerThread's while loop to end
		serverThread.running = false;
		// ServerThread is still waiting for a connection
		// Let's send a false connection to shut it down
		try {
			Socket s = new Socket(localAddress, localPort);
			s.close();
		} catch (Exception e) {
		}
		
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
