package org.rares.miner49er.domain.projects.viewholder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.domain.projects.adapter.ProjectViewProperties;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.util.TextUtils;
import org.rares.ratv.rotationaware.RotationAwareTextView;
import org.rares.ratv.rotationaware.animation.AnimationDTO;

public class RotatingViewHolder extends ResizeableItemViewHolder implements org.rares.miner49er._abstract.ItemViewAnimator {

    private static final String TAG = RotatingViewHolder.class.getSimpleName();

    @BindView(R.id.ratv_resource_name_item)
    public RotationAwareTextView projectNameTextView;

    @BindView(R.id.project_logo)
    View projectLogoView;

    @BindView(R.id.issues_number_info)
    LinearLayout issuesNumberInfo;

    @BindView(R.id.users_info)
    LinearLayout usersInfo;

    @BindView(R.id.hours_info)
    LinearLayout totalHoursInfo;

    @BindView(R.id.child_domain_info_container)
    LinearLayout childDomainInfoContainer;

    private int forcedWidth = Integer.MIN_VALUE;

//    @BindView(R.id.ratv_name_item_container)
//    LinearLayout nameContainer;

    private TextView issuesValue;
    private TextView usersValue;
    private TextView hoursValue;

    private int originalTextSize = 60;

    private ProjectViewProperties projectViewProperties = new ProjectViewProperties();

    @SuppressLint("SetTextI18n")
    public RotatingViewHolder(View itemView) {
        super(itemView);
        setItemProperties(projectViewProperties);
//        Miner49erApplication.getRefWatcher(itemView.getContext()).watch(this);
        TextView issuesLabel = issuesNumberInfo.findViewById(R.id.helper_label);
        TextView usersLabel = usersInfo.findViewById(R.id.helper_label);
        TextView hoursLabel = totalHoursInfo.findViewById(R.id.helper_label);

        issuesValue = issuesNumberInfo.findViewById(R.id.helper_value);
        usersValue = usersInfo.findViewById(R.id.helper_value);
        hoursValue = totalHoursInfo.findViewById(R.id.helper_value);

        issuesLabel.setText("Issues: ");
        usersLabel.setText("Users: ");
        hoursLabel.setText("Hours: ");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindData(Object o, boolean shortVersion, boolean selected) {
        ProjectData data = (ProjectData) o;
        int itemBgColor = data.getColor() == 0 ? Color.parseColor("cbbeb5") : data.getColor();
        projectViewProperties.setItemBgColor(itemBgColor);
        projectViewProperties.setId(data.getId());

        shortTitle = TextUtils.extractInitials(data.getName());
        longTitle = data.getName();

        toggleItemText(shortVersion);
        itemView.setBackgroundColor(itemBgColor);

        projectLogoView.setVisibility(View.VISIBLE);

        issuesValue.setText("16");
        usersValue.setText("4");
        hoursValue.setText("221");

        if (!shortVersion) {

            if (childDomainInfoContainer.getAlpha() == 0) {
                PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat("alpha", 0, 1);

                ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(pvh);

                setAnimator(animator);
                getAnimator().addUpdateListener(new ItemAnimationUpdateListener());
                getAnimator().setStartDelay(400);
                getAnimator().setDuration(500);
                getAnimator().start();
            }
        } else {
//            childDomainInfoContainer.setVisibility(View.VISIBLE);
            childDomainInfoContainer.setAlpha(1);
        }

        projectNameTextView.setGravity(shortVersion ? RotationAwareTextView.GRAVITY_CENTER : RotationAwareTextView.GRAVITY_START);
    }

    /**
     * Animates an item to a previously defined schema. No state is held in the holder. <br />
     * Check {@link RotationAwareTextView} innards for more information about the animation.
     *
     * @param reverse       specifies if the animation should run backwards
     * @param selected      specifies that the current item is selected
     * @param animationTime defines animation time
     */
    @Override
    public void animateItem(boolean reverse, boolean selected, int animationTime) {
        // reverse + selected = this item was selected after another one was already selected
        // !reverse + selected = this item is the first to be selected
        // reverse + !selected = this item was not selected and returns to the expanded form
        // !reverse + !selected = this item is not selected and goes to short form
        if (!projectNameTextView.isDefaultAnimatorEnabled()) {
            projectNameTextView.setEnableDefaultAnimator(true);
        }

        AnimationTextValidator animationTextValidator = new AnimationTextValidator();
        animationTextValidator.reverse = reverse;

        if (getAnimator() != null) {
            getAnimator().removeAllListeners();
        }

        projectNameTextView
                .getRotationAnimatorHost()
                .updateAnimationData(
                        preparePositionData(reverse, selected));
        setAnimator(
                projectNameTextView
                        .getRotationAnimatorHost()
                        .configureAnimator(reverse));
        getAnimator().addListener(animationTextValidator);
        getAnimator().setDuration(animationTime);
        getAnimator().start();
    }

    /**
     * Method that ensures the correct rotation, size and views are shown
     * when an item is brought in.
     *
     * @param collapsed boolean that shows width status (true = collapsed, false = expanded)
     * @param selected  boolean that shows if the current view should be treated as selected or not.
     */
    @Override
    public void validateItem(boolean collapsed, boolean selected) {
        if (getAnimator() != null && getAnimator().isRunning()) {
            return; // do not validate anything if the animator is running
        }

        projectNameTextView.setGravity(collapsed ? RotationAwareTextView.GRAVITY_CENTER : RotationAwareTextView.GRAVITY_START);
        toggleItemText(collapsed);

        AnimationDTO animationDTO = preparePositionData(!collapsed, selected);
        projectNameTextView.setRotation(collapsed ? animationDTO.maxRotation : animationDTO.minRotation);
        projectNameTextView.setTextSize(collapsed ? animationDTO.maxTextSize : animationDTO.minTextSize);
        projectNameTextView.setTextColor(collapsed ? animationDTO.maxTextColor : animationDTO.minTextColor);
        projectNameTextView.setBackgroundColor(collapsed ? animationDTO.maxBackgroundColor : animationDTO.minBackgroundColor);

        ViewGroup.LayoutParams lp = projectNameTextView.getLayoutParams();
//        lp.width = collapsed ? selected ? 48 * 2 : 48 : (int) projectNameTextView.getTextPaint().measureText(projectNameTextView.getText());
        lp.height = collapsed ? itemView.getLayoutParams().height : (int) (originalTextSize * 1.25);
        ViewGroup.MarginLayoutParams mlp = null;
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.leftMargin = collapsed ? animationDTO.maxMarginLeft : animationDTO.minMarginLeft;
            mlp.topMargin = collapsed ? animationDTO.maxMarginTop : animationDTO.minMarginTop;
            mlp.rightMargin = collapsed ? animationDTO.maxMarginRight : animationDTO.minMarginRight;
            mlp.bottomMargin = collapsed ? animationDTO.maxMarginBottom : animationDTO.minMarginBottom;
        }
        if (mlp != null) {
            projectNameTextView.setLayoutParams(mlp);
        }


        projectNameTextView.setLayoutParams(lp);

        childDomainInfoContainer.setVisibility(collapsed ? View.GONE : View.VISIBLE);
    }

