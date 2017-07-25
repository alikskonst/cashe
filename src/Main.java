import iface.ICache;
import impls.LFUCache;
import impls.LRUCache;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class with main() method
 */
public final class Main {
    private static final int NUMBER_OF_THREADS = 5;
    private static final int INITIAL_CAPACITY = 4;
    private static final float LOAD_FACTOR = 0.75f;
    private static final float EVICTION_FACTOR = 0.8f;
    private static final boolean ACCESS_ORDER = true;
    private static final int SEMAPHORE_INIT = 1;
    private static final int RANDOM_LIMIT = 30;

    /**
     * Creates and use two kinds of cache
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        ICache<Integer, Integer> lruCache = new LRUCache<>(INITIAL_CAPACITY, LOAD_FACTOR, ACCESS_ORDER);
        ICache<Integer, Integer> lfuCache = new LFUCache<>(INITIAL_CAPACITY, EVICTION_FACTOR);
        useCache(lruCache);
        useCache(lfuCache);
    }

    private Main() { }
    private static void useCache(final ICache<Integer, Integer> cache) {
        AtomicInteger increment = new AtomicInteger();
        Semaphore semaphore = new Semaphore(SEMAPHORE_INIT);
        ExecutorService executor = Executors.newCachedThreadPool();
        Random random = new Random();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.printf("Thread number: %d%n", increment.addAndGet(1));

                for (int i = 0; i < INITIAL_CAPACITY * 2; i++) {
                    Integer value = random.nextInt(RANDOM_LIMIT);
                    cache.put(i, value);
                    cache.get(i);
                }
                cache.print();
                semaphore.release();
            }
        };

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            executor.submit(task);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
