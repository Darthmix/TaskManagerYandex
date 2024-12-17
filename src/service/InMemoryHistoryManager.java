package service;

import model.*;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomDoublyLinkedList<Task> historyTasks; // История изменений
    private final Map<Integer, Node<Task>> linkedHashList;

    public InMemoryHistoryManager() {
        this.historyTasks = new CustomDoublyLinkedList<Task>();
        this.linkedHashList = new HashMap<>();
    }

    @Override
    public void remove(int id) {
        Node<Task> taskNode = linkedHashList.get(id);
        if (taskNode == null) return;
        historyTasks.removeNode(taskNode);
        linkedHashList.remove(id);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        if (linkedHashList.containsKey(task.getId())) {
            historyTasks.removeNode(linkedHashList.get(task.getId()));
        }
        historyTasks.linkLast(task);
        linkedHashList.put(task.getId(), historyTasks.tail);

    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(historyTasks.getList());
    }

    class Node<T> {

        public T data;
        public Node<T> next;
        public Node<T> prev;

        public Node(T data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }

        public Node(Node<T> prev, T data, Node<T> next) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }

    public class CustomDoublyLinkedList<T> {
        public Node<T> head;
        public Node<T> tail;
        private int size = 0;

        public void linkLast(T element) {
            final Node<T> newNode;
            if (head == null) {
                newNode = new Node<>(element);
                head = newNode;
            } else {
                final Node<T> oldTail = tail;
                newNode = new Node<>(oldTail, element, null);
                oldTail.next = newNode;
            }
            tail = newNode;
            size++;
        }

        public void removeNode(Node<T> node) {
            if (node == null || head == null) return;
            if (head == node) {
                head = head.next;
                if (head != null) {
                    head.prev = null;
                } else {
                    tail = null;
                }
            } else {
                if (node.next != null) node.next.prev = node.prev;
                if (node.prev != null) node.prev.next = node.next;
            }
            if (tail == node) tail = node.prev;
            node.prev = null;
            node.next = null;
            size--;
        }

        public List<T> getList() {
            List<T> result = new ArrayList<>();
            Node<T> currentNode = head;
            while (currentNode != null) {
                result.add(currentNode.data);
                currentNode = currentNode.next;
            }
            return result;
        }

    }
}