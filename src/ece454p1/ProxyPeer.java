package ece454p1;

import java.io.*;
import java.util.*;
import java.net.*;


/*
 * ProxyPeer is a internal representation of the remote peer
 * There should be number of lines of peers.txt -1 number of peers
 * 
 * Internally it has a message queue that will be inserted from local Peer
 * that represent the messages to be sent through the socket
 * depending on the type of the message it will wait for responses
 * from the remote PeerResponseThread
 * 
 * Types of messages can be:
 * 		join 	- will send "join"
 * 				- the remote will mark this peer as connected
 * 		leave 	- will send "leave"
 * 				- the remote will mark this peer as disconnected
 * 		update 	- will send "update" followed by:
 * 				- will update file listing and chunk listing
 * 			 	- the remote will send back the file and chunk listing
 * 		chunk	- will send "chunk"
 * 				- the remote will send the data
 */
public class ProxyPeer implements Runnable {
	public InetAddress host;
	public int port;
	public ArrayDeque<String> requests = new ArrayDeque<String>();
	
	// SyncManager
	public HashSet<String> chunks = new HashSet<String>();
	public String nextChunk;

	public ProxyPeer(InetAddress h, Integer p){
		host = h;
		port = p;
	}

	public void run() {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
		} catch (IOException e) {
			System.out.println("Unable to connect to " + this.toString());
			synchronized (this) {
				requests.clear();
			}
			return;
		}
		
		System.out.println("Proxy " + this.toString() + " started running");
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream ps = new PrintStream(socket.getOutputStream());
			
			while (true) {
				String command = null;
				synchronized (this) {
					if (requests.size() == 0) // Done
						break;
					command = requests.removeFirst();
				}
				
				//System.out.println("Sending " + command + " to " + this.toString());
				ps.println(command);
				ps.println(Peer.localAddress.getHostAddress());
				ps.println(Peer.localPort);
				
				if (command.equals("join")) {
					// Request
					ps.println(Peer.syncManager.getFileList());
					ps.println(Peer.syncManager.getChunkList());
				}if (command.equals("leave")) {
					// nothing else to send
				}else if (command.equals("update")) {
					// Request
					ps.println(Peer.syncManager.getFileList());
					ps.println(Peer.syncManager.getChunkList());
				}else if(command.equals("chunk")){
					// chunk request need to send chunkID
					String chunkID = "";
					// String chunkID = SyncManager.grabNextChunkToRequest();
					ps.println(chunkID);
					// the request will now wait for the chunkData
					// inputStream is used instead of BufferedReader
					// because this is byte array
					byte[] chunkData = new byte[Config.CHUNK_SIZE];
					socket.getInputStream().read(chunkData);
					// TODO: write the chunkDATA to chunk file
					// when a write completes, FM will push a new chunk request to our queue
					Peer.syncManager.writeChunkData(chunkID, chunkData);
				} else if (command.equals("echo")) {
					ps.println("echo");
				}
			}
		} catch (IOException e) {
			System.out.println("Stream exception to " + this.toString());
			return;
		}

		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Error closing socket to " + this.toString());
			return;
		} finally {
			socket = null;
		}
		System.out.println("Closed socket to " + this.toString());
	}
	
	private void enqueue(String command) {
		requests.add(command);

		if (requests.size() == 1) {
			new Thread(this).start();
		}
	}
	
	public synchronized void echo() {
		enqueue("echo");
	}
	
	public synchronized void join() {
		enqueue("join");
	}
	
	public synchronized void leave() {
		requests.clear();
		enqueue("leave");
	}
	
	public synchronized void update() {
		requests.remove("update");
		enqueue("update"); // Push to back of queue
	}
	
	public String toString() {
		return host.getHostAddress() + ":" + Integer.toString(port);
	}
}
