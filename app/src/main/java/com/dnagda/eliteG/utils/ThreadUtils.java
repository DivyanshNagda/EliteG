package com.dnagda.eliteG.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized thread management utility for EliteG.
 * Provides safe threading operations and proper cleanup.
 */
public final class ThreadUtils {
    private static final String TAG = "ThreadUtils";
    
    // Thread pools for different types of operations
    private static final ExecutorService CPU_EXECUTOR = createCpuExecutor();
    private static final ExecutorService IO_EXECUTOR = createIoExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    
    // Prevent instantiation
    private ThreadUtils() {
        throw new AssertionError("ThreadUtils class should not be instantiated");
    }
    
    /**
     * Create optimized CPU-bound executor
     */
    private static ExecutorService createCpuExecutor() {
        int cores = Math.max(2, Runtime.getRuntime().availableProcessors());
        return Executors.newFixedThreadPool(cores, new ThreadFactory() {
            private final AtomicInteger threadCount = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "EliteG-CPU-" + threadCount.getAndIncrement());
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        });
    }
    
    /**
     * Create optimized I/O-bound executor
     */
    private static ExecutorService createIoExecutor() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger threadCount = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "EliteG-IO-" + threadCount.getAndIncrement());
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            }
        });
    }
    
    /**
     * Execute CPU-intensive task on background thread
     */
    public static Future<?> executeCpuTask(Runnable task) {
        return CPU_EXECUTOR.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Logger.e(TAG, "Error in CPU task", e);
            }
        });
    }
    
    /**
     * Execute I/O task on background thread
     */
    public static Future<?> executeIoTask(Runnable task) {
        return IO_EXECUTOR.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Logger.e(TAG, "Error in I/O task", e);
            }
        });
    }
    
    /**
     * Execute task on main thread
     */
    public static void executeOnMainThread(Runnable task) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run();
        } else {
            MAIN_HANDLER.post(task);
        }
    }
    
    /**
     * Execute task on main thread with delay
     */
    public static void executeOnMainThreadDelayed(Runnable task, long delayMs) {
        MAIN_HANDLER.postDelayed(task, delayMs);
    }
    
    /**
     * Check if running on main thread
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
    
    /**
     * CompletableFuture-based async operation with timeout
     */
    public static <T> CompletableFuture<T> executeWithTimeout(
            java.util.function.Supplier<T> task, 
            long timeoutMs) {
        
        CompletableFuture<T> future = CompletableFuture.supplyAsync(task, IO_EXECUTOR);
        
        // Set timeout
        CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
        MAIN_HANDLER.postDelayed(() -> {
            if (!timeoutFuture.isDone()) {
                timeoutFuture.completeExceptionally(
                    new java.util.concurrent.TimeoutException("Operation timed out after " + timeoutMs + "ms")
                );
            }
        }, timeoutMs);
        
        return CompletableFuture.anyOf(future, timeoutFuture)
                .thenApply(result -> (T) result);
    }
    
    /**
     * Safe thread interruption
     */
    public static void safeInterrupt(Thread thread) {
        if (thread != null && thread.isAlive() && !thread.isInterrupted()) {
            try {
                thread.interrupt();
                Logger.d(TAG, "Thread interrupted safely: " + thread.getName());
            } catch (SecurityException e) {
                Logger.w(TAG, "Cannot interrupt thread: " + thread.getName(), e);
            }
        }
    }
    
    /**
     * Cleanup all thread pools
     */
    public static void shutdown() {
        try {
            Logger.d(TAG, "Shutting down thread pools");
            
            shutdownExecutor(CPU_EXECUTOR, "CPU");
            shutdownExecutor(IO_EXECUTOR, "I/O");
            
        } catch (Exception e) {
            Logger.e(TAG, "Error during thread pool shutdown", e);
        }
    }
    
    /**
     * Safely shutdown executor service
     */
    private static void shutdownExecutor(ExecutorService executor, String name) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                Logger.w(TAG, name + " executor did not terminate gracefully, forcing shutdown");
                executor.shutdownNow();
                
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    Logger.e(TAG, name + " executor did not terminate after forced shutdown");
                }
            } else {
                Logger.d(TAG, name + " executor shut down successfully");
            }
        } catch (InterruptedException e) {
            Logger.w(TAG, "Interrupted while shutting down " + name + " executor", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // Register shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadUtils::shutdown, "ThreadUtils-Shutdown"));
    }
}
