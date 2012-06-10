package ece454p1;

import java.util.regex.*;

public class ChunkedFile /* TODO: implements Serializable */ {
	
	private static final String CHUNK_REGEX = "(.+)[.](\\d+)";
	
	private String filename;
	private long size;
	
	public ChunkedFile(String filename, long size) {
		this.filename = filename;
		this.size = size;
	}
	
	public String chunkName(int k) {
		if (k >= this.numberOfChunks())
			return null;
		
		return filename + "." + Integer.toString(k);
	}
	
	public Object clone() {
		return new ChunkedFile(filename, size);
	}
	
	public boolean equals(Object o) {
		if (o instanceof ChunkedFile) {
			ChunkedFile f = (ChunkedFile)o;
			return (f.getSize() == size) && f.getName().equals(filename);
		}
		return false;
	}
	
	public int hashCode() {
		return filename.hashCode() + (int)(17 * size);
	}
	
	public String getName() {
		return filename;
	}
	
	public long getSize() {
		return size;
	}
	
	public int numberOfChunks() {
		return ChunkedFile.numberOfChunksForFileSize(size);
	}
	
	public String toString() {
		return filename + "<" + Long.toString(size) + " bytes>";
	}
	
	public static int numberOfChunksForFileSize(long fileSize) {
		if (fileSize == 0) {
			return 1;
		} else if (fileSize % Config.CHUNK_SIZE == 0) {
			return (int)(fileSize / Config.CHUNK_SIZE);
		} else {
			return (int)(fileSize / Config.CHUNK_SIZE) + 1;
		}
	}
	
	public static String filenameFromChunkName(String chunkName) {
		Pattern pattern = Pattern.compile(ChunkedFile.CHUNK_REGEX);
		Matcher matcher = pattern.matcher(chunkName);
		if (!matcher.matches())
			return null;
		return matcher.group(1);
	}
	
	public static int numberFromChunkName(String chunkName) {
		Pattern pattern = Pattern.compile(ChunkedFile.CHUNK_REGEX);
		Matcher matcher = pattern.matcher(chunkName);
		if (!matcher.matches())
			return 0;
		try {
			return Integer.parseInt(matcher.group(2));
		} catch (NumberFormatException e) {
			System.out.println("Error matching chunk number");
			return 0;
		}
	}
}
