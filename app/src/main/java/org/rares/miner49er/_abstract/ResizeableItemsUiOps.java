package org.rares.miner49er._abstract;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er.util.NumberUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author rares
 * @since 02.03.2018
 */

public abstract class ResizeableItemsUiOps
        implements
        Unbinder,
        BaseInterfaces.ListItemClickListener,
        BaseInterfaces.ResizeableItems
{

    private static final String TAG = ResizeableItemsUiOps.class.getSimpleName();

    protected Activity activity;

    protected Unbinder unbinder;

    @Setter
    protected BaseInterfaces.DomainLink domainLink;

    @BindDimen(R.dimen.projects_rv_collapsed_width)
    int collapsedWidth;

    @Getter @Setter
    private RecyclerView rv;

    @Getter @Setter
    private int lastSelectedId = -1;

    @Getter @Setter
    private List<ResizeableViewHolder> viewHolderList = new ArrayList<>();
    // TODO: check if the list of view holders can be removed as a link between the adapter and the ui ops

    @Getter @Setter
    private int maxElevation = 0;

    @Override
    public void resetLastSelectedId() {
        lastSelectedId = -1;
    }

    @Override
    public boolean resizeItems(int selectedId) {
        boolean deselecting = false;
        for (int i = 0; i < viewHolderList.size(); i++) {
            ResizeableViewHolder vh = viewHolderList.get(i);
            int currentId = vh.getItemProperties().getItemContainerCustomId();
            boolean forceExpand;
            if (selectedId == lastSelectedId) {                 // expand
                deselecting = true;
                vh.getItemProperties().setSelected(false);
                forceExpand = true;
            } else {                                            // compact
                if (selectedId == currentId) {
                    vh.getItemProperties().setSelected(true);
                } else {
                    vh.getItemProperties().setSelected(false);
                }
                forceExpand = false;
            }
            vh.resizeItemView(forceExpand);
        }
        if (deselecting) {
            resetLastSelectedId();
        } else {
            lastSelectedId = selectedId;
        }
        Log.d(TAG, "resizeItems() returned: " + deselecting);
        return deselecting;
    }

    public ResizeableItemsUiOps(Activity activity) {
        this.activity = activity;
        unbinder = ButterKnife.bind(this, activity);
        ((BaseInterfaces.UnbinderHost) activity).registerUnbinder(unbinder);
    }

    /**
     * Resize the projects recycler view based on the enlarge param
     *
     * @param enlarge if true, makes the rv as big as its parent.
     *                also determines if the parent rv gets elevated over
     *                the issues rv (if the device supports the operation)
     */
    protected void resizeRv(boolean enlarge) {

        int width = getRv().getLayoutParams().width;
        int height = getRv().getLayoutParams().height;
        LinearLayout.LayoutParams lpProjectsRvParent = new LinearLayout.LayoutParams(
                enlarge ? ViewGroup.LayoutParams.MATCH_PARENT : collapsedWidth, height);

        getRv().setLayoutParams(lpProjectsRvParent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getRv().setElevation(enlarge ? 0 : getMaxElevation());
        }
    }

//    @CallSuper
    @Override
    public void unbind() {
        Log.e(TAG, "unbind: called in Abstract class.");
        unbinder.unbind();
        ((BaseInterfaces.UnbinderHost) activity).deRegisterUnbinder(this);
        domainLink = null;
    }

}
