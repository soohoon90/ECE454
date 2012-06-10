package ece454p1;

import java.io.*;
import java.util.*;

public class SyncManager {
	private LocalFileManager local;
	private HashSet<ChunkedFile> globalFiles;
	
	public SyncManager() {
		local = new LocalFileManager();
		globalFiles = local.getLocalFiles();
	}
	
	/**
	 * Imports the file at filename into the global system file list, then pushes the new list to any peers
	 */
	public synchronized int addFileToSystem(String filename) {
		if (filename.contains(":")) {
			System.out.println("Illegal ':' in filename " + filename);
			return -1;
		}
		File file = new File(filename);
		
		// Don't allow duplicate filenames
		for (ChunkedFile globalFile : globalFiles) {
			if (globalFile.getName().equals(file.getName())) {
				System.out.println("File " + file.getName() + " already exists in system");
				return 1;
			}
		}
		
		// Import file
		ChunkedFile newChunkedFile = local.importFile(filename);
		if (newChunkedFile == null) {
			return -1;
		}
		globalFiles.add(newChunkedFile);
		
		if (Peer.currentState == Peer.State.connected){
			for (ProxyPeer p : Peer.proxyPeerList){
				p.update();
			}
		}
		return 0;
	}
	
	/**
	 * @return A string of colon separated items consisting of <file, size> pairs
	 */
	public synchronized String getFileList() {
		ArrayList<ChunkedFile> files = new ArrayList<ChunkedFile>(globalFiles);
		if (files.size() == 0) {
			return "";
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(files.get(0).getName());
		buffer.append(":");
		buffer.append(Long.toString(files.get(0).getSize()));
		for (int i = 1; i < files.size(); i++) {
			buffer.append(":");
			buffer.append(files.get(i).getName());
			buffer.append(":");
			buffer.append(Long.toString(files.get(i).getSize()));
		}
		return buffer.toString();
	}
	
	/**
	 * @return A string of colon separated chunk identifiers
	 */
	public synchronized String getChunkList() {
		ArrayList<String> chunks = new ArrayList<String>(local.getLocalChunks());
		if (chunks.size() == 0) {
			return "";
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(chunks.get(0));
		for (int i = 1; i < chunks.size(); i++) {
			buffer.append(":");
			buffer.append(chunks.get(i));
		}
		return buffer.toString();
	}
	
	/**
	 *
	 */
	public synchronized void parseFileList(String fileList) {
		String[] items = fileList.split(":");
		HashSet<ChunkedFile> files = new HashSet<ChunkedFile>();
		
		// Validate the file list
		if (items.length > 1) {
			if (items.length % 2 != 0) {
				System.out.println("Missing item in pair<file, size> list");
				return;
			}
			for (int i = 0; i < items.length; i += 2) {
				long size = 0;
				try {
					size = Long.parseLong(items[i + 1]);
				} catch (NumberFormatException e) {
					System.out.println("Invalid file size " + items[i + 1]);
					return;
				}
				files.add(new ChunkedFile(items[i], size));
			}
			for (ChunkedFile file : files) {
				for (ChunkedFile globalFile : globalFiles) {
					if (globalFile.getName().equals(file.getName())) {
						if (globalFile.getSize() != file.getSize()) { // Check that file sizes match
							System.out.println("File sizes don't match: " + globalFile.toString() + " (existing), and " + file.toString() + " (new)");
							return;
						}
					}
				}
			}
		}
		
		// Send out updates if list is different
		if (globalFiles.containsAll(files))
			return;
		
		globalFiles.addAll(files);
		for (ProxyPeer proxy : Peer.proxyPeerList) {
			proxy.update();
		}
	}
	
	/**
	 * 
	 */
	public synchronized void parseChunkList(ProxyPeer proxy, String chunkList) {
		String[] chunks = chunkList.split(":");
		
		if (chunks.length > 0 && chunks[0].length() == 0) {
			chunks = new String[0];
		}
		
		// Validate the chunk list
		for (String chunk : chunks) {
			String filename = ChunkedFile.filenameFromChunkName(chunk);
			int cn = ChunkedFile.numberFromChunkName(chunk);
			if (filename == null || cn < 0) {
				System.out.println("Invalid chunk " + chunk + " in list");
				return;
			}
		}
		
		// Update the proxy object
		proxy.chunks = new HashSet<String>(Arrays.asList(chunks));
		
		// Request new chunks
		HashSet<String> remoteChunks = new HashSet<String>(proxy.chunks);
		HashSet<String> localChunks = local.getLocalChunks();
		
		remoteChunks.removeAll(localChunks);
		System.out.println("Difference with " + proxy.toString() + ": " + remoteChunks);
		for (String chunk : remoteChunks) {
			proxy.chunk(chunk);
			break; // Just get any chunk
		}
	}
	
	/**
	 * Debug
	 */
	public synchronized void printGlobalFiles() {
		ArrayList<ChunkedFile> files = new ArrayList<ChunkedFile>(globalFiles);
		Collections.sort(files);
		for (ChunkedFile file : files) {
			System.out.println(file);
		}
	}
	
	/**
	 * Debug
	 */
	public synchronized void printLocalFiles() {
		ArrayList<ChunkedFile> files = new ArrayList<ChunkedFile>(local.getLocalFiles());
		Collections.sort(files);
		for (ChunkedFile file : files) {
			System.out.println(file);
		}
	}
	
	/**
	 * Debug
	 */
	public synchronized void printAllChunks() {
		System.out.println("Local:");
		ArrayList<String> chunks = new ArrayList<String>(local.getLocalChunks());
		Collections.sort(chunks);
		for (String chunk : chunks) {
			System.out.println("\t" + chunk);
		}
		System.out.println();
		
		for (ProxyPeer p : Peer.proxyPeerList) {
			System.out.println(p.toString() + ":");
			chunks = new ArrayList<String>(p.chunks);
			Collections.sort(chunks);
			for (String chunk : chunks) {
				System.out.println("\t" + chunk);
			}
			System.out.println();
		}
	}
	
	/**
	 * @return A variably sized array of bytes or null if the chunk cannot be read
	 */
	public synchronized byte[] readChunkData(String chunk) {
		// Parse the chunk name
		String filename = ChunkedFile.filenameFromChunkName(chunk);
		int cn = ChunkedFile.numberFromChunkName(chunk);
		if (filename == null || cn < 0) {
			System.out.println("Error parsing chunk name " + chunk);
			return null;
		}
		
		// Check that there is a global file for this chunk
		ChunkedFile chunkedFile = null;
		for (ChunkedFile file : globalFiles) {
			if (file.getName().equals(filename)) {
				chunkedFile = file;
				break;
			}
		}
		if (chunkedFile == null) {
			System.out.println("File " + filename + " does not have corresponding file in system");
			return null;
		}
		if (cn >= chunkedFile.numberOfChunks()) {
			System.out.println("Chunk number " + cn + " out of bounds for file " + chunkedFile.toString());
			return null;
		}
		
		// Check that it is available
		HashSet<String> localChunks = local.getLocalChunks();
		if (!localChunks.contains(chunk)) {
			System.out.println("Error chunk " + chunk + " not available locally");
			return null;
		}
		
		return local.readChunk(chunkedFile, cn);
	}
	
	public void writeChunkData(String chunk, byte[] data) {
		// Parse the chunk name
		String filename = ChunkedFile.filenameFromChunkName(chunk);
		int cn = ChunkedFile.numberFromChunkName(chunk);
		if (filename == null || cn < 0) {
			System.out.println("Error parsing chunk name " + chunk);
			return;
		}
		
		// Check that there is a global file for this chunk
		ChunkedFile chunkedFile = null;
		for (ChunkedFile file : globalFiles) {
			if (file.getName().equals(filename)) {
				chunkedFile = file;
				break;
			}
		}
		if (chunkedFile == null) {
			System.out.println("File " + filename + " does not have corresponding file in system");
			return;
		}
		if (cn >= chunkedFile.numberOfChunks()) {
			System.out.println("Chunk number " + cn + " out of bounds for file " + chunkedFile.toString());
			return;
		}
		
		local.writeChunk(chunkedFile, cn, data);
		
		// Send updates
		if (Peer.currentState == Peer.State.connected){
			for (ProxyPeer p : Peer.proxyPeerList){
				p.update();
			}
		}
		
		// Request more chunks
		HashSet<String> localChunks = local.getLocalChunks();
		
		for (ProxyPeer p : Peer.proxyPeerList) {
			HashSet<String> remoteChunks = new HashSet<String>(p.chunks);
			
			remoteChunks.removeAll(localChunks);
			for (String remoteChunk : remoteChunks) {
				p.chunk(remoteChunk);
				break; // Just get any chunk
			}
		}
	}
}