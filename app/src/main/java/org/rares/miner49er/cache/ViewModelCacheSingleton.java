package org.rares.miner49er.cache;

public class ViewModelCacheSingleton {

    private final static ViewModelCache INSTANCE = new ViewModelCache();

    public static ViewModelCache getInstance() {
        return INSTANCE;
    }

    private ViewModelCacheSingleton() {
    }
}