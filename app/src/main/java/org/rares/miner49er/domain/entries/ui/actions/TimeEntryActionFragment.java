package org.rares.miner49er.domain.entries.ui.actions;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionFragment;


public abstract class TimeEntryActionFragment extends ActionFragment {

    @BindView(R.id.content_container)
    protected ConstraintLayout container;


    @BindView(R.id.project_name_input_layout)
    protected TextInputLayout projectNameInputLayout;
    @BindView(R.id.issue_name_input_layout)
    protected TextInputLayout issueNameInputLayout;
    @BindView(R.id.owner_input_layout)
    protected TextInputLayout ownerInputLayout;
    @BindView(R.id.hours_input_layout)
    protected TextInputLayout hoursWorkedInputLayout;
    @BindView(R.id.work_date_input_layout)
    protected TextInputLayout workDateInputLayout;
    @BindView(R.id.comments_input_layout)
    protected TextInputLayout commentsInputLayout;
    @BindView(R.id.date_added_input_layout)
    protected TextInputLayout dateAddedInputLayout;

    @BindView(R.id.project_name_edit_text)
    protected TextInputEditText projectNameEditText;
    @BindView(R.id.issue_name_edit_text)
    protected TextInputEditText issueNameEditText;
    @BindView(R.id.owner_edit_text)
    protected TextInputEditText ownerEditText;
    @BindView(R.id.hours_edit_text)
    protected TextInputEditText hoursWorkedEditText;
    @BindView(R.id.work_date_edit_text)
    protected TextInputEditText workDateEditText;
    @BindView(R.id.date_added_edit_text)
    protected TextInputEditText dateAddedEditText;
    @BindView(R.id.comments_edit_text)
    protected TextInputEditText commentsEditText;

    @BindView(R.id.btn_cancel)
    protected MaterialButton btnCancel;
    @BindView(R.id.btn_add)
    protected MaterialButton btnApply;

    @BindString(R.string.success_time_entry_add)
    protected String successfulAdd;
    @BindString(R.string.success_time_entry_save)
    protected String successfulSave;
    @BindString(R.string.error_time_entry_too_many_hours)
    protected String errTimeEntryTooManyHours;
    @BindString(R.string.error_time_entry_hours)
    protected String errTimeEntryIncorrectHours;

    protected AsyncGenericDao<ProjectData> projectsDAO;
    protected AsyncGenericDao<IssueData> issuesDAO;
    protected AsyncGenericDao<TimeEntryData> timeEntriesDAO;
    protected AsyncGenericDao<UserData> usersDAO;

    protected TimeEntryData timeEntryData = null;
    protected IssueData issueData = null;
    protected UserData userData = null;
    protected ProjectData projectData = null;

    protected final int maxHours = 16;
    protected final int minHours = 1;

    protected View createView(LayoutInflater inflater, ViewGroup container) {

        rootView = (ScrollView) inflater.inflate(R.layout.fragment_time_entry_edit, container, false);
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

        InputFilter numberFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type != Character.DECIMAL_DIGIT_NUMBER) {
                    return "";
                }
            }
            return null;
        };

        InputFilter[] filters = new InputFilter[]{filter};
        InputFilter[] numberFilters = new InputFilter[]{numberFilter};

//        issueNameEditText.setFilters(filters);
//        ownerEditText.setFilters(filters);
        workDateEditText.setFilters(filters);
        hoursWorkedEditText.setFilters(numberFilters);
        commentsEditText.setFilters(filters);
//        dateAddedEditText.setFilters(filters);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        projectsDAO = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        issuesDAO = InMemoryCacheAdapterFactory.ofType(IssueData.class);
        timeEntriesDAO = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);
        usersDAO = InMemoryCacheAdapterFactory.ofType(UserData.class);
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
        resetFields();

        actionFragmentTransition.prepareExitAnimation(getView(), replacedView);
        resultListener.onFragmentDismiss();
        getFragmentManager().popBackStack();  //
    }

    @Override
    public void prepareEntry() {
        actionFragmentTransition.prepareEntryAnimation(replacedView);
    }

    @OnClick(R.id.btn_cancel)
    public void cancelAction() {
/*        issueData = null;
        timeEntryData = null;
        userData = null;*/
        resetFields();
        prepareExit();
    }


    protected void updateData() {
        timeEntryData.setParentId(issueData.getId());
        timeEntryData.setComments(commentsEditText.getEditableText().toString());
        timeEntryData.setDateAdded(System.currentTimeMillis());
        if (hoursWorkedEditText.getEditableText().toString().equals("")) {
            timeEntryData.setHours(0);
        } else {
            timeEntryData.setHours(Integer.parseInt(hoursWorkedEditText.getEditableText().toString()));
        }
        timeEntryData.setUserId(userData.id);
        timeEntryData.setUserName(userData.getName());
        timeEntryData.setUserPhoto(userData.getPicture());
//        DateTimeFormatter format = DateTimeFormat.forPattern("EE, d MMMM, y");
//        timeEntryData.setWorkDate(DateTime.parse( ,format));
        timeEntryData.setWorkDate(System.currentTimeMillis());
    }

    protected void resetFields() {
        rootView.smoothScrollTo(0, 0);
        clearErrors();
        issueNameEditText.setText("");
        ownerEditText.setText("");
        hoursWorkedEditText.setText("");
        workDateEditText.setText("");
        commentsEditText.setText("");
        dateAddedEditText.setText("");
    }

    protected void clearErrors() {
        hoursWorkedInputLayout.setError("");
        workDateInputLayout.setError("");
        commentsInputLayout.setError("");
    }
}
