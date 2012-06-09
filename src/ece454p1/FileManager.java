package ece454p1;

import java.io.*;
import java.util.*;

/*
public ArrayList<String> getLocalChunks();
public ArrayList<ChunkedFile> getLocalFiles();
public void importFile(String filename);
public byte[] readChunk(ChunkedFile file, int cn);
public void wrtieChunk(ChunkedFile file, int cn, byte[] data);

I think this might be easier to use for me:
	public int importFile(String filename);									// return 0 for ok, return 1 for already exist, return -1 for non existing file
	public String getFileList();											// synchronized global, returned as 1 string
	public String getChunkList();											// all locally available, returned as 1 string
	public void parseFileList(String fileList);								// global list doesn't matter who sent it, check for new file, if so, send update to all
	public void parseChunkList(String ip, int port, String chunkList); 		// will update a remote's
	public byte[] readChunkData(String chunkID);							// return null for non existing chunk 
	public void writeChunkData(String chunkID, byte[] chunkData);			// overwrite if already exist?
																			// will push an "update" to all to say we got a new chunk
																			// Also, it will push a new "chunk" request to this peer
																			// maybe we need ip and port of this peer
*/

public class FileManager {

	public static final String CHUNKS_PATH = "Chunks";

	public ArrayList<ChunkedFile> files = new ArrayList<ChunkedFile>();

	public FileManager() {

		// Create the chunk directory
		File chunksDir = new File(CHUNKS_PATH);

		if (!chunksDir.exists()) {
			chunksDir.mkdir();
		}

		// Start with what's in the working directory
		File pwd = new File(".");

		String[] filenames = pwd.list();
		for (String filename : filenames) {
			File file = new File(filename);
			if (!file.isDirectory() && !file.isHidden()) {
				files.add(new ChunkedFile(file.getName(), file.length()));
			}
		}
	}

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
				chunk = new RandomAccessFile(new File(FileManager.CHUNKS_PATH, chunked.chunkName(i)), "rws");

				int len = src.read(buffer);
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
		files.add(chunked);
		return chunked;
	}

	public void rebuildFile(ChunkedFile chunked) {
		File file = new File(chunked.getName());

		RandomAccessFile des = null;
		RandomAccessFile chunk = null;
		try {
			des = new RandomAccessFile(new File(chunked.getName()), "rws");

			byte[] buffer = new byte[Config.CHUNK_SIZE];
			for (int i = 0; i < chunked.numberOfChunks(); i++) {
				chunk = new RandomAccessFile(new File(FileManager.CHUNKS_PATH, chunked.chunkName(i)), "r");

				int len = chunk.read(buffer);
				des.write(buffer, 0, len);
				chunk.close();
			}
			chunk = null;
		} catch (IOException e) {
			System.out.println("Error rebuilding file " + chunked.getName());
		} finally {
			try {
				if (des != null)
					des.close();
				if (chunk != null)
					chunk.close();
			} catch (IOException e) {
				System.out.println("Error: failed to close all files during rebuild");
			}
		}
	}
}