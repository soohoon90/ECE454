package ece454p1;

import java.io.*;
import java.util.*;
import java.net.*;

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
		//System.out.println("Proxy " + this.toString() + " started running");
		
		while (true) {
			String command = null;
			synchronized (this) {
				if (requests.size() == 0) // Done
					break;
				command = requests.removeFirst();
			}
			
			Socket socket = null;
			try {
				socket = new Socket(host, port);
			} catch (IOException e) {
				//System.out.println("Unable to connect to " + this.toString());
				break;
			}
			
			BufferedReader in = null;
			PrintStream out = null;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintStream(socket.getOutputStream());
			} catch (IOException e) {
				System.out.println("Error opening streams to " + this.toString());
				break;
			}
			
			try {
				System.out.println("Starting request '" + command + "' to " + this.toString());
				out.println(command);
				out.println(Peer.localAddress.getHostAddress());
				out.println(Peer.localPort);
				
				if (command.equals("join")) {
					// Request
					out.println(Peer.syncManager.getFileList());
					out.println(Peer.syncManager.getChunkList());
				} if (command.equals("leave")) {
					// nothing else to send
				} else if (command.equals("update")) {
					// Request
					out.println(Peer.syncManager.getFileList());
					out.println(Peer.syncManager.getChunkList());
				} else if (command.equals("chunk")) {
					// Request
					out.println(nextChunk);
					
					// Reply
					InputStream is = socket.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[Config.CHUNK_SIZE];
					while (true) {
						int len = is.read(buffer);
						if (len == -1) break;
						baos.write(buffer, 0, len);
					}
					
					String chunk = null;
					synchronized (this) {
						chunk = nextChunk;
						nextChunk = null;
					}
					Peer.syncManager.writeChunkData(chunk, baos.toByteArray());
				} else if (command.equals("echo")) {
					out.println("echo");
				}
				System.out.println("Finished request '" + command + "' to " + this.toString());
			} catch (IOException e) {
				System.out.println("Stream exception to " + this.toString());
				break;
			}
			
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("Error closing socket to " + this.toString());
				break;
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
	
	public synchronized void chunk(String chunkName) {
		if (nextChunk == null) {
			System.out.println("Enqueuing next chunk " + chunkName + " from " + this.toString());
			nextChunk = chunkName;
		}
		enqueue("chunk");
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
