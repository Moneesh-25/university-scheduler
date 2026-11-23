package com.university.scheduler;

import com.university.scheduler.concurrency.BoundedTaskQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BoundedTaskQueueTest {

    @Test
    @DisplayName("BoundedTaskQueue should enforce capacity limit and block on bounds")
    void testCapacityBounds() throws InterruptedException {
        BoundedTaskQueue<String> queue = new BoundedTaskQueue<>(2);
        assertEquals(2, queue.capacity());
        assertEquals(2, queue.remainingCapacity());

        queue.put("TASK-1");
        queue.put("TASK-2");

        assertTrue(queue.isFull());
        assertEquals(0, queue.remainingCapacity());

        boolean offered = queue.offer("TASK-3", 100, TimeUnit.MILLISECONDS);
        assertFalse(offered, "Should time out and reject item when queue is full");

        assertEquals("TASK-1", queue.take());
        assertEquals(1, queue.remainingCapacity());
        assertFalse(queue.isFull());
    }

    @Test
    @DisplayName("BoundedTaskQueue should handle multi-producer multi-consumer safely without lost elements")
    void testConcurrentProducerConsumer() throws InterruptedException {
        int capacity = 10;
        BoundedTaskQueue<String> queue = new BoundedTaskQueue<>(capacity);

        int producers = 4;
        int consumers = 4;
        int itemsPerProducer = 200;
        int totalItems = producers * itemsPerProducer;

        Set<String> consumedSet = ConcurrentHashMap.newKeySet();
        AtomicInteger consumedCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(producers + consumers);
        CountDownLatch producerLatch = new CountDownLatch(producers);
        CountDownLatch consumerLatch = new CountDownLatch(consumers);

        // Producers
        for (int p = 0; p < producers; p++) {
            final int pId = p;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < itemsPerProducer; i++) {
                        queue.put("P" + pId + "-ITEM-" + i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producerLatch.countDown();
                }
            });
        }

        // Consumers
        for (int c = 0; c < consumers; c++) {
            executor.submit(() -> {
                try {
                    while (true) {
                        String item = queue.poll(150, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            consumedSet.add(item);
                            consumedCount.incrementAndGet();
                        } else if (producerLatch.getCount() == 0 && queue.isEmpty()) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    consumerLatch.countDown();
                }
            });
        }

        boolean producersDone = producerLatch.await(5, TimeUnit.SECONDS);
        boolean consumersDone = consumerLatch.await(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertTrue(producersDone, "Producers should finish within 5s");
        assertTrue(consumersDone, "Consumers should finish within 5s");
        assertEquals(totalItems, consumedCount.get(), "All produced items must be consumed");
        assertEquals(totalItems, consumedSet.size(), "No duplicated items in consumed set");
    }
}
