package ece454p1;

import java.io.*;

public class RemoteFile extends ChunkedFile {
	private boolean[] chunks;
	private long size;
	
	public RemoteFile(String filename, long size, int[] chunksAvailable) {
		super(filename);
		
		this.size = size;
		this.chunks = new boolean[ChunkedFile.numberOfChunksForSize(size)];
		for (int i = 0; i < chunksAvailable.length; i++) {
			this.chunks[chunksAvailable[i]] = true;
		}
	}
	
	public long getSize() {
		return size;
	}
	
	public boolean hasChunk(int k) {
		if (k >= chunks.length)
			return false;
		return chunks[k];
	}
	
	public int numberOfChunks() {
		return chunks.length;
	}
}
