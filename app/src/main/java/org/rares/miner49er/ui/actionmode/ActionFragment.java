package org.rares.miner49er.ui.actionmode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.GenericDaoFactory;
import org.rares.miner49er.ui.actionmode.transitions.ActionFragmentTransition;
import org.rares.miner49er.ui.actionmode.transitions.TranslationTransition;

public abstract class ActionFragment extends Fragment {

    public abstract boolean validateForm();

    public abstract void applyAction();

    public abstract String getActionTag();

//    public abstract void populateFields(long entityId);

    @Setter
    protected View replacedView;

    @Setter
    protected ActionFragmentTransition actionFragmentTransition = new TranslationTransition();

    @Getter
    @Setter
    protected ActionEnforcer.FragmentResultListener resultListener;

    protected ScrollView rootView;
    protected Unbinder unbinder;

    @BindView(R.id.content_container)
    protected ConstraintLayout container;

    @BindView(R.id.project_name_input_layout)
    protected TextInputLayout inputLayoutProjectName;
    @BindView(R.id.project_name_input_layout_edit)
    protected TextInputEditText editTextProjectName;
    @BindView(R.id.project_short_name_input_layout)
    protected TextInputLayout inputLayoutProjectShortName;
    @BindView(R.id.project_short_name_input_layout_edit)
    protected TextInputEditText editTextProjectShortName;
    @BindView(R.id.project_description_input_layout)
    protected TextInputLayout inputLayoutProjectDescription;
    @BindView(R.id.project_description_input_layout_edit)
    protected TextInputEditText editTextProjectDescription;
    @BindView(R.id.project_icon_input_layout)
    protected TextInputLayout inputLayoutProjectIcon;
    @BindView(R.id.project_icon_input_layout_edit)
    protected TextInputEditText editTextProjectIcon;
    @BindView(R.id.project_owner_input_layout)
    protected TextInputLayout inputLayoutProjectOwner;
    @BindView(R.id.project_owner_input_layout_edit)
    protected TextInputEditText editTextProjectOwner;

    @BindString(R.string.error_field_required)
    protected String errRequired;
    @BindString(R.string.error_field_contains_illegal_characters)
    protected String errCharacters;
    @BindString(R.string.error_field_already_exists)
    protected String errExists;
    @BindString(R.string.success_project_add)
    protected String successfulAdd;
    @BindString(R.string.success_project_save)
    protected String successfulUpdate;

    @BindColor(R.color.indigo_100_blacked)
    protected int snackbarBackgroundColor;

    @BindColor(R.color.pureWhite)
    protected int snackbarTextColor;

    protected GenericDAO<ProjectData> projectsDAO;
    protected GenericDAO<UserData> usersDAO;

    protected ProjectData projectData = null;

    protected View createView(LayoutInflater inflater, ViewGroup container) {

        rootView = (ScrollView) inflater.inflate(R.layout.fragment_project_edit, container, false);
        setReplacedView(container.findViewById(R.id.scroll_views_container));        //

        unbinder = ButterKnife.bind(this, rootView);
        prepareEntry();
        rootView.setSmoothScrollingEnabled(true);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        projectsDAO = GenericDaoFactory.ofType(ProjectData.class);
        usersDAO = GenericDaoFactory.ofType(UserData.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        rootView.smoothScrollTo(0, 0);
    }

    public void prepareExit() {
        actionFragmentTransition.prepareExitAnimation(getView(), replacedView);
        resultListener.onFragmentDismiss();
        getFragmentManager().popBackStack();  //
    }

    public void prepareEntry() {
        actionFragmentTransition.prepareEntryAnimation(replacedView);
    }

    @OnClick(R.id.btn_cancel_add_project)
    public void cancelAction() {
        projectData = null;
        resetFields();
        prepareExit();
    }

    protected void resetFields() {
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
}
