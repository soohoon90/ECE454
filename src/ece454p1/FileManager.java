package ece454p1;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class FileManager {

	public static final String CHUNKS_PATH = "Chunks";
	public static LocalFile lastFile;
	public static HashMap<String, ArrayList<Boolean>> list;

	public static String getFileChunkString(String fn){
		if (list.containsKey(fn)){
			String fileChunkString = fn;
			//			for (String cn : list.get(fileName).keySet()){
			fileChunkString += "," + list.get(fn).size();
			//			}
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

	public static void insertNewFile(String fn){
		//		Random r = new Random();
		//		int numChunk = r.nextInt(3)+3;
		//		HashMap<String, Boolean> chunks = new HashMap<String, Boolean>();
		//		for(int i = 0; i < numChunk; i++ ){
		//			chunks.put("C"+i, true);
		//		}
		File file = new File(fn);
		long numChunk = file.length() / Config.CHUNK_SIZE;
		ArrayList<Boolean> b = new ArrayList<Boolean>();
		for(long i = 0; i < numChunk; i++ ){
			b.add(true);
		}
		list.put(fn, b);
		System.out.println(getFileChunkString(fn));
	}

	public static void parseAllFileChunkString(String multiple){
		// there is multiple
		System.out.println(multiple);
		if (multiple != null){
			if (multiple.indexOf(";") != -1){
				for (String s : multiple.split(";")){
					System.out.println("\t"+s);
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
					ArrayList<Boolean> cn = new ArrayList<Boolean>();
					for(int i = 0; i < Integer.parseInt(args[1]); i++){
						cn.add(false);
					}
					list.put(fileName, cn);
				}

				//				if ( list.containsKey(fileName) == false){
				//					list.put(fileName, new HashMap<String, Boolean>());
				//				}
				//				for (int i = 1; i < args.length; i++){			
				//					if (list.get(fileName).containsKey(args[i]) == false){
				//						list.get(fileName).put(args[i], false);
				//					}
				//				}
			}
		}
	}

	public static String getFileNotLocal(){
		for (String fn : list.keySet()){
			for (int i = 0; i < list.get(fn).size(); i++){
				if (list.get(fn).get(i) == false){
					return fn.concat(",").concat(""+i);
				}
			}
		}
		return "";
	}

	public static byte[] fetchFileChunkData(String fn, int cn){
//		System.out.println("fetching...");
		// TODO: DATA
		File file = new File(fn);
		long filelength = file.length();
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			byte[] b = new byte[Config.CHUNK_SIZE];
			raf.seek(Config.CHUNK_SIZE*cn);
			raf.read(b);
			raf.close();
			return b;
		} catch (FileNotFoundException e){
			System.out.println("fetching...not found");
		} catch (IOException e){
			System.out.println("fetching...io exception");
		}
		return null;
	}

	public static void writeFileChunkData(String fn, int cn, byte[] b){
//		System.out.println("writing...");
		list.get(fn).set(cn, true);
		// TODO: DATA
		list.get(fn);
		File file = new File(fn);
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rws");
			raf.setLength(list.get(fn).size()*Config.CHUNK_SIZE);
			raf.seek(Config.CHUNK_SIZE*cn);
			raf.write(b);
			raf.close();
		} catch (FileNotFoundException e) {
			System.out.println("writing...notfound");
		} catch (UnsupportedEncodingException e) {
			System.out.println("writing...unsupported");
		} catch (IOException e) {
			System.out.println("writing...io");
		} finally {
		}
	}

	static {
		File chunksDir = new File(CHUNKS_PATH);
		list = new HashMap<String, ArrayList<Boolean>>(); 

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
