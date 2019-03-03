package org.rares.miner49er.domain.projects.ui.actions.add;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.OnClick;
import com.google.android.material.snackbar.Snackbar;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.ui.actions.ProjectActionFragment;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_DESCRIPTION;
import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_ICON;
import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_NAME;
import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_OWNER_NAME;

public class ProjectAddFormFragment extends ProjectActionFragment {

    public static final String TAG = ProjectAddFormFragment.class.getSimpleName();

    public static ProjectAddFormFragment newInstance() {
        return new ProjectAddFormFragment();
    }

    public ProjectAddFormFragment() {
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
//        UiUtil.sendViewToBack(getView());
    }



    @Override
    @OnClick(R.id.btn_add_project)
    public void applyAction() {
        boolean ok = validateForm() && addProject();
        if (ok) {
            prepareExit();
        }
    }

    public boolean validateForm() {
        boolean validForm = true;
        int scrollToY = container.getHeight();
        int diff = (int) UiUtil.pxFromDp(getContext(), 15);

        if (!validateEmptyText(editTextProjectName, inputLayoutProjectName)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectName.getY() - diff, scrollToY);
        } else {
            if (!validateCharacters(editTextProjectName, inputLayoutProjectName)) {
                validForm = false;
                scrollToY = Math.min((int) inputLayoutProjectName.getY() - diff, scrollToY);
            } else {
                if (!validateExistingName(editTextProjectName, inputLayoutProjectName, projectsDAO)) {
                    validForm = false;
                    scrollToY = Math.min((int) inputLayoutProjectName.getY() - diff, scrollToY);
                }
            }
        }

        if (!validateEmptyText(editTextProjectOwner, inputLayoutProjectOwner)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectOwner.getY() - diff, scrollToY);
        } else {
            if (!validateCharacters(editTextProjectOwner, inputLayoutProjectOwner)) {
                validForm = false;
                scrollToY = Math.min((int) inputLayoutProjectOwner.getY() - diff, scrollToY);
            }
        }

        if (!validateCharacters(editTextProjectShortName, inputLayoutProjectShortName)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectShortName.getY() - diff, scrollToY);
        }

        if (!validateCharacters(editTextProjectDescription, inputLayoutProjectDescription)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectDescription.getY() - diff, scrollToY);
        }

        if (!validateCharacters(editTextProjectIcon, inputLayoutProjectIcon)) {
            validForm = false;
            scrollToY = Math.min((int) inputLayoutProjectIcon.getY() - diff, scrollToY);
        }

        if (!validForm) {
            rootView.smoothScrollTo(0, scrollToY);
        }

        return validForm;
    }


    private boolean addProject() {
        final ProjectData newProject = new ProjectData();
        newProject.setName(editTextProjectName.getEditableText().toString());
        newProject.setDescription(editTextProjectDescription.getEditableText().toString());
        newProject.setIcon(editTextProjectIcon.getEditableText().toString());

        newProject.setOwner(usersDAO.get(1, true).blockingGet().get()); //

        newProject.setDateAdded(System.currentTimeMillis());
        newProject.setPicture(editTextProjectIcon.getEditableText().toString());

        List<UserData> users = usersDAO.getAll(true).blockingGet();         //
        List<UserData> team = new ArrayList<>();
        for (UserData u : users) {
            if (u.getRole() != 12) {
                team.add(u);
            }
            if (team.size() > 6) {
                break;
            }
        }
        newProject.setTeam(team);

        final long newProjectId = projectsDAO.insert(newProject).blockingGet(); //

        newProject.setId(newProjectId);

        final String snackbarText = String.format(successfulAdd, editTextProjectName.getEditableText().toString());
        Snackbar snackbar = Snackbar.make(container, snackbarText, Snackbar.LENGTH_LONG);
//        Drawable snackbarBackground = getContext().getResources().getDrawable(R.drawable.background_snackbar);
        View snackbarView = snackbar.getView();

        snackbarView.setBackgroundColor(snackbarBackgroundColor);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(snackbarTextColor);

        snackbar.setAction(R.string.action_undo, v -> {
            projectsDAO.delete(newProject);
            snackbar.dismiss();
        });

        resetFields();
        snackbar.show();
        return true;
    }

    private void populateFields(Bundle bundle) {

        String name = bundle.getString(KEY_NAME);
        if (name == null) {
            name = "";
        }
        String description = bundle.getString(KEY_DESCRIPTION);
        if (description == null) {
            description = "";
        }
        String icon = bundle.getString(KEY_ICON);
        if (icon == null) {
            icon = "";
        }
        String ownerName = bundle.getString(KEY_OWNER_NAME);
        if (ownerName == null) {
            ownerName = "";
        }

        inputLayoutProjectName.setError("");
        editTextProjectName.setText(name);

        inputLayoutProjectShortName.setError("");
        editTextProjectShortName.setText(TextUtils.extractVowels(name));

        inputLayoutProjectDescription.setError("");
        editTextProjectDescription.setText(description);

        inputLayoutProjectIcon.setError("");
        editTextProjectIcon.setText(icon);

        inputLayoutProjectOwner.setError("");
        editTextProjectOwner.setText(ownerName);
    }

}
