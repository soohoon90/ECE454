package ece454p1;

import java.io.*;
import java.util.*;

public class Main {
	
	private static FileManager manager = new FileManager();
	private static Peer[] peers;
	private static PeerServer server;
	
	private static void printUsageAndQuit(int code) {
		System.out.println();
		System.out.println("Usage:");
		System.out.println("\tece454p1.Main peers-file peer-number");
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
			Main.printUsageAndQuit(1);
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
			System.out.println(peers[i]);
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
			String command = lineArgs[0];
			if (command.equals("exit")) {
				System.exit(0);
			} else if (command.equals("join")) {
				Main.join(lineArgs);
			} else if (command.equals("query")) {
				Main.query(lineArgs);
			} else if (command.equals("insert")) {
				Main.insert(lineArgs);
			} else if (command.equals("leave")) {
				Main.leave(lineArgs);
			} else if (command.equals("list")) {
				Main.list(lineArgs);
			} else if (command.equals("rebuild")) {
				Main.rebuild(lineArgs);
			} else {
				System.out.println("Unknown command: " + command);
			}
		}
	}
	
	public static void join(String[] args) {
		server.start();
		
		for (Peer peer : peers) {
			peer.connect();
		}
	}
	
	public static void query(String[] args) {
		
	}
	
	public static void insert(String[] args) {
		if (args.length != 2)
			return;
		
		manager.importFile(args[1]);
	}
	
	public static void leave(String[] args) {
		server.stop();
	}
	
	public static void list(String[] args) {
		for (ChunkedFile file : manager.files) {
			System.out.println(file.getName());
		}
	}
	
	public static void rebuild(String[] args) {
		if (args.length != 2)
			return;
		
		for (ChunkedFile file : manager.files) {
			if (file.getName().equals(args[1])) {
				manager.rebuildFile(file);
				return;
			}
		}
		System.out.println("Error: chunked file " + args[1] + " not found");
	}
}
