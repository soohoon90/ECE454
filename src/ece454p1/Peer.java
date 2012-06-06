package ece454p1;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Peer and Status are the classes we really care about Peers is a container;
 * feel free to do a different container
 */
public class Peer {
	// This is the formal interface and you should follow it
	
	Thread serverThread;
	
	public Peer(PeerList peerList){
		currentState = State.disconnected;
		this.peerList = peerList;
		threadList = new ArrayList<Thread>();
		System.out.println("Peer is spawning PeerServer to listen for connections at "+peerList.myPort);
		serverThread = new Thread(new PeerServer(peerList.myPort)); 
	}
	
	public int insert(String filename){
		System.out.println("Peer was told to insert " + filename);
		return 0;
	}

	public int query(Status status){
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
		for (int i=0; i < peerList.hosts.size(); i++){
			PeerClient p = null;
			try {
				p = new PeerClient(peerList.hosts.get(i), peerList.ports.get(i));
			} catch (UnknownHostException e) {
			} catch (IOException e) {
				System.out.println("Failed to connect to "+peerList.hosts.get(i)+":"+ peerList.ports.get(i));
			}
			if (p != null){
				threadList.add(new Thread(p));
			}
		}
		for(int i = 0; i < threadList.size(); i++){
			threadList.get(i).start();
		}
		currentState = State.connected;
		return ReturnCodes.ERR_OK;
	}

	public int leave(){
		if (currentState == State.disconnected){
			return ReturnCodes.ERR_UNKNOWN_WARNING;
		}
		// TODO: finalize stuffs
		// TODO: kill threads
		for(int i = 0; i < threadList.size(); i++){
			threadList.get(i).stop();
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
		connected, disconnected, unknown
	};

	private State currentState;
	private ArrayList<Thread> threadList; 
	private PeerList peerList;

}
