package ece454p1;

import java.util.*;
import java.io.*;

public class ChunkedFile {
	
	private String filename;
	private Chunk[] chunks;
	
	private void initChunks(long size) {
		if (size % Config.CHUNK_SIZE == 0) {
			this.chunks = new Chunk[(int)(size / Config.CHUNK_SIZE)];
		} else {
			this.chunks = new Chunk[(int)(size / Config.CHUNK_SIZE) + 1];
		}
		
		for (int i = 0; i < chunks.length; i++) {
			chunks[i] = new Chunk(i);
		}
	}
	
	// New chunked file with no chunks
	public ChunkedFile(String filename, long size) {
		this.filename = filename;
		this.initChunks(size);
	}
	
	// New chunked file from complete local file
	public ChunkedFile(String filename) {
		this.filename = filename;
		
		File file = new File(filename);
		this.initChunks(file.length());
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			
			byte[] buffer = new byte[Config.CHUNK_SIZE];
			for (int i = 0; i < chunks.length; i++) {
				int len = in.read(buffer);
				chunks[i].setData(buffer, len);
			}
		} catch (IOException e) {
			System.out.println("Error reading file " + filename);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				
			}
		}
	}
	
	public Chunk[] getChunks() {
		return chunks;
	}
	
	public String getName() {
		return filename;
	}
	
	public boolean isComplete() {
		for (int i = 0; i < chunks.length; i++) {
			if (chunks[i].getFile().length() == 0) {
				return false; // Not all chunks exist locally
			}
		}
		return true;
	}
	
	public File rebuild() {
		if (!this.isComplete())
			return null;
		
		File file = new File(filename);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			
			for (int i = 0; i < chunks.length; i++) {
				byte[] data = chunks[i].getData();
				out.write(data);
			}
			return file;
		} catch (IOException e) {
			System.out.println("Error writing file " + filename);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				
			}
		}
		return null;
	}
	
	public class Chunk {
		private int number;
		
		public Chunk(int number) {
			this.number = number;
		}
		
		public byte[] getData() {
			File file = this.getFile();
			
			FileInputStream in = null;
			try {
				in = new FileInputStream(file);
				
				byte[] data = new byte[(int)file.length()];
				in.read(data);
				return data;
			} catch (IOException e) {
				System.out.println("Error reading chunk " + this.getName());
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
					
				}
			}
			return null;
		}
		
		public void setData(byte[] data, int len) {
			File file = this.getFile();
			
			FileOutputStream out = null;
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				
				out = new FileOutputStream(file);
				out.write(data, 0, len);
			} catch (IOException e) {
				System.out.println("Error writing chunk " + this.getName());
			} finally {
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					
				}
			}
		}
		
		public File getFile() {
			return new File(FileManager.CHUNKS_PATH, this.getName());
		}
		
		public String getName() {
			return filename + "." + Integer.toString(number);
		}
		
		public int getNumber() {
			return number;
		}
		
		public String toString() {
			return this.getName();
		}
	}
}
