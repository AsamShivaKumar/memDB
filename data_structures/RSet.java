package data_structures;

import java.util.*;

import resp.Value;

public class RSet{
    
    private final Set<String> set = new HashSet<>();

    public Value sadd(Value request){
        Value[] req = request.getArray();
        if(req.length < 3) return new Value("num");
        
        int count = 0;
        for(int i = 2; i < req.length; i++)
            if(req[i].getType().equals("bulk") && set.add(req[i].getBulk())) count++;

        Value response = new Value("num");
        response.setNum(count);
        return response;
    }

    public Value srem(Value request){
        Value[] req = request.getArray();
        if(req.length < 3) return new Value("num");
        
        int count = 0;
        for(int i = 2; i < req.length; i++)
            if(req[i].getType().equals("bulk") && set.remove(req[i].getBulk())) count++;

        Value response = new Value("num");
        response.setNum(count);
        return response;
    }

    public Value scard(){
        Value response = new Value("num");
        response.setNum(set.size());
        return response;
    }

    public Value smember(){
        Value response = new Value("array");
        Value[] array = new Value[set.size()];

        int i = 0;
        for(String val: set){
            array[i] = new Value("bulk");
            array[i++].setBulk(val);
        }

        response.setArray(array);
        return response;
    }

    public Value sismember(Value request){
        Value[] req = request.getArray();
        if(req.length < 3 || !req[2].getType().equals("bulk")) return new Value("num");
        
        Value response = new Value("num");
        response.setNum(set.contains(req[2].getBulk())? 1 : 0);
        return response;
    }

}