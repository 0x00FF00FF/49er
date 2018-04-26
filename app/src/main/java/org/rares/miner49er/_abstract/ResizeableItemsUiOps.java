package org.rares.miner49er._abstract;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces;

/**
 * @author rares
 * @since 02.03.2018
 */

public abstract class ResizeableItemsUiOps
        implements
        BaseInterfaces.ListItemClickListener,
        BaseInterfaces.ResizeableItems
{

    private static final String TAG = ResizeableItemsUiOps.class.getSimpleName();

    @Setter
    protected BaseInterfaces.DomainLink domainLink;

    @Setter
    private int rvCollapsedWidth;

    @Getter @Setter
    private RecyclerView rv;

    @Override
    public void resetLastSelectedId() {
        ((AbstractAdapter)getRv().getAdapter()).setLastSelectedPosition(-1);
    }

    public boolean selectItem(int selectedPosition) {
        // check if selected position is valid
        AbstractAdapter _tempAdapter = ((AbstractAdapter)getRv().getAdapter());
        final int prevSelected = _tempAdapter.getLastSelectedPosition();

//        RecyclerView.ViewHolder viewHolder = getRv().findViewHolderForAdapterPosition(prevSelected);
//        if (viewHolder != null) {
//            viewHolder.setIsRecyclable(true);
//        }

        if (prevSelected == selectedPosition) {
            resetLastSelectedId();
//            getRv().requestLayout();//////////////////////////
            return true;
        }
        _tempAdapter.setLastSelectedPosition(selectedPosition);
//        _tempAdapter.notifyItemChanged(prevSelected);
//        _tempAdapter.notifyItemChanged(selectedPosition);
        return false;
    }

/*
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
*/

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
                enlarge ? ViewGroup.LayoutParams.MATCH_PARENT : rvCollapsedWidth, height);

        // only change layout params if needed.
//        if ((enlarge && width == rvCollapsedWidth) || (!enlarge && width == ViewGroup.LayoutParams.MATCH_PARENT)) {
            getRv().setLayoutParams(lpProjectsRvParent);
//        }
        // commented to always redraw because the cra**y invalidate() method doesn't redraw the selected view

        AbstractAdapter _tempAdapter = (AbstractAdapter) getRv().getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getRv().setElevation(enlarge ? 0 : _tempAdapter.getMaxElevation());
        }
    }

}
