package org.rares.miner49er._abstract;

import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.view.View;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.ListItemEventListener;
import org.rares.miner49er.R;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.ratv.rotationaware.animation.RotationAnimatorHost;
import org.rares.ratv.rotationaware.animation.RotationAwareUpdateListener;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * @author rares
 * @since 02.03.2018
 */

public abstract class ResizeableItemViewHolder
        extends RecyclerView.ViewHolder
        implements
        Unbinder,
        View.OnClickListener {

    private static final String TAG = ResizeableItemViewHolder.class.getSimpleName();

    @Getter
    protected String shortTitle = "", longTitle = "";

    @Getter
    @Setter
    private ValueAnimator animator = null;

    @Setter
    private ListItemEventListener itemClickListener;

    @Getter
    @Setter
    ItemViewProperties itemProperties;

    @Getter
    @Setter
    private boolean toBeRebound = true;

    protected AbstractViewModel viewModel;

    private Unbinder unbinder;

//    protected Typeface typefaceLight = Typeface.create("sans-serif-light", Typeface.NORMAL);
//    protected Typeface typefaceRegular = Typeface.create("sans-serif", Typeface.NORMAL);
    protected Typeface customTypeface = ResourcesCompat.getFont(itemView.getContext(), R.font.futura_book_bt);

    protected RotationAwareUpdateListener animationUpdateListener;
    protected RotationAnimatorHost animatorHost;

    @Getter
    protected String infoLabelString;

    public abstract void bindData(Object data, boolean shortVersion, boolean selected);

    public ResizeableItemViewHolder(View itemView) {
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

    /**
     * The view holder is 'resizeable' (better said,
     * items in this view holder will be resized) so
     * at times we need to show a short version of
     * the text.
     *
     * @param shortVersion true to show the short version of the text
     */
    public void toggleItemText(boolean shortVersion) {
        // not forcing concrete classes to override this
    }

    /**
     * Convenience method that
     *
     * @return the long version of the title
     */
    public String getItemText() {
        return longTitle;
    }
}
