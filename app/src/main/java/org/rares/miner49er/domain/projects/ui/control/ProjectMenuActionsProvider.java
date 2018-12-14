package org.rares.miner49er.domain.projects.ui.control;

import androidx.fragment.app.FragmentManager;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectAddActionListener;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectEditFormFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;

public class ProjectMenuActionsProvider implements GenericMenuActions {

    private static final String TAG = ProjectMenuActionsProvider.class.getSimpleName();
    private FragmentManager fragmentManager;
    private ActionListenerManager actionManager;

    private ProjectEditFormFragment projectEditFormFragment;
    private ProjectAddActionListener projectAddActionListener;

    ProjectMenuActionsProvider(FragmentManager fragmentManager, ActionListenerManager manager) {
        this.fragmentManager = fragmentManager;
        actionManager = manager;
        projectEditFormFragment = new ProjectEditFormFragment();
        projectAddActionListener = new ProjectAddActionListener(projectEditFormFragment, actionManager);
    }

    @Override
    public boolean add(int id) {

        // show add issue fragment
        return false;
    }

    @Override
    public boolean edit(int id) {
        return false;
    }

    @Override
    public boolean remove(int id) {
        return false;
    }

    @Override
    public boolean details(int id) {
        return false;
    }

    @Override
    public boolean favorite(int id) {
        return false;
    }

    @Override
    public boolean search(int id) {
        return false;
    }

    @Override
    public boolean filter(int id) {
        return false;
    }

    @Override
    public boolean menuAction(int menuActionId, int id) {

        if (menuActionId == R.id.action_add_user) {
            // show add user fragment
        }

        if (menuActionId == R.id.action_add_project) {

            if (fragmentManager.findFragmentByTag(ProjectEditFormFragment.TAG) == null) {

                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.item_animation_from_left, R.anim.item_animation_to_left)
                        .replace(R.id.main_container, projectEditFormFragment, ProjectEditFormFragment.TAG)
                        .addToBackStack(ProjectEditFormFragment.TAG)
                        .show(projectEditFormFragment)
                        .commit();
            }
            actionManager.registerActionListener(projectAddActionListener);
        }
        return true;
    }
}
