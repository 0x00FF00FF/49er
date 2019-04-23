package org.rares.miner49er.domain.issues.ui.actions.add;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.OnClick;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.pushtorefresh.storio3.Optional;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.ui.actions.IssueActionFragment;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;
import org.rares.miner49er.util.UiUtil;

import java.util.List;
import java.util.Map;

import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_PROJECT_ID;

public class IssueAddFormFragment extends IssueActionFragment {

    public static final String TAG = IssueAddFormFragment.class.getSimpleName();

    public static IssueAddFormFragment newInstance() {
        return new IssueAddFormFragment();
    }

    public IssueAddFormFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return createView(inflater, container);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
            long projectId = args.getLong(KEY_PROJECT_ID, -1L);
            if (projectId == -1) {
                throw new IllegalStateException("To add an issue you need a project id.");
            }
            populateFields(projectId);
        } else {
            Log.w(TAG, "onCreateView: BUNDLE NULL");
        }
    }

    @Override
    public String getActionTag() {
        return TAG;
    }

    @Override
    @OnClick(R.id.btn_add_issue)
    public void applyAction() {
        if (validateForm() && addIssue()) {
            prepareExit();
        }
    }

    @Override
    public boolean validateForm() {
        clearErrors();
        if (issueData == null) {
            issueData = new IssueData();
        }
        updateIssueData();
        FormValidator<IssueData> validator = FormValidator.of(issueData);
        try {
            validator
                    .validate(IssueData::getName, n -> !n.isEmpty(), issueNameInputLayout, errRequired)
                    .validate(IssueData::getName, n -> !n.contains("#"), issueNameInputLayout, errCharacters)
                    .validate(IssueData::getName, n -> {
                        List<? extends AbstractViewModel> entities =
                                issuesDAO.getMatching(n, Optional.of(issueData.parentId), true).blockingGet();
                        return (entities == null || entities.isEmpty());
                    }, issueNameInputLayout, errExists)
                    .get();
        } catch (FormValidationException e) {
            int scrollToY = contentContainer.getHeight();
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

    private boolean addIssue() {
        issueData.id = issuesDAO.insert(issueData).blockingGet();
        IssueData toDelete = issueData.clone(true);
        issueData = new IssueData();

        final String snackbarText = String.format(successfulAdd, issueNameEditText.getEditableText().toString());
        Snackbar snackbar = Snackbar.make(contentContainer, snackbarText, Snackbar.LENGTH_LONG);
//        Drawable snackbarBackground = getContext().getResources().getDrawable(R.drawable.background_snackbar);
        View snackbarView = snackbar.getView();

        snackbarView.setBackgroundColor(snackbarBackgroundColor);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(snackbarTextColor);

        snackbar.setAction(R.string.action_undo, v -> {
            final boolean deleted = issuesDAO.delete(toDelete).blockingGet();
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

    @Override
    protected void updateIssueData() {
        super.updateIssueData();
        issueData.id = null;
    }
}
