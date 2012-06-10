package ece454p1;

import java.io.IOException;
import java.io.PrintStream;
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
	
	public static SyncManager sync = new SyncManager();
	
	public Peer(PeerList peerList){
		currentState = State.disconnected;
		this.peerList = peerList;
	}
	
	public int insert(String filename){
		System.out.println("Peer was told to insert " + filename);
		// TODO: use the proper FileManager to insert new file
		//FileManager.insertNewFile(filename);
		
		// if the peer is connected
		// open a new connection to send a insert request for each connected peer
		if (currentState == State.connected){
			for(PeerList.PeerInfo p : Peer.peerList.peers){
				if (p.connected == true){
					try {
						Socket s = new Socket(p.host, p.port);
						PrintStream ps = new PrintStream(s.getOutputStream());
						ps.println("insert");
						//ps.println(FileManager.getFileChunkString(filename));
					} catch (UnknownHostException e) {
					} catch (IOException e) {
					}
				}
			}
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
		peerServerThread = new PeerServerThread(peerList.myPort);
		peerServerThread.start();
		
		// launch the sync thread that will
		// tell everyone that we joined
		// then synchronize file list
		// then loop until it all files are in sync
		peerSyncThread = new PeerSyncThread();
		peerSyncThread.start();

		currentState = State.connected;
		return ReturnCodes.ERR_OK;
	}

	public int leave(){
		if (currentState == State.disconnected){
			return ReturnCodes.ERR_UNKNOWN_WARNING;
		}

		// SyncThread will send leave to everyone
		peerSyncThread.running = false;
		peerServerThread.running = false;
		
		// ServerThread is still blocked waiting for a connection
		// Let's send a false connection
		try {
			Socket s = new Socket(peerList.myHost, peerList.myPort);
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

	private enum State {
		connected, disconnected
	};

	private State currentState;
	public static PeerList peerList;
	public static PeerServerThread peerServerThread;
	public static PeerSyncThread peerSyncThread;

}
