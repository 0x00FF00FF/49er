package org.rares.miner49er._abstract;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.rares.miner49er.BaseInterfaces.ListItemClickListener;
import org.rares.miner49er.BaseInterfaces.UnbinderHost;
import org.rares.miner49er.R;
import org.rares.miner49er.entries.adapter.TimeEntriesViewHolder;
import org.rares.miner49er.issues.adapter.IssuesViewHolder;
import org.rares.miner49er.projects.adapter.ProjectsViewHolder;

import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * @author rares
 * @since 02.03.2018
 */

// TODO: 07.03.2018 use shared recycledViewPool
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

    @BindDimen(R.dimen.projects_rv_collapsed_width)
    int itemCollapsedWidth;

    @BindDimen(R.dimen.projects_rv_collapsed_selected_item_width)
    int itemCollapsedSelectedWidth;

    @Setter
    private int maxItemElevation = 0;

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
        if (forceExpand) {
            expandViewContainer(true);
        } else {
            if (getItemProperties().isSelected()) {
                expandViewContainer(false);
            } else {
                collapseViewContainer();
            }
        }
    }


    private void collapseViewContainer() {
//        projectName.setEllipsize(TextUtils.TruncateAt.END);
        itemView.getLayoutParams().width = itemCollapsedWidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            itemView.setElevation(0);
        }
    }

    private void expandViewContainer(boolean expandToMatchParent) {
//        projectName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        if (expandToMatchParent) {
            itemView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setElevation(0);
            }
        } else {
            itemView.getLayoutParams().width = itemCollapsedSelectedWidth;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                itemView.setElevation(20);
            }
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: " + v.toString() + "; adapter position: " + getAdapterPosition());
        if (getAdapterPosition() != NO_POSITION) {
            itemClickListener.onListItemClick(getItemProperties());
        }
    }

    @Override
    public void unbind() {
        unbinder.unbind();
    }
}
