package ece454p1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class PeerResponder implements Runnable{
	Socket peerSocket;
	
	public PeerResponder(Socket s){
		peerSocket = s;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
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
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while(peerSocket.isConnected()){
			try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (line == null) break;
		    System.out.println(" "+line);
		}
		System.out.println(" Connection lost");
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		
		try{
			out.close();
		} catch (IOException e){
			
		}
		
		try {
			peerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		
		System.out.println(" bye bye");
	}
}
