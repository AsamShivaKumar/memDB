package handlers;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock; 
import java.util.concurrent.locks.ReadWriteLock; 
import java.util.concurrent.locks.ReentrantReadWriteLock; 

import resp.Value;
import persistance.Aof;

public class HashStoreHandler{

    // hash stores
    private final Map<String, String> keyValueStore = new HashMap<>();
    private final Map<String, Map<String, String>> hashStore = new HashMap<>();

    // read-write locks
    private final ReadWriteLock rwLock1 = new ReentrantReadWriteLock();
    private final ReadWriteLock rwLock2 = new ReentrantReadWriteLock();
    private final Lock readLock1 = rwLock1.readLock();
    private final Lock readLock2 = rwLock2.readLock();
    private final Lock writeLock1 = rwLock1.writeLock();
    private final Lock writeLock2 = rwLock2.writeLock();

    private final Aof aofObject;

    public HashStoreHandler(Aof aofObject){
        this.aofObject = aofObject;
    }
    
    public Value set(Value request, boolean backup){
        Value[] req = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk") || !req[2].getType().equals("bulk")) return new Value("");
        
        String key = req[1].getBulk();
        String val = req[2].getBulk();

        writeLock1.lock();
        keyValueStore.put(key, val);
        writeLock1.unlock();

        try{
            if(backup) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        Value response = new Value("string");
        response.setString("OK");
        return response;
    }

    public Value get(Value request){
        Value[] req = request.getArray();
        if(req.length < 2 || !req[1].getType().equals("bulk")) return new Value("");
        
        String key = req[1].getBulk();

        readLock1.lock();
        String val = keyValueStore.getOrDefault(key, "");
        readLock1.unlock();

        Value response = new Value("bulk");
        response.setBulk(val);
        return response;
    }

    public Value hset(Value request, boolean backup){
        Value[] req = request.getArray();
        if(req.length < 4 || !req[1].getType().equals("bulk") || !req[2].getType().equals("bulk") || !req[3].getType().equals("bulk")) return new Value("");
        
        String key = req[1].getBulk();
        String field = req[2].getBulk();
        String val = req[3].getBulk();

        writeLock2.lock();
        if(!hashStore.containsKey(key)) hashStore.put(key, new HashMap<>());
        hashStore.get(key).put(field, val);
        writeLock2.unlock();

        try{
            if(backup) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        Value response = new Value("string");
        response.setString("OK");
        return response;
    }

    public Value hget(Value request){
        Value[] req = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk") || !req[2].getType().equals("bulk")) return new Value("");
        
        String key = req[1].getBulk();
        String field = req[2].getBulk();

        readLock2.lock();
        String val = hashStore.getOrDefault(key, new HashMap<>()).getOrDefault(field, "");
        readLock2.unlock();

        Value response = new Value("bulk");
        response.setBulk(val);
        return response;
    }
}