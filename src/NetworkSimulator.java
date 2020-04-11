import java.util.ArrayList;
import java.util.HashMap;

public class NetworkSimulator {
    private HashMap<String, ArrayList<Message>> buffer = new HashMap<>();
    private ArrayList<String> faultDetectors = new ArrayList<>();

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

    public void registerFD(String id){
        if(faultDetectors.contains(id)){
            System.out.println(id + " is already registered.");
        }
        faultDetectors.add(id);
    }

    public void broadcast(Message message){
        for(String id : faultDetectors){
            writeBuffer(id, message);
        }
    }
}
