public class Stack {
    private char[] data;
    private int top;
    private int capacity;

    public Stack(int capacity) {
        this.capacity = capacity;
        data = new char[capacity];
        top = -1;
    }

    public void push(char c) {
        if (!isFull()) {
            data[++top] = c;
        }
    }

    public char pop() {
        if (!isEmpty()) {
            return data[top--];
        }
        return '\0';
    }

    public char peek() {
        if (!isEmpty()) {
            return data[top];
        }
        return '\0';
    }

    public boolean isFull() {
        return top == capacity - 1;
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public int size() {
        return top + 1;
    }
}