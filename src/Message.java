public class Message {
    enum Type{
        pingRequest,
        pingResponse,
        serverCrashed,
        serverNotCrashed
    }
    private String id;
    private Type type;

    Message(String id, Type type){
        this.id = id;
        this.type = type;
    }

    public String getId(){
        return id;
    }

    public Type getType(){
        return type;
    }

}
