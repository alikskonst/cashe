package impls;

import iface.ICache;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Aleksey Konstantinov on 19.07.2016.
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 */
public class LRUCache<K, V> implements ICache<K, V> {
    private Map<K, V> cache;

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();
    private Condition writeCondition = writeLock.newCondition();

    /**
     * Constructor using fields
     * @param initialCapacity the initial capacity of the cache
     * @param loadFactor the load factor
     * @param accessOrder the access order - true for access-order, false for insertion-order
     */
    public LRUCache(final int initialCapacity, final float loadFactor, final boolean accessOrder) {
        this.cache = new LinkedHashMap<K, V>(initialCapacity, loadFactor, accessOrder) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
                return size() > initialCapacity;
            }
        };
    }

    @Override
    public V get(final K key) {
        readLock.lock();
        try {
            return cache.get(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Prints the keys and values of the cache.
     */
    public void print() {
        readLock.lock();
        try {
            for (Map.Entry entry : cache.entrySet()) {
                System.out.printf("Key: %s; Value: %s%n", entry.getKey(), entry.getValue());
            }
            System.out.println();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public V put(final K key, final V value) {
        V oldValue = null;
        writeLock.lock();
        try {
            if (cache.containsKey(key)) {
                oldValue = cache.get(key);
            }
            cache.put(key, value);
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
}
