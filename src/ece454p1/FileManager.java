package ece454p1;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class FileManager {

	public static final String CHUNKS_PATH = "Chunks";
	public static LocalFile lastFile;
	public static HashMap<String, HashMap<String, Boolean>> list;

	public static String getFileChunkString(String fileName){
		if (list.containsKey(fileName)){
			String fileChunkString = fileName;
			for (String cn : list.get(fileName).keySet()){
				fileChunkString += "," + cn;
			}
			return fileChunkString;
		}
		return "";
	}

	public static String getAllFileChunkString(){
		StringBuilder sb = new StringBuilder();
		Iterator iter = list.keySet().iterator();
		while (iter.hasNext()) {
			String fn = (String) iter.next();
			sb.append(getFileChunkString(fn));
			if (iter.hasNext()){
				sb.append(";");
			}
		}
		return sb.toString();
	}

	public static void insertNewFile(String fileName){
		Random r = new Random();
		HashMap<String, Boolean> chunks = new HashMap<String, Boolean>();
		int numChunk = r.nextInt(3)+3;
		for(int i = 0; i < numChunk; i++ ){
			chunks.put("C"+i, true);
		}
		list.put(fileName, chunks);
		System.out.println(getFileChunkString(fileName));
	}

	public static void parseAllFileChunkString(String multiple){
		// there is multiple
		if (multiple != null){
			if (multiple.indexOf(";") != -1){
				for (String s : multiple.split(";")){
					System.out.println(s);
					parseFileChunkString(s);
				}
			}else{
				parseFileChunkString(multiple);
			}		
		}
	}

	public static void parseFileChunkString(String fileChunkString){
		if (fileChunkString != null){
			String args[] = fileChunkString.split(",");
			if (args.length > 1){
				String fileName = args[0];
				if ( list.containsKey(fileName) == false){
					list.put(fileName, new HashMap<String, Boolean>());
				}
				for (int i = 1; i < args.length; i++){			
					if (list.get(fileName).containsKey(args[i]) == false){
						list.get(fileName).put(args[i], false);
					}
				}
			}
		}
	}

	public static String getFileNotLocal(){

		Iterator itor = list.keySet().iterator();
		while(itor.hasNext()){
			String fn = (String)itor.next();
			HashMap<String, Boolean> c = list.get(fn);
			Iterator itor2 = c.keySet().iterator();
			while (itor2.hasNext()){
				String cn = (String)itor2.next();
				Boolean exist = c.get(cn);
				String r = fn.concat(",").concat(cn);
				if (exist == false){
					return r;
				}
			}
		}

		return "";
	}

	static {
		File chunksDir = new File(CHUNKS_PATH);
		list = new HashMap<String, HashMap<String, Boolean>>(); 

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
