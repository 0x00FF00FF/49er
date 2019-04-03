package org.rares.miner49er.domain.entries.ui.actions;

import android.content.Context;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.entries.model.TimeEntryData;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.util.TextUtils;

import java.util.Calendar;
import java.util.Locale;

import static android.text.InputType.TYPE_NULL;
import static org.rares.miner49er.domain.entries.TimeEntriesInterfaces.DATE_PATTERN;


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
    protected UserData loggedInUser = null;
    protected ProjectData projectData = null;

    @BindInt(R.integer.max_hours)
    protected int maxHours;
    @BindInt(R.integer.min_hours)
    protected int minHours;
    @BindInt(R.integer.comment_max_length)
    protected int maxCommentCharacters;

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
        workDateEditText.setInputType(TYPE_NULL);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        projectsDAO = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        issuesDAO = InMemoryCacheAdapterFactory.ofType(IssueData.class);
        timeEntriesDAO = InMemoryCacheAdapterFactory.ofType(TimeEntryData.class);
        usersDAO = InMemoryCacheAdapterFactory.ofType(UserData.class);
        loggedInUser = usersDAO.get(12, true).blockingGet().get();   ///
        Log.i(TAG, "onAttach: currently 'logged in user' is " + loggedInUser.getName());
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

        Context context = getContext();
        if (context != null) {
            TextUtils.hideKeyboardFrom(context, rootView.findFocus());
        }

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
            String inputHours = hoursWorkedEditText.getEditableText().toString();
            timeEntryData.setHours(inputHours.length() > 2 ? Integer.MAX_VALUE : Integer.parseInt(inputHours));
        }
        timeEntryData.setUserId(userData.id);
        timeEntryData.setUserName(userData.getName());
        timeEntryData.setUserPhoto(userData.getPicture());
        DateTimeFormatter format = DateTimeFormat.forPattern(DATE_PATTERN);
        timeEntryData.setWorkDate(DateTime.parse(workDateEditText.getEditableText().toString(), format).getMillis());
//        timeEntryData.setWorkDate(System.currentTimeMillis());
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

    private OnDateSetListener dateSetListener = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            DateTime day = new DateTime(year, monthOfYear + 1, dayOfMonth, 9, 0);
            workDateEditText.setText(day.toString(DATE_PATTERN));
        }
    };

    @OnFocusChange(R.id.work_date_edit_text)
    void selectDateOnFocus(boolean focused) {
        if (focused) {
            long workDate = timeEntryData == null ? 0 : timeEntryData.getDateAdded();
            DateTime date = workDate == 0 ? DateTime.now() : new DateTime(workDate);
            DatePickerDialog dpd = DatePickerDialog.newInstance(
                    dateSetListener,
                    date.getYear(), // Initial year selection
                    date.getMonthOfYear() - 1, // -1 because the library expects Calendar format month
                    date.getDayOfMonth() // Inital day selection
            );
            dpd.dismissOnPause(true);
            dpd.setThemeDark(true);
            Calendar minDate = date.toCalendar(Locale.GERMANY);
            minDate.add(Calendar.DAY_OF_YEAR, -7);
            dpd.setMinDate(minDate);
            Calendar maxDate = date.toCalendar(Locale.GERMANY);
            maxDate.add(Calendar.DAY_OF_YEAR, 7);
            dpd.setMaxDate(maxDate);
            dpd.setOkColor(getResources().getColor(R.color.colorPrimaryDark));
            dpd.setCancelColor(getResources().getColor(R.color.pureWhite));
            dpd.setAccentColor(getResources().getColor(R.color.colorAccent));
            dpd.setVersion(DatePickerDialog.Version.VERSION_1);
            dpd.setScrollOrientation(DatePickerDialog.ScrollOrientation.HORIZONTAL);
            dpd.setBackground(R.drawable.background_date_picker_white);
            FragmentManager manager = getFragmentManager();
            if (manager != null) {
                dpd.show(manager, "Datepickerdialog");
            } else {
                Log.w(TAG, "Fragment manager is null.");
            }
        }
    }

    @OnClick(R.id.work_date_edit_text)
    void selectDateOnClick() {
        if (workDateEditText.isFocused()) {
            selectDateOnFocus(true);
        }
    }

    protected boolean isLoggedInUser() {
        return userData.id.equals(loggedInUser.id);
    }

    private static final String TAG = TimeEntryActionFragment.class.getSimpleName();
}
