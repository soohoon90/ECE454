package ece454p1;

import java.io.*;

public class LocalFile extends ChunkedFile {
	private long size;
	
	public LocalFile(String name, long size) {
		super(name);
		this.size = size;
	}
	
	public LocalFile(RemoteFile remoteFile) {
		super(remoteFile.getName());
		this.size = remoteFile.getSize();
	}
	
	private File getFileForChunk(int k) {
		return new File(FileManager.CHUNKS_PATH, this.getName() + "." + Integer.toString(k));
	}
	
	public long getSize() {
		return size;
	}
	
	public boolean hasChunk(int k) {
		if (k >= this.numberOfChunks())
			return false;
		
		File chunk = this.getFileForChunk(k);
		if (!chunk.exists())
			return false;
		if (chunk.length() == 0)
			return false;
		return true;
	}
	
	public int numberOfChunks() {
		return ChunkedFile.numberOfChunksForSize(size);
	}
	
	public byte[] readChunk(int k) {
		if (k >= this.numberOfChunks())
			return null;
		
		File file = this.getFileForChunk(k);
		RandomAccessFile in = null;
		try {
			in = new RandomAccessFile(file, "r");
			
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
	
	public void writeChunk(int k, byte[] data, int len) {
		if (k >= this.numberOfChunks())
			return;
		
		File file = this.getFileForChunk(k);
		RandomAccessFile out = null;
		try {
			out = new RandomAccessFile(file, "rws");
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
}
