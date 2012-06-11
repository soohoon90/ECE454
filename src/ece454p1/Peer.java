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
	public static boolean joined;
	public static ArrayList<ProxyPeer> proxyPeerList;
	public static Listener listener;
	public static SyncManager syncManager;
	
	public Peer(ArrayList<ProxyPeer> ppl) {
		proxyPeerList = ppl;
		syncManager = new SyncManager();
		listener = new Listener();
	}

	public int insert(String filename){
		syncManager.addFileToSystem(filename);
		return ReturnCodes.ERR_OK;
	}

	public int query(Status status){
		syncManager.populateStatus(status);
		return ReturnCodes.ERR_OK;
	}

	/*
	 * Note that we should have the peer list, so it is not needed as a
	 * parameter
	 */
	public int join() {
		if (joined)
			return ReturnCodes.ERR_UNKNOWN_WARNING;

		listener.start();
		for (ProxyPeer p : proxyPeerList){
			p.join();
		}
		
		joined = true;
		return ReturnCodes.ERR_OK;
	}

	public int leave() {
		if (!joined)
			return ReturnCodes.ERR_UNKNOWN_WARNING;

		for (ProxyPeer p : proxyPeerList) {
			p.leave();
		}
		
		listener.stop();
		
		joined = false;
		return ReturnCodes.ERR_OK;
	}
	
	public int echo() {
		for (ProxyPeer p : proxyPeerList) {
			p.echo();
		}
		return ReturnCodes.ERR_OK;
	}

	/*
	 * TODO: Feel free to hack around with the private data, since this is part of
	 * your design This is intended to provide some exemplars to help; ignore it
	 * if you don't like it.
	 */

//	public static PeerSyncThread peerSyncThread;

}
