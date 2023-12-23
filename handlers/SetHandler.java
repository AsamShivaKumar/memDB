package handlers;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock; 
import java.util.concurrent.locks.ReadWriteLock; 
import java.util.concurrent.locks.ReentrantReadWriteLock;

import resp.Value;
import persistance.Aof;
import data_structures.RSet;


public class SetHandler{

    private final Map<String, RSet> setStore = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private final Aof aofObject;

    public SetHandler(Aof aofObject){
        this.aofObject = aofObject;
    }

    public Value sadd(Value request, boolean backup){
        
        Value[] req = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk")) return new Value("");
        
        String key = req[1].getBulk();
        Value response = new Value("num");
        response.setNum(0);

        writeLock.lock();
        if(!setStore.containsKey(key)) setStore.put(key, new RSet());
        response = setStore.get(key).sadd(request);
        writeLock.unlock();

        try{
            if(backup && response.getNum() != 0) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        return response;
    }

    public Value srem(Value request, boolean backup){
        
        Value[] req = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("num");
        response.setNum(0);

        writeLock.lock();
        if(setStore.containsKey(key)) response = setStore.get(key).srem(request);
        writeLock.unlock();

        try{
            if(backup && response.getNum() != 0) aofObject.append(request.serializeValue());
        }catch(IOException exp){
            System.out.println("IOExpception while appending to file. Stack trace - ");
            exp.printStackTrace();
        }

        return response;
    }

    public Value scard(Value request){
        
        Value[] req = request.getArray();
        if(req.length < 2 || !req[1].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("num");
        response.setNum(0);

        readLock.lock();
        if(setStore.containsKey(key)) response = setStore.get(key).scard();
        readLock.unlock();

        return response;
    }

    public Value smembers(Value request){
        
        Value[] req = request.getArray();
        if(req.length < 2 || !req[1].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("array");
        response.setArray(new Value[0]);

        readLock.lock();
        if(setStore.containsKey(key)) response = setStore.get(key).smember();
        readLock.unlock();

        return response;
    }

    public Value sismember(Value request){
            
        Value[] req = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk") || !req[2].getType().equals("bulk")) return new Value("");
        String key = req[1].getBulk();

        Value response = new Value("num");
        response.setNum(0);

        readLock.lock();
        if(setStore.containsKey(key)) response = setStore.get(key).sismember(request);
        readLock.unlock();

        return response;
    }


}