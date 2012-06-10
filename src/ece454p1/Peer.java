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
	public static State currentState;
	public static ArrayList<ProxyPeer> proxyPeerList;
	public static Listener listener;
	public static SyncManager syncManager;
	
	public Peer(ArrayList<ProxyPeer> ppl) {
		currentState = State.disconnected;
		proxyPeerList = ppl;
		syncManager = new SyncManager();
		listener = new Listener();
	}

	public int insert(String filename){
		syncManager.addFileToSystem(filename);
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

		listener.start();
		for (ProxyPeer p : proxyPeerList){
			p.join();
		}
		
		currentState = State.connected;
		return ReturnCodes.ERR_OK;
	}

	public int leave(){
		if (currentState == State.disconnected){
			return ReturnCodes.ERR_UNKNOWN_WARNING;
		}

		for (ProxyPeer p : proxyPeerList){
			p.leave();
		}
		
		listener.stop();
		
		currentState = State.disconnected;
		return ReturnCodes.ERR_OK;
	}
	
	public void echo() {
		for (ProxyPeer p : proxyPeerList) {
			p.echo();
		}
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
