package de.jan.machinelearning.neat.data;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by DevTastisch on 09.02.2019
 */
public class ListHashMap<K, V> implements Map<K, List<V>>, Iterable<V>, Serializable {

    private final Map<K, List<V>> internal = new LinkedHashMap<>();
    private final List<V> fastList = new LinkedList<>();

    public Iterator<V> iterator() {
        return this.fastList.iterator();
    }

    public int size() {
        return this.fastList.size();
    }

    public boolean isEmpty() {
        return this.fastList.isEmpty();
    }

    public boolean containsKey(Object key) {
        return this.internal.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.fastList.contains(value);
    }

    public List<V> get(Object key) {
        if(!this.internal.containsKey(key)) {
            this.put(((K) key), new LinkedList<>());
        }
        return this.internal.get(key);
    }

    public List<V> put(K key, List<V> value) {
        this.fastList.addAll(value);
        return this.internal.put(key, value);
    }

    public void add(K key, V value){
        this.fastList.add(value);
        this.get(key).add(value);
    }

    public List<V> asList(){
        return new LinkedList<>(this.fastList);
    }

    public List<V> remove(Object key) {
        this.fastList.remove(key);
        return this.internal.remove(key);
    }

    public void putAll(Map<? extends K, ? extends List<V>> m) {
        this.internal.putAll(m);
        this.fastList.addAll(m.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }

    public void clear() {
        this.internal.clear();
        this.fastList.clear();
    }

    public Set<K> keySet() {
        return this.internal.keySet();
    }

    public Collection<List<V>> values() {
        return this.internal.values();
    }

    public Set<Entry<K, List<V>>> entrySet() {
        return this.internal.entrySet();
    }

    public Stream<V> stream() {
        return this.fastList.stream();
    }

}
