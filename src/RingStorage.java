import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RingStorage<E>
{
    private class Node<E>
    {
        private E value;

        private Node previous;
        private Node next;

        public Node(E value)
        {
            this.value = value;
            this.previous = null;
            this.next = null;
        }

        public Node(E value, Node previous, Node next)
        {
            this.value = value;
            this.previous = previous;
            this.next = next;
        }

        public E getValue() {
            return value;
        }

        public void setValue(E value) {
            this.value = value;
        }

        public Node getPrevious() {
            return previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public String toString()
        {
            return ""+value.toString();
        }
    }

    private List<Node> storage;
    private Node currElem;

    private int index;

    public RingStorage(RingStorage<E> copyRS)
    {
        this.storage = copyRS.getStorage();
        this.currElem = copyRS.getCurrElem();
        this.index = copyRS.getIndex();
    }

    public RingStorage()
    {
        this.storage = new LinkedList<>();
        this.index = 0;

        this.currElem = null;
    }

    public void addNode(E elem)
    {
        if(storage.isEmpty())
        {
            Node node = new Node(elem);
            node.setPrevious(node);
            node.setNext(node);

            this.storage.add(node);
            this.currElem = node;
        }
        else
        {
            Node newElement = new Node(elem, storage.get(storage.size() - 1), storage.get(0));

            storage.get(storage.size() - 1).setNext(newElement);
            storage.get(0).setPrevious(newElement);

            storage.add(newElement);
        }
    }

    public E getHead()
    {
        currElem = this.storage.get(index);
        //currElem = currElem.getNext();

        return (E) this.storage.get(index).getValue();
    }

    public E peekNext()
    {
        return (E) currElem.getNext().getValue();
    }

    public E peekPrevious()
    {
        return (E) currElem.getPrevious().getValue();
    }
    public E getNext()
    {
        currElem = currElem.getNext();
        E tmpElem = (E) currElem.getValue();

        return tmpElem;
    }

    public E getPrevious()
    {
        currElem = currElem.getPrevious();
        E tmpElem = (E) currElem.getValue();

        return tmpElem;
    }

    public void incrementIndex()
    {
        if(index == this.storage.size()-1)
            index = 0;
        else
            ++index;
    }

    public List<Node> getStorage() {
        return storage;
    }

    public Node getCurrElem() {
        return currElem;
    }

    public int getIndex() {
        return index;
    }

    public String toString()
    {
        String content = "content:\n";

        for(Node node : storage)
            content += node.toString()+"\n";

        return content;
    }
}
