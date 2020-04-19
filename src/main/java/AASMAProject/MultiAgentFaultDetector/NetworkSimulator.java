package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;
import java.util.HashMap;

public class NetworkSimulator {
    private HashMap<String, ArrayList<Message>> buffer = new HashMap<>();
    public ArrayList<Message> readBuffer(String id){
        if(buffer.containsKey(id)){
            return buffer.remove(id);
        }
        return null;
    }

    public void writeBuffer(String id, Message message){
        if(buffer.containsKey(id)){
            buffer.get(id).add(message);
        }else{
            ArrayList<Message> newList = new ArrayList<>();
            newList.add(message);

            buffer.put(id, newList);
        }
    }
}
