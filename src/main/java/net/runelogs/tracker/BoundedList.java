package net.runelogs.tracker;

import java.util.LinkedList;

public class BoundedList<T> extends LinkedList<T> {
    private final int maxSize;

    public BoundedList(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(T element) {
        if (size() >= maxSize) {
            removeFirst();
        }
        return super.add(element);
    }
}