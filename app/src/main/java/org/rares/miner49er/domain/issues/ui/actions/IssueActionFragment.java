package org.rares.miner49er.domain.issues.ui.actions;

import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import io.reactivex.disposables.CompositeDisposable;
import org.joda.time.DateTime;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionFragment;

public abstract class IssueActionFragment extends ActionFragment {

    private static final String TAG = IssueActionFragment.class.getSimpleName();

    protected AsyncGenericDao<ProjectData> projectsDAO;
    protected AsyncGenericDao<IssueData> issuesDAO;
    protected AsyncGenericDao<UserData> usersDAO;

    protected IssueData issueData = null;
    protected ProjectData projectData = null;
    protected UserData userData = null;

    @BindView(R.id.content_container)
    protected ConstraintLayout contentContainer;
    @BindView(R.id.project_name_input_layout)
    protected TextInputLayout projectNameInputLayout;
    @BindView(R.id.project_name_input_layout_edit)
    protected TextInputEditText projectNameEditText;
    @BindView(R.id.issue_name_input_layout)
    protected TextInputLayout issueNameInputLayout;
    @BindView(R.id.issue_name_input_layout_edit)
    protected TextInputEditText issueNameEditText;
    @BindView(R.id.issue_owner_input_layout)
    protected TextInputLayout issueOwnerInputLayout;
    @BindView(R.id.issue_owner_input_layout_edit)
    protected TextInputEditText issueOwnerEditText;
    @BindView(R.id.issue_date_added_input_layout)
    protected TextInputLayout dateAddedInputLayout;
    @BindView(R.id.issue_date_added_input_layout_edit)
    protected TextInputEditText dateAddedEditText;

    @BindView(R.id.btn_cancel_add_issue)
    protected MaterialButton cancelButton;
    @BindView(R.id.btn_add_issue)
    protected MaterialButton applyButton;

    protected Unbinder unbinder;
    protected CompositeDisposable disposable = new CompositeDisposable();

    @BindString(R.string.success_issue_add)
    protected String successfulAdd;
    @BindString(R.string.success_issue_save)
    protected String successfulSave;

    protected View createView(LayoutInflater inflater, ViewGroup container) {
        rootView = (ScrollView) inflater.inflate(R.layout.fragment_issue_edit, container, false);
        setReplacedView(container.findViewById(R.id.scroll_views_container));        //

        unbinder = ButterKnife.bind(this, rootView);
        prepareEntry();
        rootView.setSmoothScrollingEnabled(true);

        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        };

        InputFilter[] filters = new InputFilter[]{filter};

        projectNameEditText.setFilters(filters);
        issueNameEditText.setFilters(filters);
        issueOwnerEditText.setFilters(filters);
        dateAddedEditText.setFilters(filters);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        projectsDAO = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        issuesDAO = InMemoryCacheAdapterFactory.ofType(IssueData.class);
        usersDAO = InMemoryCacheAdapterFactory.ofType(UserData.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        unbinder.unbind();
    }

    @Override
    public void prepareEntry() {
        actionFragmentTransition.prepareEntryAnimation(replacedView);
    }

    @Override
    public void prepareExit() {
        resetFields();
        if (getView() != null) {
            actionFragmentTransition.prepareExitAnimation(getView(), replacedView);
        }
        resultListener.onFragmentDismiss();
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();  //
        }
    }

    @OnClick(R.id.btn_cancel_add_issue)
    public void cancelAction() {
        projectData = null;
        issueData = null;
        userData = null;
        resetFields();
        prepareExit();
    }

    protected void updateIssueData() {
        issueData.setDateAdded(System.currentTimeMillis());
        issueData.setName(issueNameEditText.getEditableText().toString());
        issueData.setLastUpdated(System.currentTimeMillis());
        issueData.setParentId(projectData.id);
        issueData.setOwner(userData);
    }

    protected void resetFields() {
        rootView.scrollTo(0, 0);
        clearErrors();
        projectNameEditText.setText("");
        issueNameEditText.setText("");
        issueOwnerEditText.setText("");
        dateAddedEditText.setText("");
    }

    protected void clearErrors() {
        projectNameInputLayout.setError("");
        issueNameInputLayout.setError("");
        issueOwnerInputLayout.setError("");
        dateAddedInputLayout.setError("");
    }

    protected void populateFields(long projectId) {
        disposable.add(projectsDAO.get(projectId, true).subscribe(
                pdOpt -> {
                    projectData = pdOpt.get();
                    projectNameEditText.setText(projectData.getName());
                    userData = pdOpt.get().getTeam().get(0);                ////
                    issueOwnerEditText.setText(userData.getName());
                }
        ));
        dateAddedEditText.setText(DateTime.now().toString("EE, d MMMM, y"));
    }
}
