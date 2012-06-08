package ece454p1;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Peer and Status are the classes we really care about Peers is a container;
 * feel free to do a different container
 */
public class Peer implements Runnable {
	
	private String address;
	private int port;
	private Socket socket;
	private ArrayList<String> chunks;
	
	public Peer(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public void connect() {
		try {
			socket = new Socket(address, port);
		} catch (IOException e) {
			System.out.println("Unable to connect to " + address + ":" + Integer.toString(port));
		}
	}
	
	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Error closing socket");
		}
		socket = null;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public void send(String message) {
		if (socket == null)
			return;
		
		PrintStream out = null;
		try {
			out = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error writing to socket");
			return;
		}
		out.println(message);
	}
	
	public String toString() {
		return address + ":" + Integer.toString(port);
	}
	
	public void run() {
		
	}
	/*
	public int insert(String filename){
		System.out.println("Peer was told to insert " + filename);
		// TODO: use the proper FileManager to insert new file
		FileManager.list.put(filename, true);
		
		// if the peer is connected
		// open a new connection to send a insert request for each connected peer
		if (currentState == State.connected){
			for(PeerList.PeerInfo p : Peer.peerList.peers){
				if (p.connected == true){
					try {
						Socket s = new Socket(p.host, p.port);
						PrintStream ps = new PrintStream(s.getOutputStream());
						ps.println("insert");
						ps.println(filename);
					} catch (UnknownHostException e) {
					} catch (IOException e) {
					}
				}
			}
		}
		// PeerInsertNotifierThread p = new PeerInsertNotifierThread(filename);
		return 0;
	}
	 */

	/*
	public int query(Status status){
		// Use the file manager to create Status
		status = new Status();
		return 0;
	}
	 */

	/*
	 * Note that we should have the peer list, so it is not needed as a
	 * parameter
	 */
	/*
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
	 */

	/*
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
	 */

}
