package org.rares.miner49er.domain.issues.ui.viewholder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindString;
import butterknife.BindView;
import lombok.Getter;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ItemViewAnimator;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.ui.custom.rotationaware.NoWidthUpdateListener;
import org.rares.miner49er.util.NumberUtils;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;
import org.rares.ratv.rotationaware.RotationAwareTextView;
import org.rares.ratv.rotationaware.animation.AnimationDTO;
import org.rares.ratv.rotationaware.animation.DefaultRotationAnimatorHost;

/**
 * @author rares
 * @since 10.10.2017
 */

public class IssuesViewHolder extends ResizeableItemViewHolder implements ItemViewAnimator {
    private static final String TAG = IssuesViewHolder.class.getSimpleName();

    @BindView(R.id.resizeable_list_item_container)
    ConstraintLayout topContainer;

    @BindView(R.id.ratv_resource_name_item)
    @Getter
    RotationAwareTextView issueName;

    @BindString(R.string._issues_info_template)
    String infoTemplate;

    private int originalTextSize = 60;  ////////////

    private TextView infoLabel = null;

    private int infoLabelId = -1;

    private IssueData holderData = null;

    public IssuesViewHolder(View itemView) {
        super(itemView);
        setItemProperties(new IssuesViewProperties());

        // set a custom animator that doesn't animate width because the default one
        // animates from whatever value the view has to -1 or -2 if it is the case
        // and we don't want that (bad visuals).
        animationUpdateListener = new NoWidthUpdateListener(issueName);
        animatorHost = new DefaultRotationAnimatorHost(issueName.gatherAnimationData());

//        issueName.getTextPaint().setTypeface(typefaceLight);
        issueName.getTextPaint().setTypeface(customTypeface);
    }

    @Override
    public void bindData(Object o, boolean shortVersion, boolean selected) {
        holderData = (IssueData) o;
        shortTitle = !holderData.getName().contains(" ") ?
                TextUtils.extractVowels(holderData.getName()) :
                TextUtils.extractInitials(holderData.getName());


        longTitle = TextUtils.capitalize(holderData.getName());

/*        Drawable d = itemView.getBackground();
        if (d instanceof LayerDrawable) {
            d.mutate();
            LayerDrawable ld = (LayerDrawable) d;
            GradientDrawable opaqueBackground = (GradientDrawable) ld.findDrawableByLayerId(R.id.opaque_background);
            if (opaqueBackground != null) {
                opaqueBackground.setColor(holderData.getColor());
            }
        }*/
        getItemProperties().setItemBgColor(holderData.getColor());
        getItemProperties().setId(holderData.getId());

//        prepareIssueInfo();
//        if (shortVersion && infoLabel != null && infoLabel.getVisibility() == View.VISIBLE) {
//            if(holderData.getName().equals("20"))
//            Log.i(TAG, "bindData: setting visibility false, alpha 0");
//            toggleInfoContainerVisiblity(false);
//            infoLabel.setAlpha(0);
//        }

        validateItem(shortVersion, selected);
    }

