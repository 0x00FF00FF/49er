package org.rares.miner49er.ui.custom.functions;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * Use a bi-consumer with a fixed value, instead of a consumer.
 * https://stackoverflow.com/a/31047513
 */
public class Binder {
    public static <A, B> Consumer<A> bindLast(BiConsumer<A, B> fn, B b) {
        return a -> fn.accept(a, b);
    }

    public static <A, B> Consumer<B> bindFirst(BiConsumer<A, B> fn, A a) {
        return b -> fn.accept(a, b);
    }
}