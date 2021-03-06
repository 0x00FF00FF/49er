package org.rares.miner49er.domain.issues.ui.actions.edit;

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
import org.joda.time.DateTime;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.issues.model.IssueData;
import org.rares.miner49er.domain.issues.ui.actions.IssueActionFragment;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;
import org.rares.miner49er.util.UiUtil;

import java.util.List;
import java.util.Map;

import static org.rares.miner49er.domain.issues.IssuesInterfaces.KEY_ISSUE_ID;

public class IssueEditFormFragment extends IssueActionFragment {

    public static final String TAG = IssueEditFormFragment.class.getSimpleName();

    public static IssueEditFormFragment newInstance() {
        return new IssueEditFormFragment();
    }

    public IssueEditFormFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        createView(inflater, container);
        Bundle args = getArguments();
        if (args != null) {
            long issueId = args.getLong(KEY_ISSUE_ID, -1L);
            if (issueId == -1) {
                throw new IllegalStateException("To edit an issue you need an issue id.");
            }
            populateFields(issueId);

            applyButton.setIcon(getResources().getDrawable(R.drawable.icon_path_done));
            applyButton.setText(R.string.action_save);
        } else {
            Log.w(TAG, "onCreateView: BUNDLE NULL");
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (issueData != null) {
            populateFields(issueData.id);
        }
    }

    @Override
    public String getActionTag() {
        return TAG;
    }

    @Override
    @OnClick(R.id.btn_add_issue)
    public void applyAction() {
        if (validateForm() && saveIssue()) {
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
                    .validate(IssueData::getOwner, o-> o!=null, issueOwnerInputLayout, errRequired)
                    .validate(IssueData::getName, n -> !n.isEmpty(), issueNameInputLayout, errRequired)
                    .validate(IssueData::getName, n -> !n.contains("#"), issueNameInputLayout, errCharacters)
                    .validate(IssueData::getName, n -> {
                        List<? extends AbstractViewModel> entities =
                                issuesDAO.getMatching(n, Optional.of(issueData.parentId), true).blockingGet();
                        if ((entities == null || entities.isEmpty())) {
                            return true;
                        }
                        // TODO: optimize this by querying the database for results not including issueData.id
                        for (int i = 0; i < entities.size(); i++) {
                            IssueData isData = (IssueData) entities.get(i);
                            if (isData.id.equals(issueData.id)) {
                                entities.remove(isData);
                                break;
                            }
                        }
                        Log.i(TAG, "validateForm: " + entities);
                        return entities.isEmpty();
                    }, issueNameInputLayout, errExists)
                .validate(IssueData::getObjectId, oid-> oid!=null, projectNameInputLayout, "no oid :|")
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

    private boolean saveIssue() {
        boolean updated = issuesDAO.update(issueData).blockingGet();

        if (!updated) {
            Log.w(TAG, "saveIssue: could not save issue.");
            return false;
        }

        final String snackbarText = String.format(successfulSave, issueNameEditText.getEditableText().toString());
        Snackbar snackbar = Snackbar.make(contentContainer, snackbarText, Snackbar.LENGTH_LONG);
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

    @Override
    protected void populateFields(long issueId) {
        Log.d(TAG, "populateFields() called with: issueId = [" + issueId + "]");
        disposable.add(issuesDAO.get(issueId, true).subscribe(
                optionalData -> {
                    if (optionalData.isPresent()) {
                        issueData = optionalData.get().clone(true);
                        issueNameEditText.setText(issueData.getName());
                        dateAddedEditText.setText(new DateTime(issueData.getDateAdded()).toString("EE, d MMMM, y"));
                        if (issueData.getOwner() == null) {
                            disposable.add(usersDAO.get(issueData.getOwnerId(), true).subscribe(
                                    optionalUser -> {
                                        if (optionalUser.isPresent()) {
                                            userData = optionalUser.get().clone();
                                        }
                                        issueData.setOwner(userData);
                                        issueOwnerEditText.setText(userData == null ? "" : userData.getName());
                                    }
                            ));
                        } else {
                            userData = issueData.getOwner().clone();
                            issueOwnerEditText.setText(userData.getName());
                        }
                        disposable.add(projectsDAO.get(issueData.parentId, true).subscribe(
                                optionalProject -> {
                                    if (optionalProject.isPresent()) {
                                        projectData = optionalProject.get().clone(true);
                                        projectNameEditText.setText(projectData.getName());
                                    }
                                }
                        ));
                    }
                }
        ));
    }
}
