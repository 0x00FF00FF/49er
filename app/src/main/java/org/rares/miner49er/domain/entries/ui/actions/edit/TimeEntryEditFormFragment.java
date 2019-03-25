package org.rares.miner49er.domain.entries.ui.actions.edit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.OnClick;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import org.joda.time.DateTime;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.entries.ui.actions.TimeEntryActionFragment;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;
import org.rares.miner49er.util.UiUtil;

import java.util.List;
import java.util.Map;

import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.KEY_TIME_ENTRY_ID;

public class TimeEntryEditFormFragment extends TimeEntryActionFragment {
    public static final String TAG = TimeEntryEditFormFragment.class.getSimpleName();


    public static TimeEntryEditFormFragment newInstance() {
        return new TimeEntryEditFormFragment();
    }

    public TimeEntryEditFormFragment() {
    }

    @Override
    public String getActionTag() {
        return TAG;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        createView(inflater, container);
        Bundle args = getArguments();
        Log.i(TAG, "onCreateView: " + args);
        long teId = -1;
        if (args != null) {
            teId = args.getLong(KEY_TIME_ENTRY_ID, -1);
        }

        if (teId == -1) {
            throw new IllegalStateException("To edit a time entry you need an id.");
        }

        populateFields(teId);

        btnApply.setIcon(getResources().getDrawable(R.drawable.icon_path_done));
        btnApply.setText(R.string.action_save);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        UiUtil.sendViewToBack(getView());
        if (timeEntryData != null && timeEntryData.getId() > 0) {
            populateFields(timeEntryData.getId());
        }
    }

    @Override
    @OnClick(R.id.btn_add)
    public void applyAction() {
        if (validateForm() && saveTimeEntry()) {
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
                    .validate(TimeEntryData::getWorkDate, date -> {
                        List<? extends AbstractViewModel> entities =
                                timeEntriesDAO.getMatching(issueData.id + " " + date, true).blockingGet();
                        return (entities == null || entities.isEmpty());
                    }, workDateInputLayout, errTimeEntryExists)
                    .validate(TimeEntryData::getUserId, o -> o != -1, ownerInputLayout, errRequired)
                    .validate(TimeEntryData::getComments, d -> !d.contains("#"), commentsInputLayout, errCharacters)
                    .validate(TimeEntryData::getHours, d -> d > 0 && d <= 16, hoursWorkedInputLayout, errCharacters)
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

    private boolean saveTimeEntry() {

        updateData();

        timeEntriesDAO.update(timeEntryData);

        final String snackbarText = successfulSave;

        Snackbar snackbar = Snackbar.make(container, snackbarText, Snackbar.LENGTH_LONG);
//        Drawable snackbarBackground = getContext().getResources().getDrawable(R.drawable.background_snackbar);
        View snackbarView = snackbar.getView();

        snackbarView.setBackgroundColor(snackbarBackgroundColor);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(snackbarTextColor);

        snackbar.setAction(R.string.action_dismiss, v -> {
            snackbar.dismiss();
        });

        resetFields();
        snackbar.show();
        return true;
    }

    private void populateFields(long timeEntryId) {
        if (timeEntryId <= 0) {
            return;
        }

        timeEntryData = timeEntriesDAO.get(timeEntryId, true).blockingGet().get();        ////
        issueData = issuesDAO.get(timeEntryData.parentId, true).blockingGet().get();
        projectData = projectsDAO.get(issueData.parentId, true).blockingGet().get();
        userData = projectData.getTeam().get(0);    ///

        clearErrors();
        issueNameEditText.setText(issueData.getName());
        commentsEditText.setText(timeEntryData.getComments());
        dateAddedEditText.setText(new DateTime(timeEntryData.getDateAdded()).toString("EE, d MMMM, y"));
        hoursWorkedEditText.setText(String.valueOf(timeEntryData.getHours()));
        ownerEditText.setText(timeEntryData.getUserName());
        workDateEditText.setText(new DateTime(timeEntryData.getWorkDate()).toString("EE, d MMMM, y"));
    }

}
