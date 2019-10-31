package org.rares.miner49er._abstract;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.network.NetworkingService;
import org.rares.miner49er.persistence.entities.Project;

import java.util.List;

public abstract class Repository {

    private static final String TAG = Repository.class.getSimpleName();

    private NetworkingService ns = NetworkingService.INSTANCE;
    protected CompositeDisposable disposables = null;
    protected PublishProcessor<Byte> userActionProcessor = PublishProcessor.create();
    // ^ static/singleton? if so, remember that concrete classes have userActionProcessor.onComplete in shutdown()
    protected Flowable<Byte> userActionsObservable = userActionProcessor.subscribeOn(Schedulers.io())
//        .doOnSubscribe(x-> System.out.println("repo subscribe " + userActionProcessor.hasSubscribers()))
//        .doOnNext(x-> System.out.println("repo next"))
//        .doOnComplete(()->System.err.println("repo complete"))
//        .doOnError(x-> System.err.println("repo error"))
        .share();

    protected abstract void setup();

    public abstract void shutdown();

    public abstract void registerSubscriber(Consumer<List> consumer);

    protected ItemViewProperties parentProperties = ItemViewProperties.create(Project.class);

    public void setParentProperties(ItemViewProperties ivp) {
        if (ivp.getId() != 0) {
            parentProperties.setId(ivp.getId());
        }
        parentProperties.setItemBgColor(ivp.getItemBgColor());
    }

    // enqueue refresh data
    public void refreshData(boolean onlyLocal) {
        userActionProcessor.onNext(UiEvent.TYPE_CLICK);
    }

    /**
     * Convenience method to show the state of the observables/observers.
     *
     * @return <code>true</code> if the disposables were disposed of.
     * in this case, the disposables should be
     * refreshed and resubscribed if this instance should be reused.
     */
    public boolean isDisposed() {
        return disposables.isDisposed();
    }

    protected void slowDown() {
        for (int i = 0; i < 100; i++) {
            System.out.println(" ... " +
                System.currentTimeMillis() /
                    .6688866688866699 /
                    .448854684798794 *
                    .444444444444444);
        }
    }
}
