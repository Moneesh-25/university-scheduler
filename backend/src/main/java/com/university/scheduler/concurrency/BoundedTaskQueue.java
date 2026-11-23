package com.university.scheduler.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-Safe Bounded Task Queue using ReentrantLock and Dual Conditions (notFull, notEmpty).
 * Implements a classic circular buffer supporting blocking producer-consumer operations.
 *
 * Academic Application:
 * - Decouples HTTP request ingestion from CPU-intensive scheduling calculations.
 * - Enforces bounded backpressure so excessive generation requests do not exhaust system memory.
 *
 * Time Complexity:
 * - put(item): O(1)
 * - take(): O(1)
 * - offer/poll: O(1)
 */
public class BoundedTaskQueue<T> {

    private final Object[] items;
    private int putIndex;
    private int takeIndex;
    private int count;

    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;

    public BoundedTaskQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.items = new Object[capacity];
        this.lock = new ReentrantLock(true); // Fair queuing
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
    }

    /**
     * Inserts the element into the queue, waiting if necessary for space to become available.
     */
    public void put(T item) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("Null elements not permitted");
        }
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                notFull.await();
            }
            enqueue(item);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the element, waiting up to the specified timeout if necessary.
     */
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException("Null elements not permitted");
        }
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (count == items.length) {
                if (nanos <= 0L) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(item);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.
     */
    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            return (T) dequeue();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting up to the specified timeout.
     */
    @SuppressWarnings("unchecked")
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                if (nanos <= 0L) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            return (T) dequeue();
        } finally {
            lock.unlock();
        }
    }

    private void enqueue(T item) {
        items[putIndex] = item;
        if (++putIndex == items.length) {
            putIndex = 0;
        }
        count++;
        notEmpty.signal();
    }

    private Object dequeue() {
        Object item = items[takeIndex];
        items[takeIndex] = null; // facilitate GC
        if (++takeIndex == items.length) {
            takeIndex = 0;
        }
        count--;
        notFull.signal();
        return item;
    }

    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    public int capacity() {
        return items.length;
    }

    public int remainingCapacity() {
        lock.lock();
        try {
            return items.length - count;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return count == 0;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return count == items.length;
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> snapshot() {
        lock.lock();
        try {
            List<T> list = new ArrayList<>(count);
            int idx = takeIndex;
            for (int i = 0; i < count; i++) {
                list.add((T) items[idx]);
                if (++idx == items.length) {
                    idx = 0;
                }
            }
            return list;
        } finally {
            lock.unlock();
        }
    }
}
