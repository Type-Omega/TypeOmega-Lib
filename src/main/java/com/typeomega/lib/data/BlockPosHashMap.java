package com.typeomega.lib.data;

import net.minecraft.core.BlockPos;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link HashMap} whose entries live in a {@link BlockPosHashing} map. Some GTCEu code casts a map it reads from a
 * context to {@code HashMap} (the {@code "predicates"} entry of the pattern match context), so a plain fastutil map
 * cannot stand in for it; this keeps the {@code HashMap} type and none of its position hashing. Insertion ordered.
 *
 * <p>
 * Every {@code HashMap} method is routed to the delegate; the inherited table is never allocated.
 */
public final class BlockPosHashMap<V> extends HashMap<BlockPos, V> {

    private final Object2ObjectLinkedOpenCustomHashMap<BlockPos, V> delegate = BlockPosHashing.newMap();

    public BlockPosHashMap() {
        super(1);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public V put(BlockPos key, V value) {
        return delegate.put(key, value);
    }

    @Override
    public void putAll(Map<? extends BlockPos, ? extends V> m) {
        delegate.putAll(m);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Set<BlockPos> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Map.Entry<BlockPos, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public V putIfAbsent(BlockPos key, V value) {
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return delegate.remove(key, value);
    }

    @Override
    public boolean replace(BlockPos key, V oldValue, V newValue) {
        return delegate.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(BlockPos key, V value) {
        return delegate.replace(key, value);
    }

    @Override
    public V computeIfAbsent(BlockPos key, Function<? super BlockPos, ? extends V> mappingFunction) {
        return delegate.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(BlockPos key, BiFunction<? super BlockPos, ? super V, ? extends V> remapping) {
        return delegate.computeIfPresent(key, remapping);
    }

    @Override
    public V compute(BlockPos key, BiFunction<? super BlockPos, ? super V, ? extends V> remapping) {
        return delegate.compute(key, remapping);
    }

    @Override
    public V merge(BlockPos key, V value, BiFunction<? super V, ? super V, ? extends V> remapping) {
        return delegate.merge(key, value, remapping);
    }

    @Override
    public void forEach(BiConsumer<? super BlockPos, ? super V> action) {
        delegate.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super BlockPos, ? super V, ? extends V> function) {
        delegate.replaceAll(function);
    }

    @Override
    public int hashCode() {
        // The delegate's entries hash keys with the position strategy; Map.hashCode is defined on key.hashCode().
        int hash = 0;
        for (Map.Entry<BlockPos, V> entry : delegate.entrySet()) {
            hash += entry.getKey().hashCode() ^ (entry.getValue() == null ? 0 : entry.getValue().hashCode());
        }
        return hash;
    }

    @Override
    public Object clone() {
        BlockPosHashMap<V> copy = new BlockPosHashMap<>();
        copy.delegate.putAll(delegate);
        return copy;
    }
}
