
import java.io.*;
import java.net.*;
import java.util.*;

import resp.Resp;
import resp.Value;
import persistance.Aof;
import data_structures.RList;

import handlers.HashStoreHandler;
import handlers.ListHandler;
import handlers.SetHandler;

public class Server {
	
	private ServerSocket server;
    private boolean runServer;

    // append-only file -- for persistance of db
    private final Aof aofObject = new Aof("data/db_file.txt");

    // handlers
    HashStoreHandler hashStoreHandler = new HashStoreHandler(aofObject);
    ListHandler listHandler = new ListHandler(aofObject);
    SetHandler setHandler = new SetHandler(aofObject);

    // backup boolean
    boolean backup = false;

	
	public Server(int port){
		try {
			server = new ServerSocket(port);
            aofObject.open();
			System.out.printf("Server started listening on port: %d\n", port);			
		}catch(IOException exp) {
			System.out.println("IOExpception while setting up server. Stack trace - ");
			exp.printStackTrace();
		}
	}

    public void start(){
        runServer = true;
        readFromDB();
        backup = true;
        Thread serverThread = new Thread(this::run);
        serverThread.start();
    }

    private void run() {
        while(runServer) {
            acceptRequest();
        }
    }

    public void stop() {
        runServer = false;
        try{
            System.out.println("Closing server...");
            server.close();
            aofObject.close();
        }catch(IOException exp) {
            System.out.println("IOExpception while closing server. Stack trace - ");
            exp.printStackTrace();
        }
    }

    private void readFromDB(){
        try{
        
            BufferedReader reader = new BufferedReader(new FileReader("data/db_file.txt"));
            while(true){
                Resp parser = new Resp(reader);
                Value data = parser.read();
                if(data.getType().equals("")) break;
                handler(data);
            }

            reader.close();
        }catch(IOException exp){
            System.out.println("IOExpception while reading from db_file. Stack trace - ");
            exp.printStackTrace();
        }
    }

    private Value ping(){
        Value response =  new Value("string");
        response.setString("PONG");
        return response;
    }

    private Value handler(Value request){
        if(!request.getType().equals("array")) return new Value("");
        Value[] req = request.getArray();
        String cmd = req[0].getBulk().toUpperCase();

        switch(cmd){
            case "PING":
                return ping();
            case "SET": 
                return hashStoreHandler.set(request, backup);
            case "GET":
                return hashStoreHandler.get(request);
            case "HSET":
                return hashStoreHandler.hset(request, backup);
            case "HGET":
                return hashStoreHandler.hget(request);
            case "LPUSH":
                return listHandler.lpush(request, backup);
            case "RPUSH":
                return listHandler.rpush(request, backup);
            case "LPOP":
                return listHandler.lpop(request, backup);
            case "RPOP":
                return listHandler.rpop(request, backup);
            case "LLEN":
                return listHandler.llen(request);
            case "LINDEX":
                return listHandler.lindex(request);
            case "LRANGE":
                return listHandler.lrange(request);
            case "LTRIM":
                return listHandler.ltrim(request, backup);
            case "SADD":
                return setHandler.sadd(request, backup);
            case "SREM":
                return setHandler.srem(request, backup);
            case "SCARD":
                return setHandler.scard(request);
            case "SMEMBERS":
                return setHandler.smembers(request);
            case "SISMEMBER":
                return setHandler.sismember(request);
            default: return new Value("");
        }
    }
	
	public void acceptRequest() {
		Socket client;
		try {
			client = server.accept();
			System.out.printf("Connection established with client - %d\n", client.getPort());
			
            Resp parser = new Resp(client.getInputStream());
			Value data = parser.read();
			
			System.out.printf("Data received from the client: %s\n", data.toString());

            // send reponse to client
            PrintWriter wr = new PrintWriter(client.getOutputStream());
            Value response = handler(data);
            wr.write(response.serializeValue());
            wr.close();
			// client.close();
			
		} catch (IOException e) {
			System.out.println("IOExpception while connecting to client. Stack trace - ");
			e.printStackTrace();
		}
	}

    public static void main(String[] args) {
		
		Server svr = new Server(6379);
		svr.start();
        // svr.stop();
	}
}