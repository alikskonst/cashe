package impls;

import iface.ICache;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Aleksey Konstantinov on 31.07.2016.
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 */
public class LFUCache<K, V> implements ICache<K, V> {
    private static final int INIT_INDEX = 0;
    private int minFrequency;
    private float evictionFactor;

    private Map<K, V> cache;
    private Map<K, Integer> keyFrequencies;
    private List<K>[] frequencies;

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();
    private Condition writeCondition = writeLock.newCondition();

    /**
     * Constructor using fields
     * @param size the size of the cache
     * @param evictionFactor the eviction factor that using to
     *                       count number of elements to evict
     */
    public LFUCache(final int size, final float evictionFactor) {
        this.evictionFactor = evictionFactor;
        cache = new HashMap<>(size);
        keyFrequencies = new HashMap<>();
        frequencies = new LinkedList[size + 1];
        initFrequencies();
    }

    @Override
    public V get(final K key) {
        if (cache.containsKey(key)) {
            increaseFrequency(key);
        }
        readLock.lock();
        try {
            return cache.get(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void print() {
        synchronized (this.getClass()) {
            for (int i = INIT_INDEX; i < frequencies.length; i++) {
                System.out.println("Frequency number " + i);
                for (K key : frequencies[i]) {
                    System.out.printf("Key: %s; Value: %s%n", key, cache.get(key));
                }
            }
            System.out.println();
        }
    }

    @Override
    public V put(final K key, final V value) {
        V oldValue = null;
        writeLock.lock();
        try {
            if (!cache.containsKey(key)) {
                if (size() == frequencies.length - 1) {
                    doEviction();
                }
                frequencies[INIT_INDEX].add(key);
                keyFrequencies.put(key, INIT_INDEX);
                cache.put(key, value);
                minFrequency = 0;
            } else {
                oldValue = cache.get(key);
                cache.replace(key, value);
            }
            return oldValue;
        } finally {
            writeCondition.signalAll();
            writeLock.unlock();
        }
    }

    @Override
    public int size() {
        return cache.size();
    }

    private void doEviction() {
        writeLock.lock();
        try {
            int currentlyDeleted = 0;
            float target = Math.round(size() * evictionFactor);
            while (currentlyDeleted < target) {
                for (List list : frequencies) {
                    if (!list.isEmpty()) {
                        Iterator<K> iterator = list.iterator();
                        while (iterator.hasNext() && currentlyDeleted++ < target) {
                            K key = iterator.next();
                            iterator.remove();
                            cache.remove(key);
                            keyFrequencies.remove(key);
                        }
                    }
                }
            }
        } finally {
            writeCondition.signalAll();
            writeLock.unlock();
        }
    }

    private void increaseFrequency(final K key) {
        writeLock.lock();
        try {
            int currentFrequency = keyFrequencies.get(key);
            int nextFrequency;
            if (currentFrequency == size()) {
                nextFrequency = currentFrequency;
            } else {
                nextFrequency = currentFrequency + 1;
            }
            if (minFrequency > nextFrequency) {
                minFrequency = nextFrequency;
            }
            keyFrequencies.put(key, nextFrequency);
            frequencies[currentFrequency].remove(key);
            frequencies[nextFrequency].add(key);
        } finally {
            writeCondition.signalAll();
            writeLock.unlock();
        }
    }

    private void initFrequencies() {
        for (int i = INIT_INDEX; i < frequencies.length; i++) {
            frequencies[i] = new LinkedList<>();
        }
    }
}
