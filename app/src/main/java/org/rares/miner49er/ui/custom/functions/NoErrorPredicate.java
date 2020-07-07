package org.rares.miner49er.ui.custom.functions;

import io.reactivex.annotations.NonNull;

@FunctionalInterface
public interface NoErrorPredicate<T> {

    boolean test(@NonNull T t);
}
