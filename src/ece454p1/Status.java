package ece454p1;

import java.util.*;

/**
 * Status is the class that you populate with status data on the state of
 * replication in this peer and its knowledge of the replication level within
 * the system. The thing required in the Status object is the data as specified
 * in the private section The methods shown are examples of methods that we may
 * implement to access such data You will need to create methods to populate the
 * Status data.
 **/
public class Status {

	public ArrayList<ChunkedFile> globalFiles;
	public ArrayList<String> localChunks;
	public ArrayList<ArrayList<String>> proxyChunksList;
	
	public int numberOfFiles() {
		return globalFiles.size();
	}

	/*Use -1 to indicate if the file requested is not present*/
	public float fractionPresentLocally(int fileNumber) {
		if (fileNumber >= this.numberOfFiles() || fileNumber < 0) {
			return -1;
		}
		
		ChunkedFile file = globalFiles.get(fileNumber);
		int numberPresent = 0;
		for (String chunk : localChunks) {
			String filename = ChunkedFile.filenameFromChunkName(chunk);
			if (filename.equals(file.getName()))
				numberPresent++;
		}
		return numberPresent / (float)file.numberOfChunks();
	}

	/*Use -1 to indicate if the file requested is not present*/
	public float fractionPresent(int fileNumber) {
		if (fileNumber >= this.numberOfFiles() || fileNumber < 0){
			return -1;
		}
		
		ChunkedFile file = globalFiles.get(fileNumber);
		HashSet<String> globalChunks = new HashSet<String>();
		globalChunks.addAll(localChunks);
		for (ArrayList<String> proxyChunks : proxyChunksList) {
			globalChunks.addAll(proxyChunks);
		}
		
		int numberPresent = 0;
		for (String chunk : globalChunks) {
			String filename = ChunkedFile.filenameFromChunkName(chunk);
			if (filename.equals(file.getName()))
				numberPresent++;
		}
		return numberPresent / (float)file.numberOfChunks();
	}

	/*Use -1 to indicate if the file requested is not present*/
	public int minimumReplicationLevel(int fileNumber) {
		if (fileNumber >= this.numberOfFiles() || fileNumber < 0) {
			return -1;
		}
		
		ChunkedFile file = globalFiles.get(fileNumber);
		int least = proxyChunksList.size() + 1;
		for (int i = 0; i < file.numberOfChunks(); i++) {
			int replication = 0;
			String chunk = file.chunkName(i);
			if (localChunks.contains(chunk))
				replication++;
			
			for (ArrayList<String> proxyChunks : proxyChunksList) {
				if (proxyChunks.contains(chunk))
					replication++;
			}
			
			if (replication < least)
				least = replication;
		}
		return least;
	}
	
	/*Use -1 to indicate if the file requested is not present*/
	public float averageReplicationLevel(int fileNumber) {
		if (fileNumber >= this.numberOfFiles() || fileNumber < 0) {
			return -1;
		}
		
		ChunkedFile file = globalFiles.get(fileNumber);
		int total = 0;
		for (int i = 0; i < file.numberOfChunks(); i++) {
			int replication = 0;
			String chunk = file.chunkName(i);
			if (localChunks.contains(chunk))
				total++;
			
			for (ArrayList<String> proxyChunks : proxyChunksList) {
				if (proxyChunks.contains(chunk))
					total++;
			}
		}
		return total / (float)file.numberOfChunks();
	}
	
	// This is very cheesy and very lazy, but the focus of this assignment is
	// not on dynamic containers but on the BT p2p file distribution

	/* The number of files currently in the system, as viewed by this peer */
	//private int numFiles = 0;

	/*
	 * The fraction of the file present locally (= chunks on this peer/total
	 * number chunks in the file)
	 */
	//float[] local;

	/*
	 * The fraction of the file present in the system (= chunks in the
	 * system/total number chunks in the file) (Note that if a chunk is present
	 * twice, it doesn't get counted twice; this is simply intended to find out
	 * if we have the whole file in the system; given that a file must be added
	 * at a peer, think about why this number would ever not be 1.)
	 */
	//float[] system;

	/*
	 * Sum by chunk over all peers; the minimum of this number is the least
	 * replicated chunk, and thus represents the least level of replication of
	 * the file
	 */
	//int[] leastReplication;

	/*
	 * Sum all chunks in all peers; dived this by the number of chunks in the
	 * file; this is the average level of replication of the file
	 */
	//float[] weightedLeastReplication;
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("Fraction Present Locally\n");
		for (int i = 0; i < this.numberOfFiles(); i++) {
			buffer.append(i);
			buffer.append(": ");
			buffer.append(globalFiles.get(i).toString());
			buffer.append(" bytes: ");
			buffer.append(this.fractionPresentLocally(i));
			buffer.append("\n");
		}
		
		buffer.append("\n");
		buffer.append("Fraction Present\n");
		for (int i = 0; i < this.numberOfFiles(); i++) {
			buffer.append(i);
			buffer.append(": ");
			buffer.append(globalFiles.get(i).toString());
			buffer.append(" bytes: ");
			buffer.append(this.fractionPresent(i));
			buffer.append("\n");
		}
		
		buffer.append("\n");
		buffer.append("Minimum Replication Level\n");
		for (int i = 0; i < this.numberOfFiles(); i++) {
			buffer.append(i);
			buffer.append(": ");
			buffer.append(globalFiles.get(i).toString());
			buffer.append(" bytes: ");
			buffer.append(this.minimumReplicationLevel(i));
			buffer.append("\n");
		}
		
		buffer.append("\n");
		buffer.append("Weighted Replication Level\n");
		for (int i = 0; i < this.numberOfFiles(); i++) {
			buffer.append(i);
			buffer.append(": ");
			buffer.append(globalFiles.get(i).toString());
			buffer.append(" bytes: ");
			buffer.append(this.averageReplicationLevel(i));
			buffer.append("\n");
		}
		
		return buffer.toString();
	}
}

