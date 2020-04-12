class Node {

    FaultDetector faultDetector;
    Server server;

    Node nextNode;
    Node prevNode;

    public Node(FaultDetector faultDetector, Server server) {
        this.faultDetector = faultDetector;
        this.server = server;
    }
}

public class CircularList {
    private Node head = null;
    private Node tail = null;
    private int size  = 0;

    public void addNode(FaultDetector faultDetector, Server server) {
        Node newNode = new Node(faultDetector, server);

        Node aux = null;

        if (head == null) {
            head = newNode;
        } else {
            aux = tail;
            tail.nextNode = newNode;
        }

        tail = newNode;
        tail.nextNode = head;

        if(aux != null){
            tail.prevNode = aux;
        }

        head.prevNode = tail;

        size++;
    }

    public Node getNodeByFDid(String faultDetectorID) {
        Node currentNode = head;

        if (head == null) {
            return null;
        } else {
            do {
                if (currentNode.faultDetector.getId().equals(faultDetectorID)) {
                    return currentNode;
                }
                currentNode = currentNode.nextNode;
            } while (currentNode != head);
            return null;
        }
    }

    public int getSize(){
        return size;
    }

    public void print_FD_Server() {
        Node currentNode = head;

        if (head != null) {
            do {
                System.out.println(currentNode.faultDetector.getId() + " " + currentNode.server.getId());
                currentNode = currentNode.nextNode;
            } while (currentNode != head);
        }
    }

}
