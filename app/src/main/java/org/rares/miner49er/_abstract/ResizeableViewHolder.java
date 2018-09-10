package org.rares.miner49er._abstract;

import android.animation.Animator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.ListItemEventListener;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * @author rares
 * @since 02.03.2018
 */

// TODO: 4/23/18 this is going to be refactored. remove or rethink the item view properties.
public abstract class ResizeableViewHolder
        extends RecyclerView.ViewHolder
        implements
        Unbinder,
        View.OnClickListener {

    private static final String TAG = ResizeableViewHolder.class.getSimpleName();

    @Getter
    @Setter
    Animator animator = null;   // perhaps extend valueAnimator and provide only ofPVH + rename to avoid confusion

    @Setter
    private ListItemEventListener itemClickListener;

    @Getter
    @Setter
    ItemViewProperties itemProperties;

    @Getter
    @Setter
    private boolean toBeRebound = true;

    private Unbinder unbinder;

    public abstract void bindData(Object data, boolean shortVersion);

    public ResizeableViewHolder(View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
        ButterKnife.bind(this, itemView);

//       Miner49erApplication.getRefWatcher(itemView.getContext()).watch(itemView);
    }

    @Override
    public void onClick(View v) {
//        Log.d(TAG, "onClick: " + v.toString() + "; adapter position: " + getAdapterPosition());
        if (getAdapterPosition() != NO_POSITION) {
            itemClickListener.onListItemClick(this);
        }
    }

    @Override
    public void unbind() {
        unbinder.unbind();
    }
}
