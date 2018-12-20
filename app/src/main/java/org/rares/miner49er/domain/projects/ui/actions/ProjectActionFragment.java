package org.rares.miner49er.domain.projects.ui.actions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.GenericDAO;
import org.rares.miner49er.persistence.dao.GenericDaoFactory;
import org.rares.miner49er.ui.actionmode.ActionFragment;

import java.util.ArrayList;
import java.util.List;

public abstract class ProjectActionFragment extends ActionFragment {
    @BindView(R.id.content_container)       // <-- move these from here :|
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

    @Override
    public void prepareExit() {
        List<TextInputLayout> layouts = new ArrayList<>();
        layouts.add(inputLayoutProjectName);
        layouts.add(inputLayoutProjectShortName);
        layouts.add(inputLayoutProjectDescription);
        layouts.add(inputLayoutProjectIcon);
        layouts.add(inputLayoutProjectOwner);

        for (TextInputLayout til : layouts) {
            if (til.getEditText().getEditableText().length() == 0) {
                if (til.getError() != null && til.getError().equals(errRequired)) {
                    til.setError("");
                }
            }
        }

        actionFragmentTransition.prepareExitAnimation(getView(), replacedView);
        resultListener.onFragmentDismiss();
        getFragmentManager().popBackStack();  //
    }

    @Override
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