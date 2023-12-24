package handlers;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock; 
import java.util.concurrent.locks.ReadWriteLock; 
import java.util.concurrent.locks.ReentrantReadWriteLock;

import resp.Value;

public class PubSubHandler{

    private final Map<String, Set<Integer>> channels = new HashMap<>();
    private final Map<Integer, Set<String>> subscribers = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();


    private Value getResponseArray(String chnl){
        Value response = new Value("array");
        Value[] array = new Value[3];
        array[0] = new Value("bulk");
        array[0].setBulk("subscribe");
        array[1] = new Value("bulk");
        array[1].setBulk(chnl);
        array[2] = new Value("num");
        array[2].setNum(channels.getOrDefault(chnl, new HashSet()).size());
        response.setArray(array);
        return response;
    }

    public Value subscribe(Value request, int clientPort){
        Value req[] = request.getArray();
        if(req.length < 2) return new Value("array");

        Value response = new Value("array");
        Value[] array = new Value[req.length - 1];

        for(int i = 1; i < req.length; i++){
            if(!req[i].getType().equals("bulk")) continue;
            String chnl = req[i].getBulk();

            writeLock.lock();

            if(!channels.containsKey(chnl)) channels.put(chnl, new HashSet<>());
            if(!subscribers.containsKey(clientPort)) subscribers.put(clientPort, new HashSet<>());

            channels.get(chnl).add(clientPort);
            subscribers.get(clientPort).add(chnl);

            array[i-1] = getResponseArray(chnl);

            writeLock.unlock();
        }

        response.setArray(array);
        return response;
    }

    public Value unsubscribe(Value request, int clientPort){
        Value req[] = request.getArray();
        if(req.length < 2) return new Value("array");

        Value response = new Value("array");
        Value[] array = new Value[req.length - 1];

        for(int i = 1; i < req.length; i++){
            if(!req[i].getType().equals("bulk")) continue;
            String chnl = req[i].getBulk();

            writeLock.lock();

            if(channels.containsKey(chnl) && channels.get(chnl).contains(clientPort)) channels.get(chnl).remove(clientPort);
            if(subscribers.containsKey(clientPort) && subscribers.get(clientPort).contains(chnl)) subscribers.get(clientPort).remove(chnl);

            array[i-1] = getResponseArray(chnl);

            writeLock.unlock();
        }

        response.setArray(array);
        return response;
    }

    public Value publish(Value request, int clientPort, Map<Integer, PrintWriter> clients){
        Value req[] = request.getArray();
        if(req.length < 3 || !req[1].getType().equals("bulk") || !req[2].getType().equals("bulk")) return new Value("string");

        Value response = new Value("string");
        response.setString("FAILED");

        if(subscribers.containsKey(clientPort) && subscribers.get(clientPort).size() > 0) return response;

        for(int client: channels.getOrDefault(req[1].getBulk(), new HashSet<>())){
            if(!clients.containsKey(client)) continue;
            clients.get(client).write(req[2].serializeValue());
        }

        response.setString("OK");
        return response;
    }


}