package org.rares.miner49er.domain.entries.ui.actions.add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.OnClick;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.pushtorefresh.storio3.Optional;
import org.joda.time.DateTime;
import org.rares.miner49er.BaseInterfaces;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.ViewModelCache;
import org.rares.miner49er.cache.ViewModelCacheSingleton;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.ui.actions.HoursPerDayValidation;
import org.rares.miner49er.domain.entries.ui.actions.TimeEntryActionFragment;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;
import org.rares.miner49er.util.UiUtil;

import java.util.Map;
import java.util.UUID;

import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.DATE_PATTERN;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_COMMENTS;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_DATE_ADDED;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_HOURS_WORKED;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_ISSUE_NAME;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_PROJECT_NAME;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_WORK_DATE;
import static org.rares.miner49er.domain.issues.IssuesInterfaces.KEY_ISSUE_ID;
import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_OWNER_NAME;

public class TimeEntryAddFormFragment extends TimeEntryActionFragment {

    public static final String TAG = TimeEntryAddFormFragment.class.getSimpleName();
    private ViewModelCache cache = ViewModelCacheSingleton.getInstance();

    public static TimeEntryAddFormFragment newInstance() {
        return new TimeEntryAddFormFragment();
    }

    public TimeEntryAddFormFragment() {
    }

    public TimeEntryAddFormFragment(BaseInterfaces.ActionFragmentDependencyProvider provider){
        projectsDAO = provider.getProjectsDAO();
        issuesDAO = provider.getIssuesDAO();
        timeEntriesDAO = provider.getTimeEntriesDAO();
        usersDAO = provider.getUsersDAO();
        loggedInUser = provider.getCache().loggedInUser;
        replacedView = provider.getReplacedView();
        resultListener = provider.getResultListener();
    }

    @Override
    public String getActionTag() {
        return TAG;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return createView(inflater, container);
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        if (args != null) {
            Optional<IssueData> issueDataOptional = issuesDAO.get(args.getLong(KEY_ISSUE_ID, -1), true).blockingGet();
            if (issueDataOptional.isPresent()) {
                issueData = issueDataOptional.get().clone(true);
            }
        }
        Bundle bundle = new Bundle();
        if (issueData != null) {
            bundle.putString(KEY_ISSUE_NAME, issueData.getName());
        }

        populateFields(bundle);
    }


    @Override
    @OnClick(R.id.btn_add)
    public void applyAction() {
        boolean ok = validateForm() && addTimeEntry();
        if (ok) {
            prepareExit();
        }
    }

    public boolean validateForm() {
        clearErrors();
        if (timeEntryData == null) {
            timeEntryData = new TimeEntryData();
        }
        updateData();
        FormValidator<TimeEntryData> validator = FormValidator.of(timeEntryData);
        try {
            validator.validate(TimeEntryData::getWorkDate, n -> n != -1, workDateInputLayout, errRequired)
                    .validate(TimeEntryData::getWorkDate,
                            HoursPerDayValidation.builder()
                                    .dao(timeEntriesDAO)
                                    .timeEntryData(timeEntryData)
                                    .maxHours(maxHours)
                                    .build()
                                    .validation(),
                            hoursWorkedInputLayout, String.format(errTimeEntryTooManyHours, maxHours))
                    .validate(TimeEntryData::getUserId, o -> o != -1, ownerInputLayout, errRequired)
                    .validate(TimeEntryData::getComments, d -> d.length() <= maxCommentCharacters, commentsInputLayout, errCharactersNumber)
                    .validate(TimeEntryData::getHours, d -> d >= minHours && d <= maxHours,
                            hoursWorkedInputLayout, String.format(errTimeEntryIncorrectHours, minHours, maxHours))
                    .get();
        } catch (FormValidationException e) {
            int scrollToY = container.getHeight();
            int diff = (int) UiUtil.pxFromDp(getContext(), 15);
            Map<Object, String> errors = e.getInvalidFields();
            for (Object o : errors.keySet()) {
                TextInputLayout layout = ((TextInputLayout) o);
                layout.setError(errors.get(o));
                scrollToY = Math.min((int) layout.getY() - diff, scrollToY);
            }
            rootView.smoothScrollTo(0, scrollToY);
            return false;
        }
        return true;
    }


    private boolean addTimeEntry() {
        timeEntryData.objectId = UUID.randomUUID().toString();
        timeEntryData.id = timeEntriesDAO.insert(timeEntryData).blockingGet(); //

        final TimeEntryData toDelete = timeEntryData.clone();

        timeEntryData = new TimeEntryData();

        final String snackbarText = successfulAdd;  // // TODO: 10.06.2019 use the activity to show this stuff?
        Snackbar snackbar = Snackbar.make(container, snackbarText, Snackbar.LENGTH_LONG);
//        Drawable snackbarBackground = getContext().getResources().getDrawable(R.drawable.background_snackbar);
        View snackbarView = snackbar.getView();

        snackbarView.setBackgroundColor(snackbarBackgroundColor);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(snackbarTextColor);

        snackbar.setAction(R.string.action_undo, v -> {
            boolean deleted = timeEntriesDAO.delete(toDelete).blockingGet();
            snackbar.dismiss();
            snackbarView.postDelayed(() -> {
                snackbar.setText(deleted ? entryRemoved : errNotRemoved);
                textView.setTextColor(deleted ? snackbarTextColor : errorTextColor);
                snackbar.setAction(R.string.action_dismiss, d -> snackbar.dismiss());
                snackbar.show();
            }, 500);
        });

        resetFields();
        snackbar.show();
        return true;
    }

    private void populateFields(Bundle bundle) {

        Optional<ProjectData> optional = projectsDAO.get(issueData.parentId, true).blockingGet();
        if (optional.isPresent()) {
            projectData = optional.get().clone(true);
        }
        userData = cache.loggedInUser;

        String projectName = bundle.getString(KEY_PROJECT_NAME, projectData.getName());
        String issueName = bundle.getString(KEY_ISSUE_NAME, "");
        String ownerName = bundle.getString(KEY_OWNER_NAME, userData.getName());
        String workDate = bundle.getString(KEY_WORK_DATE, DateTime.now().toString(DATE_PATTERN));
        String dateAdded = bundle.getString(KEY_DATE_ADDED, DateTime.now().toString(DATE_PATTERN));
        String comments = bundle.getString(KEY_COMMENTS, "");
        String hoursWorked = bundle.getString(KEY_HOURS_WORKED, "");

        clearErrors();

        projectNameEditText.setText(projectName);
        issueNameEditText.setText(issueName);
        ownerEditText.setText(ownerName);
        workDateEditText.setText(workDate);
        dateAddedEditText.setText(dateAdded);
        commentsEditText.setText(comments);
        hoursWorkedEditText.setText(hoursWorked);
    }

    @Override
    protected void updateData() {
        super.updateData();
        timeEntryData.id = null;
    }
}

