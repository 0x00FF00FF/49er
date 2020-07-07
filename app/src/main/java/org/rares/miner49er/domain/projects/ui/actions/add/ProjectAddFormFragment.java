package org.rares.miner49er.domain.projects.ui.actions.add;


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
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.ui.actions.ProjectActionFragment;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        editTextProjectOwner.setText(cache.loggedInUser.getName());
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
        clearErrors();
        if (projectData == null) {
            projectData = new ProjectData();
        }
        updateProjectData();
        FormValidator<ProjectData> validator = FormValidator.of(projectData);
        try {
            validator.validate(ProjectData::getName, n -> !n.isEmpty(), inputLayoutProjectName, errRequired)
                    .validate(ProjectData::getName, p -> !p.contains("#"), inputLayoutProjectName, errCharacters)
                    .validate(ProjectData::getName, name -> {
                        List<? extends AbstractViewModel> entities =
                                projectsDAO.getMatching(name, Optional.of(null), true).blockingGet();
                        return (entities == null || entities.isEmpty());
                    }, inputLayoutProjectName, errExists)
                    .validate(ProjectData::getOwner, o -> o != null, inputLayoutProjectOwner, errRequired)
                    .validate(ProjectData::getDescription, d -> !d.contains("#"), inputLayoutProjectDescription, errCharacters)
                    .validate(ProjectData::getIcon, d -> !d.contains("#"), inputLayoutProjectIcon, errCharacters)
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


    private boolean addProject() {
        projectData.setObjectId(UUID.randomUUID().toString());
        final long projectDataId = projectsDAO.insert(projectData).blockingGet(); //

        projectData.setId(projectDataId);

        final ProjectData toDelete = new ProjectData();
        toDelete.updateData(projectData);

        projectData = new ProjectData();

        final String snackbarText = String.format(successfulAdd, editTextProjectName.getEditableText().toString());
        Snackbar snackbar = Snackbar.make(container, snackbarText, Snackbar.LENGTH_LONG);
//        Drawable snackbarBackground = getContext().getResources().getDrawable(R.drawable.background_snackbar);
        View snackbarView = snackbar.getView();

        snackbarView.setBackgroundColor(snackbarBackgroundColor);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(snackbarTextColor);

        snackbar.setAction(R.string.action_undo, v -> {
            snackbar.dismiss();
            snackbarView.postDelayed(() -> {                                    /////////
                boolean deleted = projectsDAO.delete(toDelete).blockingGet();   //////
                if (deleted) {
                    snackbar.setText(entryRemoved);
                } else {
                    textView.setTextColor(errorTextColor);
                    snackbar.setText(errNotRemoved);
                }
                snackbar.setAction(R.string.action_dismiss, d -> snackbar.dismiss());
                snackbar.show();
            }, 500);

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

        clearErrors();
        editTextProjectName.setText(name);
        editTextProjectShortName.setText(TextUtils.extractVowels(name));
        editTextProjectDescription.setText(description);
        editTextProjectIcon.setText(icon);
        editTextProjectOwner.setText(ownerName);
    }

    @Override
    protected void updateProjectData() {
        super.updateProjectData();
        projectData.id = null;

        if (projectData.getOwner() == null) {
            UserData userData = cache.loggedInUser;
            projectData.setOwner(userData); //
            projectData.parentId = projectData.getOwner().id;
        }

        if (projectData.getTeam() == null) {
            projectData.setTeam(Collections.emptyList());
        }

        projectData.setIssues(Collections.emptyList());
    }
}