    @Override
    public void toggleItemText(boolean shortVersion) {
        issueName.setText(shortVersion ? shortTitle : longTitle);
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
    public ValueAnimator animateItem(boolean reverse, boolean selected, int animationTime) {
        // reverse + selected = this item was selected after another one was already selected
        // !reverse + selected = this item is the first to be selected
        // reverse + !selected = this item was not selected and returns to the expanded form
        // !reverse + !selected = this item is not selected and goes to short form

        AnimationTextValidator animationTextValidator = new AnimationTextValidator();
        animationTextValidator.reverse = reverse;
        animationTextValidator.selected = selected;

        animatorHost.clearListeners();

        AnimationDTO dto = preparePositionData(reverse, selected);

        animatorHost.updateAnimationData(dto);
        animatorHost.configureAnimator(reverse).addUpdateListener(animationUpdateListener);
        setAnimator(animatorHost.animator);

        validatePosition(!reverse, selected, dto);

        getAnimator().addListener(animationTextValidator);
        getAnimator().setDuration(animationTime);
//        getAnimator().start();
        return getAnimator();
    }


    /**
     * Method that ensures the correct rotation, size and views are shown
     * when an item is brought into the viewport.
     *
     * @param collapsed boolean that shows width status (true = collapsed, false = expanded)
     * @param selected  boolean that shows if the current view should be treated as selected or not.
     */
    @Override
    public void validateItem(boolean collapsed, boolean selected) {

        int fadeAnimationDelay = 250;
        boolean shouldValidate = true;
        if (getAnimator() != null && getAnimator().isRunning()) {
            shouldValidate = false; // do not validate anything if the animator is running
        }

        if (shouldValidate) {
            toggleItemText(collapsed);

            AnimationDTO animationDTO = preparePositionData(!collapsed, selected);
            issueName.setRotation(collapsed ? animationDTO.maxRotation : animationDTO.minRotation);
            issueName.setTextSize(collapsed ? animationDTO.maxTextSize : animationDTO.minTextSize);
            issueName.setTextColor(collapsed ? animationDTO.maxTextColor : animationDTO.minTextColor);
            issueName.setBackgroundColor(collapsed ? animationDTO.maxBackgroundColor : animationDTO.minBackgroundColor);

            validatePosition(collapsed, selected, animationDTO);
            issueName.setEllipsize(!collapsed);
        }

        prepareIssueInfo();

        if (infoLabel != null) {

            infoLabel.setText(infoLabelString);
            float currentAlpha = infoLabel.getAlpha();

            toggleInfoContainerVisiblity(!collapsed);

            if (!collapsed && (currentAlpha != 1 && (getAnimator() == null || !getAnimator().isRunning()))) {
                infoLabel.postDelayed(() -> startInfoContainerFade(currentAlpha), fadeAnimationDelay);
//                startInfoContainerFade(currentAlpha);
            }
        } else {
            itemView.postDelayed(() -> addInfoLabelToContainer(itemView.getContext().getResources(), collapsed), fadeAnimationDelay);
//            addInfoLabelToContainer(itemView.getContext().getResources(), collapsed);
        }
    }

    /**
     * Method that prepares most of the information needed
     * by an animation or validation operation. These are
     * start and end values but no 'in between' information.
     * Any or all of these values can be disregarded in the
     * update listener (which is the case for width).
     *
     * @param reverse  false when the values should be configured
     *                 for going back to the expanded form; this
     *                 is needed because we can not just use
     *                 animation.reverse() on the selected item
     *                 because that state holds different values
     *                 than its normal siblings
     * @param selected true for indicating that other values should
     *                 be applied to this item
     * @return an {@link AnimationDTO} that holds these values
     */
    private AnimationDTO preparePositionData(boolean reverse, boolean selected) {
//        Log.d(TAG, "preparePositionData() called with: reverse = [" + reverse + "], selected = [" + selected + "]");

        final TextPaint _tempTextPaint = issueName.getTextPaint();
        _tempTextPaint.setTextSize(issueName.getOriginalTextSize());

        AnimationDTO adto = new AnimationDTO();
        final int startRotation = issueName.getOriginalRotation();
        final int endRotation = issueName.getTargetRotation();
//        final int startWidth = issueName.getOriginalWidth();
//        final int endWidth = 48;
        final int startHeight = (int) (originalTextSize * 1.25);
        final int endHeight = itemView.getHeight();
        final int startTextSize = issueName.getOriginalTextSize();
        final int endTextSize = issueName.getOriginalTextSize();
        final int startTextColor = issueName.getOriginalTextColor();
        final int endTextColor = issueName.getTargetTextColor();
        final int startBackgroundColor = 0; // fully transparent black
        final int endBackgroundColor = issueName.getTargetBackgroundColor();
        final int selectedTextSize = (int) (endTextSize * 1.15F);
//        final int selectedWidth = endWidth * 2;

        final int minMarginLeft = issueName.getOriginalMarginLeft();
        final int minMarginTop = issueName.getOriginalMarginTop();
        final int minMarginRight = issueName.getOriginalMarginRight();
        final int minMarginBottom = issueName.getOriginalMarginBottom();

        final int maxMarginLeft = issueName.getTargetMarginLeft();
        final int maxMarginTop = issueName.getTargetMarginTop();
        final int maxMarginRight = issueName.getTargetMarginRight();
        final int maxMarginBottom = issueName.getTargetMarginBottom();

        final int selectedLeftMargin = (int) UiUtil.pxFromDp(itemView.getContext(), 2);

        adto.minRotation = startRotation;
        adto.maxRotation = endRotation;
//        adto.minWidth = startWidth;
//        adto.maxWidth = endWidth;
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

        ViewGroup.LayoutParams lp = issueName.getLayoutParams();
        ViewGroup.MarginLayoutParams mlp = null;
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            mlp = (ViewGroup.MarginLayoutParams) lp;
        }

        if (reverse) {
            if (selected) { // selected item goes back to expanded
                adto.minRotation = startRotation;
                adto.maxRotation = endRotation;
                adto.minTextSize = issueName.getOriginalTextSize();
                adto.maxTextSize = selectedTextSize;
                adto.minTextColor = issueName.getOriginalTextColor();
            } else {
                // go back to expanded form
                adto.maxRotation = (int) issueName.getRotation();
                adto.maxWidth = issueName.getWidth();
                adto.maxHeight = issueName.getHeight();
                adto.maxTextSize = issueName.getTextSize();
            }
        } else {
            if (selected) {
                // animate to the selected state (larger text size + larger text view)
                adto.minWidth = issueName.getWidth();
                adto.minHeight = issueName.getHeight();
                adto.minTextSize = issueName.getTextSize();
                adto.minRotation = (int) issueName.getRotation();
//                adto.maxWidth = selectedWidth;
                adto.maxTextSize = selectedTextSize;
                adto.minBackgroundColor = issueName.getBackgroundColor();
                adto.minTextColor = issueName.getTextPaint().getColor();
                adto.maxTextColor = 0xFFFFFFFF;
//                adto.maxBackgroundColor = 0x50FFFFFF;

                if (mlp != null) {
                    adto.minMarginLeft = mlp.leftMargin;
                    adto.minMarginTop = mlp.topMargin;
                    adto.minMarginRight = mlp.rightMargin;
                    adto.minMarginBottom = mlp.bottomMargin;
                }
                adto.maxMarginLeft = selectedLeftMargin;

            } else {
                // get current values and animate to collapsed form
                adto.minRotation = (int) issueName.getRotation();
                adto.minTextSize = issueName.getTextSize();
                adto.minWidth = issueName.getWidth();
                adto.minHeight = issueName.getHeight();
                adto.minBackgroundColor = issueName.getBackgroundColor();
                adto.minTextColor = issueName.getTextPaint().getColor();

                if (mlp != null) {
                    adto.minMarginLeft = mlp.leftMargin;
                    adto.minMarginTop = mlp.topMargin;
                    adto.minMarginRight = mlp.rightMargin;
                    adto.minMarginBottom = mlp.bottomMargin;
                }
            }
        }
//        Log.v(TAG, "preparePositionData: " + adto);
        return adto;
    }

