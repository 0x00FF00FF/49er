package org.rares.miner49er.domain.projects.ui.control;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.issues.ui.actions.add.IssueAddActionListener;
import org.rares.miner49er.domain.issues.ui.actions.add.IssueAddFormFragment;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.projects.ui.actions.add.ProjectAddActionListener;
import org.rares.miner49er.domain.projects.ui.actions.add.ProjectAddFormFragment;
import org.rares.miner49er.domain.projects.ui.actions.details.ProjectDetailsActionListener;
import org.rares.miner49er.domain.projects.ui.actions.details.ProjectDetailsFragment;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectEditActionListener;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectEditFormFragment;
import org.rares.miner49er.domain.projects.ui.actions.remove.ProjectRemoveAction;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.fragments.YesNoDialogFragment;

import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_PROJECT_ID;

public class ProjectMenuActionsProvider
        implements GenericMenuActions {

    private static final String TAG = ProjectMenuActionsProvider.class.getSimpleName();
    private FragmentManager fragmentManager;
    private ActionListenerManager actionManager;
    private ProjectRemoveAction projectRemoveAction;

    ProjectMenuActionsProvider(FragmentManager fragmentManager, ActionListenerManager manager, ProjectRemoveAction projectRemoveAction) {
        this.fragmentManager = fragmentManager;
        actionManager = manager;
        this.projectRemoveAction = projectRemoveAction;
    }

    @Override
    public boolean add(long id) {

        // show add issue fragment

        ActionFragment issueAddFormFragment = IssueAddFormFragment.newInstance();
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putLong(KEY_PROJECT_ID, id);
        issueAddFormFragment.setArguments(fragmentArgs);
        IssueAddActionListener issueAddActionListener = new IssueAddActionListener(issueAddFormFragment, actionManager);

        showFragment(issueAddFormFragment);

        actionManager.registerActionListener(issueAddActionListener);

        return true;
    }

    @Override
    public boolean edit(long id) {
        ActionFragment projectEditFormFragment = ProjectEditFormFragment.newInstance(id);

        ProjectEditActionListener projectEditActionListener = new ProjectEditActionListener(projectEditFormFragment, actionManager);
        showFragment(projectEditFormFragment);

        actionManager.registerActionListener(projectEditActionListener);

        return true;
    }

    @Override
    public boolean remove(long id) {
        String projectName = InMemoryCacheAdapterFactory.ofType(ProjectData.class).get(id, true).blockingGet().get().getName();
        YesNoDialogFragment removeYnDialog =
                YesNoDialogFragment.newInstance(projectName, R.string.question_delete_project, R.string.details_question_delete_project);

        projectRemoveAction.setProjectId(id);

        removeYnDialog.setListener(projectRemoveAction);
        removeYnDialog.show(fragmentManager, YesNoDialogFragment.TAG);
        return true;
    }

    @Override
    public boolean details(long id) {
        ActionFragment projectDetailsFragment = ProjectDetailsFragment.newInstance(id);

        ProjectDetailsActionListener projectDetailsActionListener = new ProjectDetailsActionListener(projectDetailsFragment, actionManager);
        showFragment(projectDetailsFragment);

        actionManager.registerActionListener(projectDetailsActionListener);

        return true;
    }

    @Override
    public boolean favorite(long id) {
        return false;
    }

    @Override
    public boolean search(long id) {
        return false;
    }

    @Override
    public boolean filter(long id) {
        return false;
    }

    @Override
    public boolean menuAction(int menuActionId, long id) {

        if (menuActionId == R.id.action_add_user) {
            // show add user fragment
        }

        if (menuActionId == R.id.action_add_project) {
            ActionFragment projectAddFormFragment = ProjectAddFormFragment.newInstance();
            ProjectAddActionListener projectAddActionListener = new ProjectAddActionListener(projectAddFormFragment, actionManager);

            showFragment(projectAddFormFragment);

            actionManager.registerActionListener(projectAddActionListener);
        }
        return true;
    }

    private void showFragment(ActionFragment fragment) {
        String tag = fragment.getActionTag();
            fragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.item_animation_from_left, R.anim.item_animation_to_left)
                    .replace(R.id.main_container, fragment, tag)
                    .show(fragment)
                    .commit();
    }
}
