package ece454p1;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/*
public class FileManager {
	public static LocalFile[] getLocalFiles();
	public static int[] getChunks
}
 */

public class FileManager {
	
	public static final String CHUNKS_PATH = "Chunks";
	public static LocalFile lastFile;
	public static HashMap<String, Boolean> list;
	
	public String getFileNotLocal(){
		for(Entry<String, Boolean> e : list.entrySet()){
			if (e.getValue() == false){
				return e.getKey();
			}
		}
		return "";
	}
	
	public static LocalFile[] localFiles;
	
	static {
		File chunksDir = new File(CHUNKS_PATH);
		list = new HashMap<String, Boolean>(); 
		
		if (!chunksDir.exists()) {
			chunksDir.mkdir();
		}
	}
	
	public static void main(String[] args) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			String line = "";
			
			System.out.print("> ");
			try {
				line = in.readLine();
			} catch (IOException e) {
				System.out.println("Error reading input");
				System.exit(1);
			}
			
			if (line.equals("exit")) {
				System.exit(0);
			} else if (line.equals("rebuild")) {
				FileManager.rebuildLastFile();
			} else {
				lastFile = FileManager.addFile(line);
			}
		}
	}
	
	public static LocalFile addFile(String filename) {
		File srcFile = new File(filename);
		File desFile = new File(srcFile.getName());
		
		LocalFile local = new LocalFile(srcFile.getName(), srcFile.length());
		
		RandomAccessFile src = null;
		RandomAccessFile des = null;
		try {
			if (!desFile.exists()) {
				desFile.createNewFile();
			}
			
			src = new RandomAccessFile(srcFile, "r");
			des = new RandomAccessFile(desFile, "rws");
			
			byte[] buffer = new byte[Config.CHUNK_SIZE];
			for (int i = 0; i < local.numberOfChunks(); i++) {
				int len = src.read(buffer);
				local.writeChunk(i, buffer, len);
				des.write(buffer, 0, len);
			}
			
			return local;
		} catch (IOException e) {
			System.out.println("Error importing file");
		} finally {
			try {
				if (src != null)
					src.close();
				if (des != null)
					des.close();
			} catch (IOException e) {
			
			}
		}
		return null;
	}
	
	/*
	private LocalFile[] listLocalFiles() {
		
	}
	 */
	
	public static void rebuildLastFile() {
		if (lastFile == null)
			return;
		
		File desFile = new File(lastFile.getName());
		
		RandomAccessFile des = null;
		try {
			if (!desFile.exists()) {
				desFile.createNewFile();
			}
			
			des = new RandomAccessFile(new File(lastFile.getName()), "rws");
			
			for (int i = 0; i < lastFile.numberOfChunks(); i++) {
				byte[] data = lastFile.readChunk(i);
				des.write(data);
			}
			
		} catch (IOException e) {
			System.out.println("Error rebuilding file " + lastFile.getName());
		} finally {
			try {
				if (des != null)
					des.close();
			} catch (IOException e) {
				
			}
		}
	}
}
