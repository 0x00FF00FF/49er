package org.rares.miner49er.domain.projects.ui.actions.edit;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.util.UiUtil;

public class ProjectEditFormFragment extends ActionFragment {

    public static final String TAG = ProjectEditFormFragment.class.getSimpleName();

    private ScrollView rootView;

    public ProjectEditFormFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ScrollView v = (ScrollView) inflater.inflate(R.layout.fragment_project_edit, container, false);
        replacedView = container.findViewById(R.id.scroll_views_container);
        prepareEntry();
        rootView = v;
        v.setSmoothScrollingEnabled(true);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        UiUtil.sendViewToBack(getView());
    }

    @Override
    public void onResume() {
        super.onResume();
        rootView.smoothScrollTo(0, 0);
    }

    @Override
    public void prepareEntry() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) replacedView.getLayoutParams();
        replacedView.animate().translationXBy(replacedView.getWidth() + lp.leftMargin + lp.rightMargin);
    }

    @Override
    public void prepareExit() {
        Animation exitAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.item_animation_to_left);
        exitAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // get fragment manager and handle back pressed
                AppCompatActivity context = (AppCompatActivity) getContext();
                if (context != null) {
                    context.getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        replacedView.animate().translationX(0).start();
        getView().startAnimation(exitAnimation);
    }

    @Override
    public boolean validateForm() {
        return false;
    }

    @Override
    public boolean applyAction() {
        return addProject();
    }

    public boolean addProject() {
//        repository.add()
        return true;
    }

}
