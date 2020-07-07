package org.rares.miner49er.domain.projects.ui.actions.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectEditFormFragment;

import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_PROJECT_ID;

public class ProjectDetailsFragment extends ProjectEditFormFragment {

    public static ProjectDetailsFragment newInstance(long projectId) {
        Bundle args = new Bundle();
        args.putLong(KEY_PROJECT_ID, projectId);

        ProjectDetailsFragment fragment = new ProjectDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ProjectDetailsFragment() {
    }

    @Override
    public String getActionTag() {
        return TAG;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        createView(inflater, container);

        Object pid = getArguments().get(KEY_PROJECT_ID);
        if (pid == null) {
            throw new IllegalStateException("To edit a project you need an id.");
        }
        populateFields(getArguments().getLong(KEY_PROJECT_ID));

        btnApply.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnEditUsers.setVisibility(View.GONE);
        btnEditUsers.setEnabled(false);
        btnApply.setEnabled(false);
        editTextProjectName.setEnabled(false);
        editTextProjectShortName.setEnabled(false);
        editTextProjectDescription.setEnabled(false);
        editTextProjectIcon.setEnabled(false);
        editTextProjectOwner.setEnabled(false);
        rootView.findViewById(R.id.user_list_container).setEnabled(false);
        return rootView;
    }
}
