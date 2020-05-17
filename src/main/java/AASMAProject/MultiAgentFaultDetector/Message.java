package AASMAProject.MultiAgentFaultDetector;

import java.util.ArrayList;

public class Message {

    public enum Type{
        pingRequest,
        pingResponse,
        serverInfected,
        reviveResponse,
        reviveRequest,
    }

    private String source;
    private String destination;
    private Type type;
    private byte[] content;
    private boolean contagious;

    Message(String source, String destination, Type type, boolean contagious){
        this.source = source;
        this.destination = destination;
        this.type = type;
        this.contagious = contagious;
    }

    Message(String source, String destination, Type type, boolean contagious, byte[] content){
        this(source, destination, type, contagious);
        this.content = content;
    }

    public boolean isContagious() {
        return contagious;
    }

    public String getSource(){
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public Type getType(){
        return type;
    }

    public byte[] getContent(){
        return this.content;
    }
}
