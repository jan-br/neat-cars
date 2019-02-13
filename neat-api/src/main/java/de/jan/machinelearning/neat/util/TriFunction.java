package de.jan.machinelearning.neat.util;

/**
 * Created by DevTastisch on 12.02.2019
 */
public interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
}
