package ece454p1;

import java.util.*;
import java.io.*;

public class ChunkedFile {
	
	public ArrayList<Chunk> chunks;
	public final String filename;
	public final long size;
	
	// New chunked file with no chunks
	public ChunkedFile(String filename, long size) {
		this.filename = filename;
		this.size = size;
		this.chunks = new ArrayList<Chunk>();
	}
	
	// New chunked file from complete local file
	public ChunkedFile(String filename) {
		this.filename = filename;
		this.chunks = new ArrayList<Chunk>();
		
		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("Error cannot find file " + filename);
			this.size = 0;
			return;
		}
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			
			long length = file.length();
			for (int n = 0; n < length / Config.CHUNK_SIZE + 1; n++) {
				Chunk chunk = new Chunk(n);
				this.chunks.add(chunk);
				
				// Create the file chunk
				File chunkFile = new File(FileManager.CHUNKS_PATH, chunk.getName());
				if (!chunkFile.exists()) {
					try {
						chunkFile.createNewFile();
					} catch (IOException e) {
						System.out.println("Error creating chunk " + chunk.getName());
						this.chunks.clear();
						break;
					}
				}
				
				// Write the chunk
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(chunkFile);
					
					for (long i = Math.min(length, Config.CHUNK_SIZE); i > 0; i--) {
						out.write(in.read());
						length--;
					}
				} catch (IOException e) {
					System.out.println("Error writing chunk " + chunk.getName());
					this.chunks.clear();
					break;
				} finally {
					if (out != null)
						out.close();
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading file " + filename);
			this.chunks.clear();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					
				}
			}
		}
		
		if (this.chunks.size() > 0) {
			this.size = file.length();
		} else {
			this.size = 0;
		}
	}
	
	public class Chunk {
		public final int number;
		
		public Chunk(int number) {
			this.number = number;
		}
		
		public String getName() {
			return filename + "." + Integer.toString(number);
		}
		
		public String toString() {
			return this.getName();
		}
	}
}