    /**
     * Visual effects for the info container
     *
     * @param currentAlpha the alpha to start the animation with
     */
    private void startInfoContainerFade(float currentAlpha) {

        PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat("alpha", currentAlpha, 1);

        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(pvh);

        setAnimator(animator);

        getAnimator().addUpdateListener(new IssueAnimationUpdateListener());
        getAnimator().setDuration(400);
        getAnimator().start();
    }

    /**
     * Validate size position, alignment, gravity for issueName
     */
    private void validatePosition(boolean collapsed, boolean selected, AnimationDTO animationDTO) {
        issueName.setGravity(collapsed ? RotationAwareTextView.GRAVITY_CENTER : RotationAwareTextView.GRAVITY_START);

        ViewGroup.LayoutParams issueNameLp = issueName.getLayoutParams();

        issueNameLp.width = 0;
        issueNameLp.height = collapsed ? ViewGroup.LayoutParams.MATCH_PARENT : (int) (originalTextSize * 1.25);

        ViewGroup.MarginLayoutParams mlp = null;
        if (issueNameLp instanceof ViewGroup.MarginLayoutParams) {
            mlp = (ViewGroup.MarginLayoutParams) issueNameLp;
            mlp.leftMargin = collapsed ? animationDTO.maxMarginLeft : animationDTO.minMarginLeft;
            mlp.topMargin = collapsed ? animationDTO.maxMarginTop : animationDTO.minMarginTop;
            mlp.rightMargin = collapsed ? animationDTO.maxMarginRight : animationDTO.minMarginRight;
            mlp.bottomMargin = collapsed ? animationDTO.maxMarginBottom : animationDTO.minMarginBottom;
        }

        ConstraintLayout.LayoutParams issueNameClp = null;
        if (issueNameLp instanceof ConstraintLayout.LayoutParams) {
            issueNameClp = (ConstraintLayout.LayoutParams) issueNameLp;

            int leftMargin = collapsed ? animationDTO.maxMarginLeft : animationDTO.minMarginLeft;
            int topMargin = collapsed ? animationDTO.maxMarginTop : animationDTO.minMarginTop;
            int rightMargin = collapsed ? animationDTO.maxMarginRight : animationDTO.minMarginRight;
            int bottomMargin = collapsed ? animationDTO.maxMarginBottom : animationDTO.minMarginBottom;

            issueNameClp.leftMargin = leftMargin;
            issueNameClp.topMargin = topMargin;
            issueNameClp.rightMargin = rightMargin;
            issueNameClp.bottomMargin = bottomMargin;
        }
    }

