import java.util.ArrayList;
import java.util.HashMap;

public class NetworkSimulator {
    private static HashMap<String, ArrayList<String>> buffer = new HashMap<>();

    public static ArrayList<String> readBuffer(String id){
        if(buffer.containsKey(id)){
            return buffer.remove(id);
        }
        return null;
    }

    public static void writeBuffer(String id, String message){
        if(buffer.containsKey(id)){
            buffer.get(id).add(message);
        }else{
            ArrayList<String> newList = new ArrayList<>();
            newList.add(message);

            buffer.put(id, newList);
        }
    }
}
