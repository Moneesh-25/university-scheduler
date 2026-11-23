package com.university.scheduler.concurrency;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-Safe Concurrent Stack for Schedule Operations & Undo Rollback.
 * Demonstrates safe concurrent push, pop, and snapshot operations using
 * ReentrantReadWriteLock to allow multiple concurrent readers while ensuring
 * exclusive writes.
 *
 * Academic Application:
 * - Maintains historical schedule modification snapshots.
 * - Enables transactional rollback / undo for administrative changes.
 *
 * Time Complexity:
 * - push(E): O(1) amortized
 * - pop(): O(1)
 * - peek(): O(1)
 * - size(): O(1)
 */
public class ConcurrentScheduleStack<T> {

    private static class Node<E> {
        final E item;
        final Node<E> next;

        Node(E item, Node<E> next) {
            this.item = item;
            this.next = next;
        }
    }

    private Node<T> head = null;
    private int size = 0;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true); // fair lock
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public void push(T item) {
        if (item == null) {
            throw new NullPointerException("Null items are not allowed in ConcurrentScheduleStack");
        }
        writeLock.lock();
        try {
            head = new Node<>(item, head);
            size++;
        } finally {
            writeLock.unlock();
        }
    }

    public T pop() {
        writeLock.lock();
        try {
            if (head == null) {
                throw new EmptyStackException();
            }
            T val = head.item;
            head = head.next;
            size--;
            return val;
        } finally {
            writeLock.unlock();
        }
    }

    public T peek() {
        readLock.lock();
        try {
            if (head == null) {
                throw new EmptyStackException();
            }
            return head.item;
        } finally {
            readLock.unlock();
        }
    }

    public int size() {
        readLock.lock();
        try {
            return size;
        } finally {
            readLock.unlock();
        }
    }

    public boolean isEmpty() {
        readLock.lock();
        try {
            return size == 0;
        } finally {
            readLock.unlock();
        }
    }

    public void clear() {
        writeLock.lock();
        try {
            head = null;
            size = 0;
        } finally {
            writeLock.unlock();
        }
    }

    public List<T> toList() {
        readLock.lock();
        try {
            List<T> list = new ArrayList<>(size);
            Node<T> current = head;
            while (current != null) {
                list.add(current.item);
                current = current.next;
            }
            return list;
        } finally {
            readLock.unlock();
        }
    }
}
