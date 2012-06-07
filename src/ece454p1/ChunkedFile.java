package ece454p1;

import java.io.*;

/*
public abstract class ChunkedFile {
	
	public String getName();
	public long getSize();
	public boolean hasChunk(int k);
	public int numberOfChunks();
}

public class LocalFile {
	public LocalFile(String name, long size);
	public LocalFile(RemoteFile remoteFile);
	
	public byte[] readChunk(int k);
	public void writeChunk(int k, byte[] data, int len);
}

public class RemoteFile {
	public RemoteFile(String filename, long size, int[] availableChunks);
}
 */

public abstract class ChunkedFile {
	private String name;
	
	public ChunkedFile(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract long getSize();
	public abstract boolean hasChunk(int k);
	public abstract int numberOfChunks();
	
	static int numberOfChunksForSize(long size) {
		if (size % Config.CHUNK_SIZE == 0) {
			return (int)(size / Config.CHUNK_SIZE);
		} else {
			return (int)(size / Config.CHUNK_SIZE) + 1;
		}
	}
}
