package ece454p1;

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
		this.numFiles = FileManager.list.size();
	}
	
	public int numberOfFiles(){
		return numFiles;
	}

	/*Use -1 to indicate if the file requested is not present*/
	public float fractionPresentLocally(int fileNumber){
		if (fileNumber > numFiles || fileNumber < 0){
			return -1;
		}
		Map.Entry<String, Boolean> entry = (Entry<String, Boolean>) FileManager.list.entrySet().toArray()[fileNumber];
		if (entry.getValue() == true){
			return 1;
		}
		return 0;
	}

	/*Use -1 to indicate if the file requested is not present*/
	public float fractionPresent(int fileNumber){
		if (fileNumber > numFiles || fileNumber < 0){
			return -1;
		}
		
		
		return 0;
	}

	/*Use -1 to indicate if the file requested is not present*/
	public int minimumReplicationLevel(int fileNumber){
		if (fileNumber > numFiles || fileNumber < 0){
			return -1;
		}
		
		
		return 0;
	}
	
	/*Use -1 to indicate if the file requested is not present*/
	public float averageReplicationLevel(int fileNumber){
		if (fileNumber > numFiles || fileNumber < 0){
			return -1;
		}
		
		
		return 0;
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

