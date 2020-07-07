package org.rares.miner49er.ui.custom.functions;


import io.reactivex.annotations.NonNull;

import java.util.Objects;

@FunctionalInterface
public interface AdvancedFunction<T, R> {

    R apply(@NonNull T t);

    default <V> AdvancedFunction<V, R> compose(AdvancedFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> AdvancedFunction<T, V> andThen(AdvancedFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
}