    /**
     * It's more effective to inflate a layout
     * that contains less items, and add them
     * later programmatically, after the view
     * is shown.
     *
     * @param res needed to get the customizable
     *            resources (size, colors etc.)
     */
    private void addInfoLabelToContainer(Resources res, boolean collapsed) {
        if (res == null || issueName == null || topContainer == null || infoLabelId != -1) {
//            Log.w(TAG, "addInfoLabelToContainer: RETURNING. Prerequisites not met.");
            return;
        }
        int textColor = 0xAA999999;
        textColor = UiUtil.getBrighterColor(textColor, 0.1F);

        infoLabel = new TextView(itemView.getContext());
        infoLabelId = NumberUtils.generateViewId();
        infoLabel.setId(infoLabelId);

        infoLabel.setTypeface(customTypeface);
        infoLabel.setTextColor(textColor);
        infoLabel.setText(infoLabelString);
        infoLabel.setAlpha(0);

        infoLabel.setHorizontallyScrolling(false);
        infoLabel.setLines(1);
        infoLabel.setSingleLine();
        infoLabel.setEllipsize(TruncateAt.END);

//        infoLabel.setTypeface(typefaceLight);

        int textSize = res.getDimensionPixelSize(R.dimen.list_item_secondary_text_size);
        infoLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.topToBottom = issueName.getId();
        lp.leftToLeft = issueName.getId();
        lp.startToStart = issueName.getId();
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        infoLabel.setLayoutParams(lp);

        if (issueName.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
            lp = (ConstraintLayout.LayoutParams) issueName.getLayoutParams();
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.bottomToTop = infoLabel.getId();

            lp.goneBottomMargin = 0;
            lp.bottomMargin = 0;
            issueName.setLayoutParams(lp);
        }

        toggleInfoContainerVisiblity(!collapsed);

        topContainer.addView(infoLabel);

        startInfoContainerFade(infoLabel.getAlpha());
    }

    /**
     * Listener that helps setting values at the
     * start/end of animations.
     */
    class AnimationTextValidator extends AnimatorListenerAdapter {

        boolean reverse = false;
        boolean selected = false;

        @Override
        public void onAnimationStart(Animator animation) {
            toggleItemText(!reverse);
            if (issueName != null) {
                issueName.setEllipsize(reverse);
            }
            toggleInfoContainerVisiblity(reverse);
//            issueName.getTextPaint().setTypeface(selected ? reverse ? typefaceLight : typefaceRegular : typefaceLight);
        }
    }

    /**
     * Listener that acts on the info container alpha value.
     */
    class IssueAnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float alpha = (float) animation.getAnimatedValue("alpha");
            setInfoContainerAlpha(alpha);
        }
    }

    private void setInfoContainerAlpha(float alpha) {
        if (infoLabel != null) {
            infoLabel.setAlpha(alpha);
        }
    }

    private void toggleInfoContainerVisiblity(boolean visible) {
        if (infoLabel != null) {
            infoLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void prepareIssueInfo() {

        infoLabelString = UiUtil.populateInfoString(infoTemplate, holderData);

        if (infoLabel != null) {
            infoLabel.setText(infoLabelString);
        }

//        Log.d(TAG, "prepareIssueInfo: " + issueName.getText() + "|" + infoLabelString);
    }

    @Override
    public void unbind() {
        if (getAnimator() != null) {
            getAnimator().end();
        }

        if (issueName != null && issueName.getHandler() != null) {
            issueName.getHandler().removeCallbacksAndMessages(null);
        }
        issueName = null;
        infoLabel = null;
        infoLabelString = null;

        super.unbind();
    }
}