#ECE454 Project 1

## Peer Interface

### insert(String filename);
	- insert a whole file to the local system
	- chunks it up and put all the chunks in the `Chunks` folder
	- notifies everyone with updated file and chunk listing
### query();
	- based on the synchronized file and chunk listing,
	- query the system status of the replication
### join();
	- tells local peer to join the pool
	- remotes will consider this peer `connected`
### leave();
	- tells local peer to leave the pool
	- remotes will consider this peer `disconnected`
	
## Peer Threads

### ServerThread
Server thread listens on the port outlined by peers.txt. When a new connection comes it will spawn a Response thread.

### ResponseThread
Response thread expects a request in the protocol {`update`, `chunk`, `join`, `leave`}. Every request will be a String representing which command followed by ip and port as identifier. Update will be followed by the file and chunk listing. chunk will be followed by chunk identifier being requested.

## ProxyPeer
ProxyPeer represents the remote peer listed in the peers.txt. it has `send(String request)` which will spawn a thread to send the request if not already spawned, a internal request queue to be filled up by Sync Manager when necessary. 

## File Manager Interfaces
`
public void importFile(String filename);
public String getFileList();
public String getChunkList();
public void parseFileList(String fileList);
public void parseChunkList(String chunkList);
public byte[] readChunkData(String chunkID);
public void writeChunkData(String chunkID, byte[] chunkData);
`

## Sync Manager
Sync Manager mediates File Manager and ProxyPeers.
