public class Queue {
    private char[] data;
    private int front;
    private int rear;
    private int count;
    private int capacity;

    public Queue(int capacity) {
        this.capacity = capacity;
        data = new char[capacity];
        front = 0;
        rear = -1;
        count = 0;
    }

    public void enqueue(char c) {
        if (!isFull()) {
            rear = (rear + 1) % capacity;
            data[rear] = c;
            count++;
        }
    }

    public char dequeue() {
        if (!isEmpty()) {
            char c = data[front];
            front = (front + 1) % capacity;
            count--;
            return c;
        }
        return '\0';
    }

    public char peek() {
        if (!isEmpty()) {
            return data[front];
        }
        return '\0';
    }

    public boolean isFull() {
        return count == capacity;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public int size() {
        return count;
    }
}