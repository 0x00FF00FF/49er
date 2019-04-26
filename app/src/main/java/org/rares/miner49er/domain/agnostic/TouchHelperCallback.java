package org.rares.miner49er.domain.agnostic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.Messenger;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.AbstractAdapter;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;

import java.util.concurrent.TimeUnit;

import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class TouchHelperCallback<VH extends ResizeableItemViewHolder, VM extends AbstractViewModel> extends ItemTouchHelper.SimpleCallback {

    private static final String TAG = TouchHelperCallback.class.getSimpleName();

    @Setter
    private AbstractAdapter<VH, VM> adapter;
    @Setter
    private AsyncGenericDao<VM> dao;
    @Setter
    private SwipeDeletedListener deletedListener;

    public TouchHelperCallback() {
        super(0, RIGHT);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View v = viewHolder.itemView;
        float alpha = 2 * v.getTranslationX() / v.getWidth();
        Drawable remove = recyclerView.getContext().getResources().getDrawable(R.drawable.icon_path_delete);
        remove.setBounds(
                48,
                v.getTop() + v.getHeight() / 2 - remove.getIntrinsicHeight() / 2,
                48 + remove.getIntrinsicWidth(),
                v.getTop() + v.getHeight() / 2 + remove.getIntrinsicHeight() / 2);
        if (isCurrentlyActive) {
            remove.setAlpha((int) (alpha > 1 ? 255 : alpha * 255));
            remove.draw(c);
        }
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder, @NonNull ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull ViewHolder viewHolder, int direction) {
        Context context = viewHolder.itemView.getContext();
        final VM vm = adapter.getDisplayData(viewHolder.getAdapterPosition());
        vm.setDeleted(true);

        final CompositeDisposable disposables = new CompositeDisposable();
        final Disposable pseudoDeleteDisposable = dao.update(vm)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(x -> {
                    if (deletedListener != null) {
                        deletedListener.onItemPseudoDeleted(viewHolder);
                    }
                    Log.i(TAG, "onSwiped: pseudo-removed? " + x);
                });
        final Disposable disposable = Single.just(0)
                .delay(3, TimeUnit.SECONDS)
                .subscribe((s) -> disposables.add(dao.delete(vm)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(x -> {
                            if (deletedListener != null) {
                                deletedListener.onItemDeleted(viewHolder);
                            }
                            Log.i(TAG, "onSwiped: removed? " + x);
                        })));

        disposables.add(pseudoDeleteDisposable);
        disposables.add(disposable);

        Action action = () -> {
            vm.setDeleted(false);
            disposables.add(dao.update(vm).subscribe((updated) -> disposable.dispose()));   ////
        };

        ((Messenger) context).showMessage(
                context.getResources().getString(R.string.entry_removed),
                Messenger.UNDOABLE,
                Completable.fromAction(action));
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull ViewHolder viewHolder) {
        boolean canSwipe = false;
        AbstractAdapter adapter = (AbstractAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            canSwipe = adapter.canRemoveItem(viewHolder.getAdapterPosition());
        }
        return canSwipe ? RIGHT : 0;
    }

    public interface SwipeDeletedListener {
        void onItemPseudoDeleted(ViewHolder vh);

        void onItemDeleted(ViewHolder vh);
    }
}
