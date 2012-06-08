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
	
	public Peer(PeerList peerList){
		currentState = State.disconnected;
		this.peerList = peerList;
	}
	
	private static Peer[] peers;
	private static PeerServer server;
	
	private String address;
	private int port;
	private Socket socket;
	private ArrayList<String> files;
	
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
	
	public void run() {
		
	}
	
	private static void printUsageAndQuit(int code) {
		System.out.println();
		System.out.println("Usage:");
		System.out.println("\tPeer peers-file peer-number");
		System.out.println();
		System.exit(code);
	}
	
	// e.g. java ece454p1.Peer peers.txt 1
	public static void main(String[] args) {
		
		// Parse args
		String peersFilename = null;
		int peerNumber = -1;
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (i == args.length - 2) { // Peers file
				peersFilename = args[i];
			} else if (i == args.length - 1) { // Peer number
				try {
					peerNumber = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.out.println("Error parsing peer number");
				}
			} else {
				System.out.println("Unknown arg: " + args[i]);
			}
		}
		
		if (peersFilename == null || peerNumber == -1) {
			System.out.println("Error: Missing peers file or peer number");
			Peer.printUsageAndQuit(1);
		}
		
		// Parse peers file
		ArrayList<Peer> peersList = new ArrayList<Peer>();
		RandomAccessFile peersIn = null;
		try {
			peersIn = new RandomAccessFile(peersFilename, "r");
			
			int lineNumber = 0;
			String line;
			while ((line = peersIn.readLine()) != null) {
				String[] items = line.split(" ");
				if (items.length != 2) {
					System.out.println("Error parsing peers file");
					System.exit(1);
				}
				
				int port = -1;
				try {
					port = Integer.parseInt(items[1]);
				} catch (NumberFormatException e) {
					System.out.println("Error parsing port number");
					System.exit(1);
				}
				
				if (lineNumber == peerNumber) {
					server = new PeerServer(port);
				} else {
					peersList.add(new Peer(items[0], port));
				}
				lineNumber++;
			}
		} catch (IOException e) {
			System.out.println("Error reading peers file");
			System.exit(1);
		} finally {
			try {
				if (peersIn != null)
					peersIn.close();
			} catch (IOException e) {
				
			}
		}
		
		if (peerNumber < 0 || peerNumber > peersList.size()) {
			System.out.println("Error: Peer number out of range");
			System.exit(1);
		}
		peers = peersList.toArray(new Peer[peersList.size()]);
		
		// Startup info
		System.out.println("Other peers:");
		for (int i = 0; i < peers.length; i++) {
			System.out.println(peers[i].address + ":" + Integer.toString(peers[i].port));
		}
		
		// Console
		System.out.println();
		System.out.println("Type 'exit' to quit");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String line = "";
			
			System.out.print("> ");
			try {
				line = in.readLine();
			} catch (IOException e) {
				System.out.println("Error reading input");
				System.exit(1);
			}
			
			String[] lineArgs = line.split(" ");
			if (lineArgs[0].equals("exit")) {
				System.exit(0);
			} else if (lineArgs[0].equals("join")) {
				Peer.join(lineArgs);
			} else if (lineArgs[0].equals("query")) {
				Peer.query(lineArgs);
			} else if (lineArgs[0].equals("insert")) {
				Peer.insert(lineArgs);
			} else if (lineArgs[0].equals("leave")) {
				Peer.leave(lineArgs);
			} else {
				System.out.println("Unknown command: " + lineArgs[0]);
			}
		}
	}
	
	public static void join(String[] args) {
		server.start();
		
		for (Peer peer : peers) {
			if (peer.socket == null) {
				peer.connect();
			}
		}
	}
	
	public static void query(String[] args) {
		
	}
	
	public static void insert(String[] args) {
		
	}
	
	public static void leave(String[] args) {
		server.stop();
	}
	
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
