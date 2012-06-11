package ece454p1;

import java.io.*;
import java.util.*;
import java.net.*;

public class ProxyPeer implements Runnable {
	
	private InetAddress address;
	private int port;
	private HashSet<String> chunks = new HashSet<String>();
	
	// Must be synchronized
	private ArrayDeque<String> requests = new ArrayDeque<String>();
	private ArrayDeque<String> pendingChunks = new ArrayDeque<String>();
	
	public ProxyPeer(InetAddress address, Integer port){
		this.address = address;
		this.port = port;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public synchronized HashSet<String> getChunks() {
		return new HashSet<String>(chunks);
	}
	
	public synchronized void setChunks(HashSet<String> chunks) {
		this.chunks = new HashSet<String>(chunks);
	}
	
	public synchronized ArrayDeque<String> getPendingChunks() {
		return new ArrayDeque<String>(pendingChunks);
	}
	
	public synchronized void setPendingChunks(ArrayDeque<String> pendingChunks) {
		this.pendingChunks = new ArrayDeque<String>(pendingChunks);
		if (pendingChunks.size() > 0) {
			if (!requests.contains("chunk")) // The pending list has grown from zero
				enqueue("chunk");
		} else {
			if (requests.contains("chunk")) // The pending list has shrunk to zero
				requests.remove("chunk");
		}
	}

	public void run() {
		//System.out.println("Proxy " + this.toString() + " started running");
		
		while (true) {
			String command = null;
			String chunk = null;
			synchronized (this) { // Only access to queue
				if (requests.size() == 0)
					break;
				command = requests.removeFirst();
				
				if (command.equals("chunk")) {
					chunk = pendingChunks.removeFirst();
					if (pendingChunks.size() > 0)
						requests.add("chunk"); // Push 'chunk' back on queue because there are more chunks
				}
			}
			
			Socket socket = null;
			try {
				socket = new Socket(address, port);
			} catch (IOException e) {
				//System.out.println("Unable to connect to " + this.toString());
				continue;
			}
			
			BufferedReader in = null;
			PrintStream out = null;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintStream(socket.getOutputStream());
			} catch (IOException e) {
				System.out.println("Error opening streams to " + this.toString());
				continue;
			}
			
			try {
				//System.out.println("Starting request '" + command + "' to " + this.toString());
				out.println(command);
				out.println(Peer.localAddress.getHostAddress());
				out.println(Peer.localPort);
				
				if (command.equals("join")) {
					System.out.println("Sending 'join' to " + this.toString());
					// Request
					out.println(Peer.syncManager.getFileList());
					out.println(Peer.syncManager.getChunkList());
				} else if (command.equals("update")) {
					System.out.println("Sending 'update' to " + this.toString());
					// Request
					out.println(Peer.syncManager.getFileList());
					out.println(Peer.syncManager.getChunkList());
				} else if (command.equals("chunk")) {
					System.out.println("Downloading " + chunk + " from " + this.toString());
					// Request
					out.println(chunk);
					
					// Reply
					InputStream is = socket.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[Config.CHUNK_SIZE];
					while (true) {
						int len = is.read(buffer);
						if (len == -1) break;
						baos.write(buffer, 0, len);
					}
					
					Peer.syncManager.writeChunkData(chunk, baos.toByteArray());
				} else if (command.equals("leave")) {
					// Nothing else to send
				} else if (command.equals("echo")) {
					System.out.println("Sending 'echo' to " + this.toString());
					out.println("echo");
				}
				//System.out.println("Requested '" + command + "' from " + this.toString());
			} catch (IOException e) {
				System.out.println("Stream exception to " + this.toString());
				continue;
			}
			
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("Error closing socket to " + this.toString());
				continue;
			}
			//System.out.println("Closed socket to " + this.toString());
		}
		//System.out.println("Proxy " + this.toString() + " stopped running");
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
		this.setChunks(new HashSet<String>());
	}
	
	public synchronized void update() {
		requests.remove("update");
		enqueue("update"); // Push 'update' to back of queue
	}
	
	public String toString() {
		return address.getHostAddress() + ":" + Integer.toString(port);
	}
}
