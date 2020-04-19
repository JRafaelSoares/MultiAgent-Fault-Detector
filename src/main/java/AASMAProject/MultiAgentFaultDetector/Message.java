package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;

public class Message {
    public enum Type{
        pingRequest,
        pingResponse,
        serverCrashed,
        serverNotCrashed,
        revived,
        reviveRequest,
        //for estatistics
        serverStateRequest,
        serverStateResponse
    }
    private String id;
    private Type type;
    private ArrayList<String> list;
    private State state;

    public Message(String id, Type type){
        this.id = id;
        this.type = type;
    }

    Message(String id, Type type, ArrayList<String> list){
        this.id = id;
        this.type = type;
        this.list = list;
    }

    Message(String id, Type type, State state){
        this.id = id;
        this.type = type;
        this.state = state;
    }


    public String getId(){
        return id;
    }

    public Type getType(){
        return type;
    }

    public ArrayList<String> getList(){
        return this.list;
    }

    public State getState(){
        return this.state;
    }
}
