public class Message {
    enum Type{
        pingRequest,
        pingResponse,
        serverCrashed,
        serverNotCrashed,
        revived
    }
    private String id;
    private String idTarget;
    private Type type;

    Message(String id, Type type){
        this.id = id;
        this.type = type;
    }

    Message(String id, String idTarget, Type type){
        this.id = id;
        this.idTarget = idTarget;
        this.type = type;
    }


    public String getId(){
        return id;
    }

    public Type getType(){
        return type;
    }

    public String getIdTarget(){
        return idTarget;
    }

}
