package ece454p1;

import java.io.*;
import java.util.*;

public class FileManager {
	
	public static final String CHUNKS_PATH = "Chunks";
	
	static {
		File chunksDir = new File(CHUNKS_PATH);
		
		if (!chunksDir.exists()) {
			chunksDir.mkdir();
		}
	}
	
	public static void main(String[] args) {
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
			
			if (line.equals("exit")) {
				System.exit(0);
			} else {
				FileManager.addFile(line);
			}
		}
	}
	
	public static synchronized void addFile(String filename) {
		File src = new File(filename);
		File des = new File(src.getName());
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(src);
			
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(des);
				
				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
			} finally {
				if (out != null)
					out.close();
			}
			
			ChunkedFile file = new ChunkedFile(src.getName());
		} catch (IOException e) {
			System.out.println("Error copying file");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					
				}
			}
		}
	}
}