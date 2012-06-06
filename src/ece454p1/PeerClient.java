package ece454p1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

class PeerClient implements Runnable{
	Socket peerSocket;
	
	public PeerClient(String host, int port) throws UnknownHostException, IOException{
		System.out.println("Peer spawned new PeerClient to join" + host + ":" +port);
		peerSocket = new Socket(host, port);
	}
	
	@Override
	public void run() {	
		InputStream in = null;
		try {
			in = peerSocket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		
		OutputStream out = null;
		try {
			out = peerSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		
		PrintStream ps = new PrintStream(out, true); // Second param: auto-flush on write = true
		while(true){
			Random r = new Random();
			int n = r.nextInt(50);
		    ps.println("ACK " +n);
		    try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
		
		
	}
}

