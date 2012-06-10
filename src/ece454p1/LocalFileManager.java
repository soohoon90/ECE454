package ece454p1;

import java.io.*;
import java.util.*;

/**
 * Only SyncManager should talk to this class
 */
public class LocalFileManager {

	public static final String CHUNKS_PATH = "Chunks";
	
	public LocalFileManager() {
		// Create the chunk directory
		File chunksDir = new File(CHUNKS_PATH);
		
		if (!chunksDir.exists()) {
			chunksDir.mkdir();
		}
	}
	
	/**
	 * @return The set of locally available chunks
	 */
	public HashSet<String> getLocalChunks() {
		File chunksDir = new File(CHUNKS_PATH);
		
		HashSet<String> localChunks = new HashSet<String>();
		if (chunksDir.isDirectory()) {
			for (String chunk : chunksDir.list()) {
				File file = new File(chunk);
				if (!file.isDirectory() && !file.isHidden()) {
					localChunks.add(chunk);
				}
			}
		}
		return localChunks;
	}
	
	/**
	 * @return The set of the locally complete files
	 */
	public HashSet<ChunkedFile> getLocalFiles() {
		File pwd = new File(".");
		
		HashSet<ChunkedFile> localFiles = new HashSet<ChunkedFile>();
		for (String filename : pwd.list()) {
			File file = new File(filename);
			if (!file.isDirectory() && !file.isHidden()) {
				localFiles.add(new ChunkedFile(file.getName(), file.length()));
			}
		}
		return localFiles;
	}
	
	/**
	 * Copies the file at filename into the working directory and places the chunks in the 'Chunks/' directory
	 */
	public ChunkedFile importFile(String filename) {
		File file = new File(filename);
		ChunkedFile chunked = new ChunkedFile(file.getName(), file.length());
		
		RandomAccessFile src = null;
		RandomAccessFile des = null;
		RandomAccessFile chunk = null;
		try {
			src = new RandomAccessFile(file, "r");
			des = new RandomAccessFile(new File(chunked.getName()), "rws");
			
			byte[] buffer = new byte[Config.CHUNK_SIZE];
			for (int i = 0; i < chunked.numberOfChunks(); i++) {
				chunk = new RandomAccessFile(new File(LocalFileManager.CHUNKS_PATH, chunked.chunkName(i)), "rws");
				
				int len = src.read(buffer);
				if (len == -1) len = 0;
				des.write(buffer, 0, len);
				chunk.write(buffer, 0, len);
				chunk.close();
			}
			chunk = null;
		} catch (IOException e) {
			System.out.println("Error importing file " + filename);
			return null;
		} finally {
			try {
				if (src != null)
					src.close();
				if (des != null)
					des.close();
				if (chunk != null)
					chunk.close();
			} catch (IOException e) {
				System.out.println("Error: failed to close all files during import");
			}
		}
		return chunked;
	}
	
	/**
	 * @return A variably sized array of bytes, or null if the chunk cannot be read
	 */
	public byte[] readChunk(ChunkedFile chunkedFile, int cn) {
		if (cn >= chunkedFile.numberOfChunks())
			return null;
		
		// Read chunk
		File file = new File(LocalFileManager.CHUNKS_PATH, chunkedFile.chunkName(cn));
		RandomAccessFile in = null;
		try {
			in = new RandomAccessFile(file, "r");
			
			byte[] data = new byte[(int)file.length()];
			in.read(data);
			return data;
		} catch (IOException e) {
			System.out.println("Error reading chunk " + chunkedFile.chunkName(cn));
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				
			}
		}
		return null;
	}
	
	/**
	 * Writes the chunk to disk, and if the file is complete, then the file is rebuilt
	 */
	public void writeChunk(ChunkedFile chunkedFile, int cn, byte[] data) {
		if (cn >= chunkedFile.numberOfChunks())
			return;
		
		// Write chunk
		File file = new File(LocalFileManager.CHUNKS_PATH, chunkedFile.chunkName(cn));
		RandomAccessFile out = null;
		try {
			out = new RandomAccessFile(file, "rws");
			out.write(data);
		} catch (IOException e) {
			System.out.println("Error writing chunk " + chunkedFile.chunkName(cn));
			return;
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				
			}
		}
		
		// Rebuild file if all chunks available
		HashSet<String> chunks = this.getLocalChunks();
		for (int i = 0; i < chunkedFile.numberOfChunks(); i++) {
			if (!chunks.contains(chunkedFile.chunkName(i))) {
				return; // Chunk(i) is missing
			}
		}
		
		// Write file
		RandomAccessFile full = null;
		try {
			full = new RandomAccessFile(new File(chunkedFile.getName()), "rws");

			for (int i = 0; i < chunkedFile.numberOfChunks(); i++) {
				byte[] buffer = this.readChunk(chunkedFile, i);
				full.write(buffer);
			}
		} catch (IOException e) {
			System.out.println("Error rebuilding file " + chunkedFile.getName());
		} finally {
			try {
				if (full != null)
					full.close();
			} catch (IOException e) {

			}
		}
	}
}