    private AnimationDTO preparePositionData(boolean reverse, boolean selected) {
        final TextPaint _tempTextPaint = projectNameTextView.getTextPaint();
        _tempTextPaint.setTextSize(projectNameTextView.getOriginalTextSize());

        AnimationDTO adto = new AnimationDTO();
        final int startRotation = projectNameTextView.getOriginalRotation();
        final int endRotation = projectNameTextView.getTargetRotation();
        final int startWidth = (int) _tempTextPaint.measureText(longTitle);
        final int endWidth = 48;
        final int startHeight = (int) (originalTextSize * 1.25);
        final int endHeight = itemView.getHeight();
        final int startTextSize = projectNameTextView.getOriginalTextSize();
        final int endTextSize = projectNameTextView.getTargetTextSize();
        final int startTextColor = projectNameTextView.getOriginalTextColor();
        final int endTextColor = projectNameTextView.getTargetTextColor();
        final int startBackgroundColor = 0; // fully transparent black
        final int endBackgroundColor = projectNameTextView.getTargetBackgroundColor();
        final int selectedTextSize = (int) (endTextSize * 1.5F);
        final int selectedWidth = endWidth * 2;

        final int minMarginLeft = projectNameTextView.getOriginalMarginLeft();
        final int minMarginTop = projectNameTextView.getOriginalMarginTop();
        final int minMarginRight = projectNameTextView.getOriginalMarginRight();
        final int minMarginBottom = projectNameTextView.getOriginalMarginBottom();

        final int maxMarginLeft = projectNameTextView.getTargetMarginLeft();
        final int maxMarginTop = projectNameTextView.getTargetMarginTop();
        final int maxMarginRight = projectNameTextView.getTargetMarginRight();
        final int maxMarginBottom = projectNameTextView.getTargetMarginBottom();

        adto.minRotation = startRotation;
        adto.maxRotation = endRotation;
        adto.minWidth = startWidth;
        adto.maxWidth = endWidth;
        adto.minHeight = startHeight;
        adto.maxHeight = endHeight;
        adto.minTextSize = startTextSize;
        adto.maxTextSize = endTextSize;
        adto.minTextColor = startTextColor;
        adto.maxTextColor = endTextColor;
        adto.minBackgroundColor = startBackgroundColor;
        adto.maxBackgroundColor = endBackgroundColor;
        adto.minMarginLeft = minMarginLeft;
        adto.minMarginTop = minMarginTop;
        adto.minMarginRight = minMarginRight;
        adto.minMarginBottom = minMarginBottom;
        adto.maxMarginLeft = maxMarginLeft;
        adto.maxMarginTop = maxMarginTop;
        adto.maxMarginRight = maxMarginRight;
        adto.maxMarginBottom = maxMarginBottom;
        adto.updateListener = projectNameTextView.getAnimationUpdateListener();

        ViewGroup.LayoutParams lp = projectNameTextView.getLayoutParams();
        ViewGroup.MarginLayoutParams mlp = null;
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            mlp = (ViewGroup.MarginLayoutParams) lp;
        }

