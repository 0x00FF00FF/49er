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
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.util.UiUtil;

public class ProjectEditFormFragment extends ActionFragment {

    public static final String TAG = ProjectEditFormFragment.class.getSimpleName();

    private ScrollView rootView;
    private Unbinder unbinder;

    @BindView(R.id.content_container)
    ConstraintLayout container;

    @BindView(R.id.project_name_input_layout)
    TextInputLayout inputLayoutProjectName;
    @BindView(R.id.project_name_input_layout_edit)
    TextInputEditText editTextProjectName;
    @BindView(R.id.project_short_name_input_layout)
    TextInputLayout inputLayoutProjectShortName;
    @BindView(R.id.project_short_name_input_layout_edit)
    TextInputEditText editTextProjectShortName;
    @BindView(R.id.project_description_input_layout)
    TextInputLayout inputLayoutProjectDescription;
    @BindView(R.id.project_description_input_layout_edit)
    TextInputEditText editTextProjectDescription;
    @BindView(R.id.project_icon_input_layout)
    TextInputLayout inputLayoutProjectIcon;
    @BindView(R.id.project_icon_input_layout_edit)
    TextInputEditText editTextProjectIcon;
    @BindView(R.id.project_owner_input_layout)
    TextInputLayout inputLayoutProjectOwner;
    @BindView(R.id.project_owner_input_layout_edit)
    TextInputEditText editTextProjectOwner;

    @BindString(R.string.error_field_required)
    String errRequired;
    @BindString(R.string.error_field_contains_illegal_characters)
    String errCharacters;

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
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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

    @OnClick(R.id.btn_cancel_add_project)
    void cancelAction() {
        resetFields();
        prepareExit();
        getFragmentManager().popBackStack();
        dismissListener.onFragmentDismiss();
    }

    @OnClick(R.id.btn_add_project)
    void addAction() {
        if (validateForm()) {
            if (addProject()) {
                prepareExit();
                getFragmentManager().popBackStack();
                dismissListener.onFragmentDismiss();
            }
        }
    }

    @Override
    public boolean validateForm() {
        boolean validForm = true;
        int scrollToY = container.getHeight();
        int diff = (int) UiUtil.pxFromDp(getContext(), 15);

        if (!validateEmptyText(editTextProjectName, inputLayoutProjectName)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectName.getY() - diff, scrollToY);
        } else {
            if (!validateCharacters(editTextProjectName, inputLayoutProjectName)) {
                validForm = false;
                scrollToY = Math.min((int) inputLayoutProjectName.getY() - diff, scrollToY);
            }
        }

        if (!validateEmptyText(editTextProjectOwner, inputLayoutProjectOwner)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectOwner.getY() - diff, scrollToY);
        } else {
            if (!validateCharacters(editTextProjectOwner, inputLayoutProjectOwner)) {
                validForm = false;
                scrollToY = Math.min((int) inputLayoutProjectOwner.getY() - diff, scrollToY);
            }
        }

        if (!validateCharacters(editTextProjectShortName, inputLayoutProjectShortName)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectShortName.getY() - diff, scrollToY);
        }

        if (!validateCharacters(editTextProjectDescription, inputLayoutProjectDescription)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectDescription.getY() - diff, scrollToY);
        }

        if (!validateCharacters(editTextProjectIcon, inputLayoutProjectIcon)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectIcon.getY() - diff, scrollToY);
        }

        if (!validForm) {
            rootView.smoothScrollTo(0, scrollToY);
        }

        return validForm;
    }

    @Override
    public boolean applyAction() {
        return addProject();
    }

    public boolean addProject() {
//        repository.add()
        return true;
    }

    private void resetFields() {
        rootView.smoothScrollTo(0, 0);
        inputLayoutProjectName.setError("");
        editTextProjectName.setText("");
        inputLayoutProjectShortName.setError("");
        editTextProjectShortName.setText("");
        inputLayoutProjectDescription.setError("");
        editTextProjectDescription.setText("");
        inputLayoutProjectIcon.setError("");
        editTextProjectIcon.setText("");
        inputLayoutProjectOwner.setError("");
        editTextProjectOwner.setText("");
    }

    private boolean validateEmptyText(TextInputEditText editText, TextInputLayout layout) {
        if ("".equals(editText.getText().toString())) {
            layout.setError(errRequired);
            return false;
        } else {
            layout.setError("");
        }
        return true;
    }

    private boolean validateCharacters(TextInputEditText editText, TextInputLayout layout) {
        if (editText.getText().toString().contains("#")) {
            layout.setError(errCharacters);
            return false;
        } else {
            layout.setError("");
        }
        return true;
    }
}
