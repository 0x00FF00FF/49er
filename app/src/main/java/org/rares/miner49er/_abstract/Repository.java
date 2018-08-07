package org.rares.miner49er._abstract;

import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import io.reactivex.functions.Consumer;
import org.rares.miner49er.persistence.StorioFactory;

import java.util.List;

public abstract class Repository {

    protected StorIOSQLite storio = StorioFactory.INSTANCE.get();
    protected NetworkingService ns = NetworkingService.INSTANCE;

    public abstract void setup();
    public abstract void registerSubscriber(Consumer<List> consumer);
    public abstract void shutdown();
}