        if (reverse) {
            if (selected) { // selected item goes back to expanded
                adto.minRotation = startRotation;
                adto.maxRotation = endRotation;
                adto.minTextSize = projectNameTextView.getOriginalTextSize();
                adto.maxTextSize = selectedTextSize;
                adto.minTextColor = projectNameTextView.getOriginalTextColor();
            } else {
                // go back to expanded form
                adto.maxRotation = (int) projectNameTextView.getRotation();
                adto.maxWidth = projectNameTextView.getWidth();
                adto.maxHeight = projectNameTextView.getHeight();
                adto.maxTextSize = projectNameTextView.getTextSize();
            }
        } else {
            if (selected) {
                // animate to the selected state (larger text size + larger text view)
                adto.minWidth = projectNameTextView.getWidth();
                adto.minHeight = projectNameTextView.getHeight();
                adto.minTextSize = projectNameTextView.getTextSize();
                adto.minRotation = (int) projectNameTextView.getRotation();
                adto.maxWidth = selectedWidth;
                adto.maxTextSize = selectedTextSize;
                adto.minBackgroundColor = projectNameTextView.getBackgroundColor();
                adto.minTextColor = projectNameTextView.getTextPaint().getColor();
                adto.maxTextColor = 0xFFFFFFFF;
                adto.maxBackgroundColor = 0x77FFFFFF;

                if (mlp != null) {
                    adto.minMarginLeft = mlp.leftMargin;
                    adto.minMarginTop = mlp.topMargin;
                    adto.minMarginRight = mlp.rightMargin;
                    adto.minMarginBottom = mlp.bottomMargin;
                }

            } else {
                // get current values and animate to collapsed form
                adto.minRotation = (int) projectNameTextView.getRotation();
                adto.minTextSize = projectNameTextView.getTextSize();
                adto.minWidth = projectNameTextView.getWidth();
                adto.minHeight = projectNameTextView.getHeight();
                adto.minBackgroundColor = projectNameTextView.getBackgroundColor();
                adto.minTextColor = projectNameTextView.getTextPaint().getColor();

                if (mlp != null) {
                    adto.minMarginLeft = mlp.leftMargin;
                    adto.minMarginTop = mlp.topMargin;
                    adto.minMarginRight = mlp.rightMargin;
                    adto.minMarginBottom = mlp.bottomMargin;
                }

            }
        }
        return adto;
    }

    public void toggleItemText(boolean shortVersion) {
        projectNameTextView.setText(shortVersion ? shortTitle : longTitle);
    }

    class AnimationTextValidator extends AnimatorListenerAdapter {
        boolean reverse = false;

        @Override
        public void onAnimationStart(Animator animation) {
            if (reverse) {
                toggleItemText(false);
                childDomainInfoContainer.setVisibility(reverse ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!reverse) {
                toggleItemText(true);
            }
            projectNameTextView.setGravity(!reverse ? RotationAwareTextView.GRAVITY_CENTER : RotationAwareTextView.GRAVITY_START);
        }
    }

    class ItemAnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float alpha = (float) animation.getAnimatedValue("alpha");
            childDomainInfoContainer.setAlpha(alpha);
        }
    }
}
