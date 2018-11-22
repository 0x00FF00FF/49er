package org.rares.miner49er.domain.projects.viewholder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.constraint.ConstraintLayout;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindString;
import butterknife.BindView;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er._abstract.ItemViewAnimator;
import org.rares.miner49er._abstract.ResizeableItemViewHolder;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.adapter.ProjectViewProperties;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.ui.custom.glide.GlideApp;
import org.rares.miner49er.ui.custom.rotationaware.NoWidthUpdateListener;
import org.rares.miner49er.util.NumberUtils;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;
import org.rares.ratv.rotationaware.RotationAwareTextView;
import org.rares.ratv.rotationaware.animation.AnimationDTO;
import org.rares.ratv.rotationaware.animation.DefaultRotationAnimatorHost;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class ProjectsViewHolder extends ResizeableItemViewHolder implements ItemViewAnimator {

    private static final String TAG = ProjectsViewHolder.class.getSimpleName();

    @BindView(R.id.resizeable_list_item_container)
    ConstraintLayout topContainer;

    @BindView(R.id.ratv_resource_name_item)
    RotationAwareTextView projectNameTextView;

    @BindView(R.id.project_logo)
    ImageView projectLogoView;

    @BindView(R.id.project_image)
    ImageView projectImage;

    private TextView infoLabel = null;

    @BindString(R.string._projects_info_issues_label)
    String issuesLabel;
    @BindString(R.string._projects_info_users_label)
    String usersLabel;
    @BindString(R.string._projects_info_total_hours)
    String hoursLabel;

    private int originalTextSize = 60;      /////

    private ProjectViewProperties projectViewProperties = new ProjectViewProperties();

    private int infoLabelId = -1;

    @Setter
    private boolean __SETTING_SHOW_PROJECT_NAME_WHILE_COLLAPSED = false;
    @Setter
    private boolean __SETTING_SHOW_SELECTED_PROJECT_NAME_WHILE_COLLAPSED = false;

    public ProjectsViewHolder(View itemView) {
        super(itemView);
        setItemProperties(projectViewProperties);
        animationUpdateListener = new NoWidthUpdateListener(projectNameTextView);
        animatorHost = new DefaultRotationAnimatorHost(projectNameTextView.gatherAnimationData());
    }

    @Override
    public void bindData(Object o, boolean shortVersion, boolean selected) {
        ProjectData data = (ProjectData) o;
        int itemBgColor = data.getColor() == 0 ? Color.parseColor("cbbeb5") : data.getColor();
        projectViewProperties.setItemBgColor(itemBgColor);
        projectViewProperties.setId(data.getId());

        shortTitle = __SETTING_SHOW_PROJECT_NAME_WHILE_COLLAPSED ? TextUtils.extractVowels(data.getName()) : "";
        shortTitle = __SETTING_SHOW_SELECTED_PROJECT_NAME_WHILE_COLLAPSED && selected ? TextUtils.extractVowels(data.getName()) : shortTitle;

        longTitle = data.getName();

        Drawable d = itemView.getBackground();
        if (d instanceof LayerDrawable) {
            d.mutate();
            LayerDrawable ld = (LayerDrawable) d;
            GradientDrawable gd = (GradientDrawable) ld.findDrawableByLayerId(R.id.opaque_background);
            if (gd != null) {
                gd.setColor(data.getColor());
            }
        }

        projectLogoView.setVisibility(View.VISIBLE);

        String pictureUrl = data.getPicture();

        try {
            pictureUrl = URLDecoder.decode(data.getPicture(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        GlideApp
                .with(itemView)
                .load(pictureUrl)
                .placeholder(R.drawable.skull)
                .fallback(R.drawable.skull)
                .error(R.drawable.skull)
                .into(projectLogoView);

        GlideApp
                .with(itemView)
                .load(pictureUrl)
//                .onlyRetrieveFromCache(true)
                .placeholder(R.drawable.skull)
//                .transform(new PositionedCrop(0.5F, 0.5F))
                .into(projectImage);

        int issuesNumber = 0;
        int usersNumber = 0;
        int hoursNumber = 0;

        List<IssueData> issues = data.getIssues();
        // todo => get these from cache
        if (issues != null) {
            issuesNumber = issues.size();
            for (IssueData issueData : issues) {
                List<TimeEntryData> timeEntryData = issueData.getTimeEntries();
                if (timeEntryData != null) {
                    for (TimeEntryData ted : timeEntryData) {
                        hoursNumber += ted.getHours();
                    }
                }
            }
        }
        List<UserData> users = data.getTeam();
        if (users != null) {
            usersNumber = users.size();
        }

        populateInfoLabel(issuesNumber, usersNumber, hoursNumber);

        if (infoLabel != null && infoLabel.getVisibility() == View.VISIBLE) {
            toggleInfoContainerVisiblity(false);
            infoLabel.setAlpha(0);
        }

        validateItem(shortVersion, selected);
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

        validatePosition(!reverse, dto);

        getAnimator().addListener(animationTextValidator);
        getAnimator().setDuration(animationTime);
//        getAnimator().start();
        return getAnimator();
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
        int fadeAnimationDelay = 250;
        boolean shouldValidate = true;
        if (getAnimator() != null && getAnimator().isRunning()) {
            shouldValidate = false; // do not validate anything if the animator is running
        }

//        projectNameTextView.setGravity(collapsed ? RotationAwareTextView.GRAVITY_CENTER : RotationAwareTextView.GRAVITY_START);
        if (shouldValidate) {
            toggleItemText(collapsed);

            AnimationDTO animationDTO = preparePositionData(!collapsed, selected);
            projectNameTextView.setRotation(collapsed ? animationDTO.maxRotation : animationDTO.minRotation);
            projectNameTextView.setTextSize(collapsed ? animationDTO.maxTextSize : animationDTO.minTextSize);
            projectNameTextView.setTextColor(collapsed ? animationDTO.maxTextColor : animationDTO.minTextColor);
            projectNameTextView.setBackgroundColor(collapsed ? animationDTO.maxBackgroundColor : animationDTO.minBackgroundColor);

            validatePosition(collapsed, animationDTO);
            projectNameTextView.setEllipsize(!collapsed);
        }

        projectLogoView.setAlpha(!collapsed ? .8F : selected ? 1 : 0.3F);

        if (infoLabel != null) {
            float currentAlpha = infoLabel.getAlpha();

            toggleInfoContainerVisiblity(!collapsed);

            if (!collapsed && (currentAlpha != 1 && (getAnimator() == null || !getAnimator().isRunning()))) {
                infoLabel.postDelayed(() -> startInfoContainerFade(currentAlpha), fadeAnimationDelay);
            }
        } else {
            itemView.postDelayed(() -> addInfoLabelToContainer(itemView.getContext().getResources(), collapsed), fadeAnimationDelay);
        }

        if (projectImage != null) {
            projectImage.setVisibility(collapsed ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Validate size position, alignment, gravity for projectNameTextView
     */
    private void validatePosition(boolean collapsed, AnimationDTO animationDTO) {
        projectNameTextView.setGravity(collapsed ? RotationAwareTextView.GRAVITY_CENTER : RotationAwareTextView.GRAVITY_START);

        ViewGroup.LayoutParams lp = projectNameTextView.getLayoutParams();
        boolean matchParentWidth = lp.width == ViewGroup.LayoutParams.MATCH_PARENT;

        lp.width = matchParentWidth ? ViewGroup.LayoutParams.MATCH_PARENT : collapsed ? animationDTO.maxWidth : animationDTO.minWidth;
        lp.height = collapsed ? ViewGroup.LayoutParams.MATCH_PARENT : (int) (originalTextSize * 1.25);

        ViewGroup.MarginLayoutParams mlp = null;
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.leftMargin = collapsed ? animationDTO.maxMarginLeft : animationDTO.minMarginLeft;
            mlp.topMargin = collapsed ? animationDTO.maxMarginTop : animationDTO.minMarginTop;
            mlp.rightMargin = collapsed ? animationDTO.maxMarginRight : animationDTO.minMarginRight;
            mlp.bottomMargin = collapsed ? animationDTO.maxMarginBottom : animationDTO.minMarginBottom;
        }
        ConstraintLayout.LayoutParams clp = null;
        if (lp instanceof ConstraintLayout.LayoutParams) {
            clp = (ConstraintLayout.LayoutParams) lp;

            int leftMargin = collapsed ? animationDTO.maxMarginLeft : animationDTO.minMarginLeft;
            int topMargin = collapsed ? animationDTO.maxMarginTop : animationDTO.minMarginTop;
            int rightMargin = collapsed ? animationDTO.maxMarginRight : animationDTO.minMarginRight;
            int bottomMargin = collapsed ? animationDTO.maxMarginBottom : animationDTO.minMarginBottom;

            clp.leftMargin = leftMargin;
            clp.topMargin = topMargin;
            clp.rightMargin = rightMargin;
            clp.bottomMargin = bottomMargin;
        }
//        if (clp != null) {
//            projectNameTextView.setLayoutParams(clp);
//        } else {
//            if (mlp != null) {
//                projectNameTextView.setLayoutParams(mlp);
//            } else {
//                projectNameTextView.setLayoutParams(lp);
//            }
//        }
    }

    private AnimationDTO preparePositionData(boolean reverse, boolean selected) {
        final TextPaint _tempTextPaint = projectNameTextView.getTextPaint();
        _tempTextPaint.setTextSize(projectNameTextView.getOriginalTextSize());

        AnimationDTO adto = new AnimationDTO();
        final int startRotation = projectNameTextView.getOriginalRotation();
        final int endRotation = projectNameTextView.getTargetRotation();
        final int startWidth = projectNameTextView.getOriginalWidth();
        final int endWidth = 48;
        final int startHeight = (int) (originalTextSize * 1.25);
        final int endHeight = itemView.getHeight();
        final int startTextSize = projectNameTextView.getOriginalTextSize();
        final int endTextSize = projectNameTextView.getTargetTextSize();
        final int startTextColor = projectNameTextView.getOriginalTextColor();
        final int endTextColor = projectNameTextView.getTargetTextColor();
        final int startBackgroundColor = 0; // fully transparent black
        final int endBackgroundColor = projectNameTextView.getTargetBackgroundColor();
        final int selectedTextSize = startTextSize/*(int) (endTextSize * 1.5F)*/;
        final int selectedWidth = endWidth * 2;

        final int minMarginLeft = projectNameTextView.getOriginalMarginLeft();
        final int minMarginTop = projectNameTextView.getOriginalMarginTop();
        final int minMarginRight = projectNameTextView.getOriginalMarginRight();
        final int minMarginBottom = projectNameTextView.getOriginalMarginBottom();

        final int maxMarginLeft = projectNameTextView.getTargetMarginLeft();
        final int maxMarginTop = projectNameTextView.getTargetMarginTop();
        final int maxMarginRight = projectNameTextView.getTargetMarginRight();
        final int maxMarginBottom = projectNameTextView.getTargetMarginBottom();

        final int minShadowColor = projectNameTextView.getOriginalShadowColor();
        final int maxShadowColor = projectNameTextView.getTargetShadowColor();

        final int minShadowRadius = projectNameTextView.getOriginalShadowRadius();
        final int maxShadowRadius = projectNameTextView.getTargetShadowRadius();

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
        adto.minShadowColor = minShadowColor;
        adto.maxShadowColor = maxShadowColor;
        adto.minShadowRadius = minShadowRadius;
        adto.maxShadowRadius = maxShadowRadius;

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
//                adto.maxShadowRadius = 8 * 3;
//                adto.maxBackgroundColor = 0x77FFFFFF;

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
                adto.minShadowRadius = projectNameTextView.getShadowRadius();
                adto.minShadowColor = projectNameTextView.getShadowColor();

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

    private void populateInfoLabel(int issuesValue,
                                   float usersValue,
                                   float hoursValue) {
        infoLabelString = String.format(
                issuesLabel + "%2s " +
                        usersLabel + "%3.1s " +
                        hoursLabel + "%3.1s ",
                issuesValue,
                usersValue,
                hoursValue
        );
    }

    private void toggleInfoContainerVisiblity(boolean visible) {
        if (infoLabel != null) {
            infoLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
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

        getAnimator().addUpdateListener(new ItemAnimationUpdateListener());
        getAnimator().setDuration(400);
        getAnimator().start();
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
        if (res == null || projectNameTextView == null || topContainer == null || infoLabelId != -1) {
            Log.w(TAG, "addInfoLabelToContainer: RETURNING. Prerequisites not met.");
            return;
        }
        int textColor = 0xAA999999;
        textColor = UiUtil.getBrighterColor(textColor, 0.1F);

        infoLabel = new TextView(itemView.getContext());
        infoLabelId = NumberUtils.generateViewId();
        infoLabel.setId(infoLabelId);

        infoLabel.setTextColor(textColor);
        infoLabel.setText(infoLabelString);
        infoLabel.setAlpha(0);

        infoLabel.setHorizontallyScrolling(false);
        infoLabel.setLines(1);
        infoLabel.setSingleLine();
        infoLabel.setEllipsize(TruncateAt.END);

        int textSize = res.getDimensionPixelSize(R.dimen.list_item_secondary_text_size);
        infoLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        lp.topToBottom = R.id.ratv_resource_name_item;
        lp.startToStart = R.id.ratv_resource_name_item;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        infoLabel.setLayoutParams(lp);

        if (projectNameTextView.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
            lp = (ConstraintLayout.LayoutParams) projectNameTextView.getLayoutParams();
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.bottomToTop = infoLabel.getId();

            lp.goneBottomMargin = 0;
            lp.bottomMargin = 0;
            projectNameTextView.setLayoutParams(lp);
        }

        toggleInfoContainerVisiblity(!collapsed);

        topContainer.addView(infoLabel);

        projectLogoView.bringToFront();

        startInfoContainerFade(infoLabel.getAlpha());
    }


    class AnimationTextValidator extends AnimatorListenerAdapter {
        boolean reverse = false;
        boolean selected = false;

        @Override
        public void onAnimationStart(Animator animation) {
            if (reverse) {
                toggleItemText(false);
            }
            if (infoLabel != null) {
                infoLabel.setVisibility(reverse ? View.VISIBLE : View.GONE);
            }
            if (projectImage != null) {
                projectImage.setVisibility(reverse ? View.VISIBLE : View.GONE);
            }
            projectLogoView.setAlpha(reverse ? .8F : selected ? 1 : 0.3F);
            projectNameTextView.setEllipsize(reverse);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!reverse) {
                toggleItemText(true);
            }
//            projectNameTextView.setGravity(!reverse ? RotationAwareTextView.GRAVITY_CENTER : RotationAwareTextView.GRAVITY_START);
        }
    }

    class ItemAnimationUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float alpha = (float) animation.getAnimatedValue("alpha");
            if (infoLabel != null) {
                infoLabel.setAlpha(alpha);
            }
        }
    }
}
