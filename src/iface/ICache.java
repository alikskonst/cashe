package iface;

/**
 * Created by Aleksey Konstantinov on 18.07.2016.
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 */
public interface ICache<K, V> {
    /**
     * Returns the value by the key from the cache
     * @param key the key whose associated value is to be returned
     * @return value if key is cached, else null
     */
    V get(K key);

    /**
     * Prints the keys and values of the cache.
     */
    void print();

    /**
     * Puts the key-value pair into the cache. If the cache contains this key,
     * returns old value, otherwise returns null.
     * @param key key to be associated with value
     * @param value value to be associated with key
     * @return old value if cache contains this key, else null
     */
    V put(K key, V value);

    /**
     * Returns a size of the cache
     * @return size
     */
    int size();
}
