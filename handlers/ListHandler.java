package handlers;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock; 
import java.util.concurrent.locks.ReadWriteLock; 
import java.util.concurrent.locks.ReentrantReadWriteLock;

import resp.Value;
import persistance.Aof;
import data_structures.RList;

public class ListHandler{

    private final Map<String, RList> listStore = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private final Aof aofObject;

    public ListHandler(Aof aofObject){
        this.aofObject = aofObject;
    }
    
    public Value lpush(Value request, boolean backup){
        
        Value[] req = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk")) return new Value("");
        
        String key = req[1].getBulk();

        writeLock.lock();
        if(!listStore.containsKey(key)) listStore.put(key, new RList());
        Value response = listStore.get(key).lpush(request);
        writeLock.unlock();

        try{
            if(backup) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        return response;
    }

    public Value rpush(Value request, boolean backup){
        
        Value[] req = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        writeLock.lock();
        if(!listStore.containsKey(key)) listStore.put(key, new RList());
        Value response = listStore.get(key).rpush(request);
        writeLock.unlock();

        try{
            if(backup) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        return response;
    }

    public Value lpop(Value request, boolean backup){
        
        Value[] req = request.getArray();
        if(req.length < 2 || !req[1].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("");

        writeLock.lock();
        if(listStore.containsKey(key)) response = listStore.get(key).lpop();
        writeLock.unlock();

        try{
            if(!response.getType().equals("") && backup) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        return response;
    }

    public Value rpop(Value request, boolean backup){
        
        Value[] req = request.getArray();
        if(req.length < 2 || !req[1].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("");

        writeLock.lock();
        if(listStore.containsKey(key)) response = listStore.get(key).rpop();
        writeLock.unlock();

        try{
            if(!response.getType().equals("") && backup) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        return response;
    }

    public Value llen(Value request){
            
        Value[] req = request.getArray();
        if(req.length < 2 || !req[1].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("num");
        
        readLock.lock();
        
        if(!listStore.containsKey(key)) response.setNum(0);
        else response = listStore.get(key).llen();
        
        readLock.unlock();

        return response;
    }

    public Value lindex(Value request){
        
        Value[] req = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk") || !req[2].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("bulk");
        
        readLock.lock();
        if(listStore.containsKey(key)) response = listStore.get(key).lindex(request);
        readLock.unlock();

        return response;
    }

    public Value lrange(Value request){
        
        Value[] req = request.getArray();
        if(req.length < 4 || !req[1].getType().equals("bulk") || !req[2].getType().equals("bulk") || !req[3].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("array");
        
        readLock.lock();
        if(listStore.containsKey(key)) response = listStore.get(key).lrange(request);
        readLock.unlock();

        return response;
    }

    public Value ltrim(Value request, boolean backup){
        Value[] req = request.getArray();
        if(req.length < 4 || !req[1].getType().equals("bulk") || !req[2].getType().equals("bulk") || !req[3].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("");

        writeLock.lock();
        if(listStore.containsKey(key)) response = listStore.get(key).ltrim(request);
        writeLock.unlock();

        try{
            if(!response.getType().equals("") && backup) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        return response;
    }

}