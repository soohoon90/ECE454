package ece454p1;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Status is the class that you populate with status data on the state of
 * replication in this peer and its knowledge of the replication level within
 * the system. The thing required in the Status object is the data as specified
 * in the private section The methods shown are examples of methods that we may
 * implement to access such data You will need to create methods to populate the
 * Status data.
 **/
public class Status {

	public Status(){
		numFiles = Peer.syncManager.globalFiles.size();
		local = new float[numFiles];
		system = new float[numFiles];
		leastReplication = new int[numFiles];
		weightedLeastReplication = new float[numFiles];
		int totalNumChunks = 0;
		
		for (int i = 0; i < numFiles; i++){
			
			ChunkedFile f = (ChunkedFile) Peer.syncManager.globalFiles.toArray()[i];
			// get number of chunks in this file
			int numChunks = ChunkedFile.numberOfChunksForFileSize(f.getSize());
			totalNumChunks += numChunks;
			int[] replicated = new int[numChunks];
			int numChunkPresentLocally = 0;
			int numChunkSystem = 0;
			for(int j = 0; j < numChunks; j++){
				if (Peer.syncManager.local.getLocalChunks().contains(f.chunkName(j))){
					numChunkPresentLocally++;
					numChunkSystem++;
					replicated[j]++;
				}
				for (ProxyPeer p : Peer.proxyPeerList){
					if(p.chunks.contains(f.chunkName(j))){
						numChunkSystem++;
						replicated[j]++;
					}
				}
			}
			local[i] = (float)numChunkPresentLocally / numChunks;
			system[i] = (float)numChunkSystem / numChunks;
			
			// find the minimally replicated chunk
			int minValue = replicated[0];  
			for(int k=1;k<replicated.length;k++){  
				if(replicated[k] < minValue){  
					minValue = replicated[k];  
				}
			}
			leastReplication[i] = minValue;
		}
		for (int i = 0; i < numFiles; i++){
			ChunkedFile f = (ChunkedFile) Peer.syncManager.globalFiles.toArray()[i];
			int numChunks = ChunkedFile.numberOfChunksForFileSize(f.getSize());
			weightedLeastReplication[i] = (float)totalNumChunks/numChunks;
		}
		
	}

	public int numberOfFiles(){
		return numFiles;
	}

	/*Use -1 to indicate if the file requested is not present*/
	public float fractionPresentLocally(int fileNumber){
		if (fileNumber > numFiles || fileNumber < 0){
			return -1;
		}
		return local[fileNumber];
	}

	/*Use -1 to indicate if the file requested is not present*/
	public float fractionPresent(int fileNumber){
		if (fileNumber > numFiles || fileNumber < 0){
			return -1;
		}
		return system[fileNumber];
	}

	/*Use -1 to indicate if the file requested is not present*/
	public int minimumReplicationLevel(int fileNumber){
		if (fileNumber > numFiles || fileNumber < 0){
			return -1;
		}

		return leastReplication[fileNumber];
	}

	/*Use -1 to indicate if the file requested is not present*/
	public float averageReplicationLevel(int fileNumber){
		if (fileNumber > numFiles || fileNumber < 0){
			return -1;
		}

		return weightedLeastReplication[fileNumber];
	}

	// This is very cheesy and very lazy, but the focus of this assignment is
	// not on dynamic containers but on the BT p2p file distribution

	/* The number of files currently in the system, as viewed by this peer */
	private int numFiles = 0;

	/*
	 * The fraction of the file present locally (= chunks on this peer/total
	 * number chunks in the file)
	 */
	float[] local;

	/*
	 * The fraction of the file present in the system (= chunks in the
	 * system/total number chunks in the file) (Note that if a chunk is present
	 * twice, it doesn't get counted twice; this is simply intended to find out
	 * if we have the whole file in the system; given that a file must be added
	 * at a peer, think about why this number would ever not be 1.)
	 */
	float[] system;

	/*
	 * Sum by chunk over all peers; the minimum of this number is the least
	 * replicated chunk, and thus represents the least level of replication of
	 * the file
	 */
	int[] leastReplication;

	/*
	 * Sum all chunks in all peers; dived this by the number of chunks in the
	 * file; this is the average level of replication of the file
	 */
	float[] weightedLeastReplication;

}

