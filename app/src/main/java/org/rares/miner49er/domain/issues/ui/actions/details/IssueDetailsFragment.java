package org.rares.miner49er.domain.issues.ui.actions.details;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.issues.ui.actions.edit.IssueEditFormFragment;

import static org.rares.miner49er.domain.issues.IssuesInterfaces.KEY_ISSUE_ID;

public class IssueDetailsFragment extends IssueEditFormFragment {

    public static IssueDetailsFragment newInstance() {
        return new IssueDetailsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        createView(inflater, container);
        Bundle args = getArguments();
        if (args != null) {
            long issueId = args.getLong(KEY_ISSUE_ID, -1L);
            if (issueId == -1) {
                throw new IllegalStateException("To view issue details you need an issue id.");
            }
            populateFields(issueId);

            applyButton.setIcon(getResources().getDrawable(R.drawable.icon_path_done));
            applyButton.setText(R.string.action_save);
        } else {
            Log.w(TAG, "onCreateView: BUNDLE NULL");
        }

        projectNameEditText.setEnabled(false);
        issueNameEditText.setEnabled(false);
        issueOwnerEditText.setEnabled(false);
        dateAddedEditText.setEnabled(false);
        applyButton.setEnabled(false);

        cancelButton.setVisibility(View.GONE);
        applyButton.setVisibility(View.GONE);

        return rootView;
    }

}
