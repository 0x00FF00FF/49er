package org.rares.miner49er._abstract;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.rares.miner49er.BaseInterfaces.ListItemClickListener;
import org.rares.miner49er.BaseInterfaces.UnbinderHost;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * @author rares
 * @since 02.03.2018
 */


public abstract class ResizeableViewHolder
        extends RecyclerView.ViewHolder
        implements
        Unbinder,
        View.OnClickListener {

    private static final String TAG = ResizeableViewHolder.class.getSimpleName();

    @Setter
    private ListItemClickListener itemClickListener;

    @Getter
    @Setter
    ItemViewProperties itemProperties;

    @Getter
    @Setter
    private boolean toBeRebound = true;

    private Unbinder unbinder;

    public abstract void bindData(Object data);

    public ResizeableViewHolder(View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this, itemView);
        ((UnbinderHost) itemView.getContext()).registerUnbinder(unbinder);
        itemView.setOnClickListener(this);
        ButterKnife.bind(this, itemView);
    }

    public void resizeItemView(boolean forceExpand) {
//        if (forceExpand) {
//            expandViewContainer(true);
//        } else {
//            if (getItemProperties().isSelected()) {
//                expandViewContainer(false);
//            } else {
//                collapseViewContainer();
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: " + v.toString() + "; adapter position: " + getAdapterPosition());
        if (getAdapterPosition() != NO_POSITION) {
            itemClickListener.onListItemClick(this);
        }
    }

    @Override
    public void unbind() {
        unbinder.unbind();
    }
}
