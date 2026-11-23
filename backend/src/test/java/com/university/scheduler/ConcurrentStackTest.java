package com.university.scheduler;

import com.university.scheduler.concurrency.ConcurrentScheduleStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentStackTest {

    @Test
    @DisplayName("ConcurrentStack should support single-threaded basic operations correctly")
    void testBasicStackOperations() {
        ConcurrentScheduleStack<String> stack = new ConcurrentScheduleStack<>();
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());

        stack.push("BATCH-1");
        stack.push("BATCH-2");

        assertFalse(stack.isEmpty());
        assertEquals(2, stack.size());
        assertEquals("BATCH-2", stack.peek());
        assertEquals("BATCH-2", stack.pop());
        assertEquals("BATCH-1", stack.pop());
        assertTrue(stack.isEmpty());
        assertThrows(EmptyStackException.class, stack::pop);
    }

    @Test
    @DisplayName("ConcurrentStack should be thread-safe under concurrent push operations")
    void testConcurrentPushOperations() throws InterruptedException {
        ConcurrentScheduleStack<Integer> stack = new ConcurrentScheduleStack<>();
        int threadsCount = 8;
        int itemsPerThread = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadsCount);

        for (int t = 0; t < threadsCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < itemsPerThread; i++) {
                        stack.push(threadId * itemsPerThread + i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = endLatch.await(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertTrue(finished);
        assertEquals(threadsCount * itemsPerThread, stack.size(), "Stack size must match total items pushed across all threads");
    }
}
