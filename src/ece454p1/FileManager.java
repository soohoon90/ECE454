package ece454p1;

import java.io.*;

public class FileManager {
	public static void main(String[] args) {
		FileManager m = new FileManager();
		
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
			
			if (line.equals("quit")) {
				System.exit(0);
			} else {
				m.addFile(line);
			}
		}
	}
	
	public FileManager() {
		
	}
	
	public void addFile(String filename) {
		File src = new File(filename);
		File des = new File(src.getName());
		
		FileReader in = null;
		try {
			in = new FileReader(src);
			
			FileWriter out = null;
			try {
				out = new FileWriter(des);
				
				int c;
				while ((c = in.read()) != -1) {
					out.write(c);
				}
			} finally {
				if (out != null)
					out.close();
			}
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