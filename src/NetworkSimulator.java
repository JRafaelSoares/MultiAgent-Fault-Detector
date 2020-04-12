import java.util.ArrayList;
import java.util.HashMap;

public class NetworkSimulator {
    private HashMap<String, ArrayList<Message>> buffer = new HashMap<>();
    private ArrayList<String> faultDetectors = new ArrayList<>();
    private ArrayList<String> servers = new ArrayList<>();

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

    public void registerServer(String id){
        if(servers.contains(id)){
            System.out.println(id + " is already registered.");
        }
        servers.add(id);
    }

    public void broadcastFDs(Message message){
        for(String id : faultDetectors){
            writeBuffer(id, message);
        }
    }

    public void broadcastServers(Message message){
        for(String id : servers){
            writeBuffer(id, message);
        }
    }
}
