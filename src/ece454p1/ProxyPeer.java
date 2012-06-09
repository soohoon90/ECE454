package ece454p1;

import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.io.*;
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
public class ProxyPeer extends Thread{
	public InetAddress host;
	public int port;
	public Boolean connected;
	private ArrayDeque<String> requests = new ArrayDeque<String>();
	private ArrayList<String> chunks = new ArrayList<String>();

	public ProxyPeer(InetAddress h, Integer p, Boolean c){
		host = h;
		port = p;
	}

	public void run() {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			
			PrintStream ps = new PrintStream(socket.getOutputStream());
			// while there are messages to be sent
			// grab the first one and act on it
			while (true) {
				String message = null;
				synchronized (this) {
					if (requests.size() == 0) // Done
						break;
					message = requests.removeFirst();
				}
				// send the command first
				ps.println(message);
				// followed by ip and port by convention
				ps.println(Peer.localAddress.getHostAddress());
				ps.println(Peer.localPort);
				// depending on the command, send more lines
				if (message.equals("update")){
					// update request will include file listing and chunk listing
					// TODO: 
					
				}else if(message.equals("chunk")){
					// chunk request need to send chunkID
					String chunkID = "";
					// String chunkID = SyncManager.grabNextChunkToRequest();
					ps.println(chunkID);
					// the request will now wait for the chunkData
					byte[] chunkData = new byte[Config.CHUNK_SIZE];
					socket.getInputStream().read(chunkData);
					// TODO: handle the chunkDATA
					// FileManager.writeChunkData(chunkID, chunkData);
				}
			}
		} catch (IOException e) {
			System.out.println("Unable to connect to " + this.toString());
			return;
		}


		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Error closing socket to " + this.toString());
		}
		socket = null;
	}

	public void send(String message) {
		synchronized (this) {
			requests.addLast(message);

			if (requests.size() == 1 && Peer.currentState == Peer.State.connected) {
				new Thread(this).start();
			}
		}
	}

	public String toString() {
		return host.getHostAddress() + ":" + Integer.toString(port);
	}

}