package resp;

import java.io.*;

public class Resp {
	
	private BufferedReader reader;
	
	public Resp(InputStream stream) {
		reader = new BufferedReader(new InputStreamReader(stream));
	}

	public Resp(BufferedReader reader){
		this.reader = reader;
	}
	
	public Value read() throws IOException {
		
		char type = (char) reader.read();
		
		switch(type) {
			
			case '*': return readArray();
			case '$': return readBulk();
			case ':': return readNum();
			case '+': return readString();			
			default: return new Value("");
		}
	}
	
	private int readInteger() throws IOException {
		String num = reader.readLine();
		return Integer.parseInt(num);
	}
	
	private Value readNum() throws IOException{
		Value val = new Value("num");
		val.setNum(readInteger());
		return val;
	}
	
	private Value readString() throws IOException{
		Value val = new Value("string");
		val.setString(reader.readLine());
		return val;
	}
	
	private Value readArray() throws IOException {
		int size = readInteger();
		
		Value val = new Value("array");
		Value arr[] = new Value[size];
		
		for(int i = 0; i < size; i++) arr[i] = read();
		val.setArray(arr);
		
		return val;
	}
	
	private Value readBulk() throws IOException {
		Value val = new Value("bulk");
		int len = readInteger();
		char bulk[] = new char[len];
		reader.read(bulk);
		reader.readLine();
		
		val.setBulk(new String(bulk));
		return val;
	}

}