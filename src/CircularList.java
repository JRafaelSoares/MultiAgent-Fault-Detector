class Node {

    FaultDetector faultDetector;
    Server server;

    Node nextNode;

    public Node(FaultDetector faultDetector, Server server) {
        this.server = server;
        this.faultDetector = faultDetector;
    }
}

public class CircularList {
    private Node head = null;
    private Node tail = null;
    private int size  = 0;

    public void addNode(FaultDetector faultDetector, Server server) {
        Node newNode = new Node(faultDetector, server);

        if (head == null) {
            head = newNode;
        } else {
            tail.nextNode = newNode;
        }

        tail = newNode;
        tail.nextNode = head;
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

    public void print() {
        Node currentNode = head;

        if (head != null) {
            do {
                System.out.println(currentNode.faultDetector.getId() + " " + currentNode.server.getId());
                currentNode = currentNode.nextNode;
            } while (currentNode != head);
        }
    }

}
