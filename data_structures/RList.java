package data_structures;

import java.util.*;
import resp.Value;

public class RList{
    
    private final LinkedList<String> list;

    public RList(){
        list = new LinkedList<>();
    }

    public Value lpush(Value request){
        Value[] req = request.getArray();
        if(req.length < 3) return new Value("num");
        
        for(int i = 2; i < req.length; i++)
            if(req[i].getType().equals("bulk")) list.addFirst(req[i].getBulk());

        Value response = new Value("num");
        response.setNum(list.size());
        return response;
    }

    public Value rpush(Value request){
        System.out.println("rpush here");
        Value[] req = request.getArray();
        if(req.length < 3) return new Value("num");
        
        for(int i = 2; i < req.length; i++)
            if(req[i].getType().equals("bulk")) list.addLast(req[i].getBulk());

        Value response = new Value("num");
        response.setNum(list.size());
        return response;
    }

    public Value llen(){
        Value response = new Value("num");
        response.setNum(list.size());
        return response;
    }

    public Value lpop(){
        if(list.size() == 0) return new Value("");

        Value response = new Value("bulk");
        response.setBulk(list.pollFirst());
        return response;
    }

    public Value rpop(){
        if(list.size() == 0) return new Value("");

        Value response = new Value("bulk");
        response.setBulk(list.pollLast());
        return response;
    }

    public Value lindex(Value request){
        if(list.size() == 0) return new Value("");

        Value[] req = request.getArray();
        
        Value response = new Value("bulk");
        response.setBulk(list.get(Integer.parseInt(req[2].getBulk())));
        return response;
    }

    public Value lrange(Value request){
        if(list.size() == 0) return new Value("");
        Value[] req = request.getArray();
        // if(req.length < 4 || !req[2].getType().equals("num") || !req[3].getType().equals("num")) return new Value("array");

        int i = Integer.parseInt(req[2].getBulk()), j = Integer.parseInt(req[3].getBulk());
        if(i < 0) i = list.size() + i;
        if(j < 0) j = list.size() + j;

        Value response = new Value("array");
        List<Value> arr = new ArrayList<>();

        for(; i <= Math.min(j, list.size() - 1); i++){
            Value val = new Value("bulk");
            val.setBulk(list.get(i));
            arr.add(val);
        }

        response.setArray(arr.toArray(new Value[1]));
        return response;
    }

    public Value ltrim(Value request){
        if(list.size() == 0) return new Value("");

        Value[] req = request.getArray();
        // if(req.length < 4 || !req[2].getType().equals("num") || !req[3].getType().equals("num")) return new Value("string");

        int i = Integer.parseInt(req[2].getBulk()), j = Integer.parseInt(req[3].getBulk());
        if(i < 0) i = list.size() + i;
        if(j < 0) j = list.size() + j;

        List<String> arr = new ArrayList<>();

        for(; i <= Math.min(j, list.size() - 1); i++)
            arr.add(list.get(i));

        // System.out.println(arr, list);
        list.clear();
        list.addAll(arr);

        Value response = new Value("string");
        response.setString("OK");
        return response;
    }

}