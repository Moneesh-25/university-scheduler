package com.university.scheduler.service;

import com.university.scheduler.concurrency.BoundedTaskQueue;
import com.university.scheduler.concurrency.ConcurrentScheduleStack;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ConcurrencyDemoService {

    public static class DemoResult {
        public int producersCount;
        public int consumersCount;
        public int itemsProduced;
        public int itemsConsumed;
        public int queueCapacity;
        public long durationMs;
        public boolean threadSafeAndLossless;
        public List<String> executionLogs;
        public String dataStructureExplanation;
        public int stackOperationsCount;
        public boolean stackTestPassed;

        public DemoResult(int producersCount, int consumersCount, int itemsProduced, int itemsConsumed,
                          int queueCapacity, long durationMs, boolean threadSafeAndLossless,
                          List<String> executionLogs, String dataStructureExplanation,
                          int stackOperationsCount, boolean stackTestPassed) {
            this.producersCount = producersCount;
            this.consumersCount = consumersCount;
            this.itemsProduced = itemsProduced;
            this.itemsConsumed = itemsConsumed;
            this.queueCapacity = queueCapacity;
            this.durationMs = durationMs;
            this.threadSafeAndLossless = threadSafeAndLossless;
            this.executionLogs = executionLogs;
            this.dataStructureExplanation = dataStructureExplanation;
            this.stackOperationsCount = stackOperationsCount;
            this.stackTestPassed = stackTestPassed;
        }
    }

    /**
     * Executes a live concurrent demonstration with multiple producer and consumer threads.
     */
    public DemoResult runDemonstration(int numProducers, int numConsumers, int itemsPerProducer, int queueCapacity) {
        long startTime = System.currentTimeMillis();
        List<String> logs = new CopyOnWriteArrayList<>();

        BoundedTaskQueue<String> queue = new BoundedTaskQueue<>(queueCapacity);
        int totalExpectedItems = numProducers * itemsPerProducer;

        AtomicInteger producedCounter = new AtomicInteger(0);
        AtomicInteger consumedCounter = new AtomicInteger(0);
        Set<String> producedSet = ConcurrentHashMap.newKeySet();
        Set<String> consumedSet = ConcurrentHashMap.newKeySet();

        ExecutorService executor = Executors.newFixedThreadPool(numProducers + numConsumers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch producerLatch = new CountDownLatch(numProducers);
        CountDownLatch consumerLatch = new CountDownLatch(numConsumers);

        logs.add(String.format("Starting simulation: %d producers, %d consumers, BoundedTaskQueue capacity=%d",
                numProducers, numConsumers, queueCapacity));

        // Start Producers
        for (int p = 1; p <= numProducers; p++) {
            final int producerId = p;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 1; i <= itemsPerProducer; i++) {
                        String taskId = "JOB-P" + producerId + "-T" + i;
                        queue.put(taskId);
                        producedSet.add(taskId);
                        producedCounter.incrementAndGet();
                        if (i % 20 == 0 || i == itemsPerProducer) {
                            logs.add(String.format("[Producer-%d] Enqueued %s (Queue size: %d/%d)",
                                    producerId, taskId, queue.size(), queueCapacity));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producerLatch.countDown();
                }
            });
        }

        // Start Consumers
        for (int c = 1; c <= numConsumers; c++) {
            final int consumerId = c;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    while (true) {
                        String item = queue.poll(200, TimeUnit.MILLISECONDS);
                        if (item != null) {
                            consumedSet.add(item);
                            int count = consumedCounter.incrementAndGet();
                            if (count % 20 == 0 || count == totalExpectedItems) {
                                logs.add(String.format("[Consumer-%d] Processed %s (Total consumed: %d/%d)",
                                        consumerId, item, count, totalExpectedItems));
                            }
                        } else if (producerLatch.getCount() == 0 && queue.isEmpty()) {
                            // All producers done and queue empty
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

        // Fire all threads simultaneously
        startLatch.countDown();

        try {
            producerLatch.await(5, TimeUnit.SECONDS);
            consumerLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdownNow();
        }

        // Demonstrate ConcurrentScheduleStack
        ConcurrentScheduleStack<String> stack = new ConcurrentScheduleStack<>();
        ExecutorService stackExec = Executors.newFixedThreadPool(4);
        CountDownLatch stackLatch = new CountDownLatch(4);
        int stackOpsPerThread = 250;

        for (int s = 0; s < 4; s++) {
            final int id = s;
            stackExec.submit(() -> {
                try {
                    for (int op = 0; op < stackOpsPerThread; op++) {
                        stack.push("SNAPSHOT-T" + id + "-" + op);
                    }
                } finally {
                    stackLatch.countDown();
                }
            });
        }

        try {
            stackLatch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stackExec.shutdownNow();
        }

        boolean stackPassed = (stack.size() == 4 * stackOpsPerThread);
        boolean queuePassed = (producedCounter.get() == consumedCounter.get()) &&
                (consumedSet.size() == totalExpectedItems);

        long duration = Math.max(1, System.currentTimeMillis() - startTime);

        logs.add(String.format("Simulation Completed in %d ms: Produced=%d, Consumed=%d, Lossless=%b",
                duration, producedCounter.get(), consumedCounter.get(), queuePassed));
        logs.add(String.format("ConcurrentStack Test: 4 threads pushed 1000 items concurrently, final size=%d (Passed: %b)",
                stack.size(), stackPassed));

        String explanation = "Data Structures Used: " +
                "1) BoundedTaskQueue: Thread-safe circular array buffer using ReentrantLock(fair=true) " +
                "with dual Conditions (notEmpty, notFull). Time Complexity: O(1) for put() and take(). " +
                "Guarantees backpressure and prevents memory exhaustion. " +
                "2) ConcurrentScheduleStack: Thread-safe stack using ReentrantReadWriteLock allowing concurrent " +
                "readers while enforcing mutual exclusion on mutations. Time Complexity: O(1) for push() and pop().";

        return new DemoResult(
                numProducers,
                numConsumers,
                producedCounter.get(),
                consumedCounter.get(),
                queueCapacity,
                duration,
                queuePassed,
                logs,
                explanation,
                4 * stackOpsPerThread,
                stackPassed
        );
    }
}
