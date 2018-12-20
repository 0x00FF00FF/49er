package org.rares.miner49er.domain.projects.ui.control;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.ui.actions.add.ProjectAddActionListener;
import org.rares.miner49er.domain.projects.ui.actions.add.ProjectAddFormFragment;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectEditActionListener;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectEditFormFragment;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ActionListenerManager;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;

import static org.rares.miner49er.domain.projects.ProjectsInterfaces.KEY_PROJECT_ID;

public class ProjectMenuActionsProvider
        implements GenericMenuActions {

    private static final String TAG = ProjectMenuActionsProvider.class.getSimpleName();
    private FragmentManager fragmentManager;
    private ActionListenerManager actionManager;

    private ActionFragment projectAddFormFragment;
    private ProjectAddActionListener projectAddActionListener;

    private ActionFragment projectEditFormFragment;
    private ProjectEditActionListener projectEditActionListener;

    ProjectMenuActionsProvider(FragmentManager fragmentManager, ActionListenerManager manager) {
        this.fragmentManager = fragmentManager;
        actionManager = manager;
    }

    @Override
    public boolean add(long id) {

        // show add issue fragment
        return false;
    }

    @Override
    public boolean edit(long id) {
        if (projectEditFormFragment == null) {
            projectEditFormFragment = ProjectEditFormFragment.newInstance(id);
        } else {
            Bundle args = new Bundle();
            args.putLong(KEY_PROJECT_ID, id);
            projectEditFormFragment.setArguments(args);
        }
        if (projectEditActionListener == null) {
            projectEditActionListener = new ProjectEditActionListener(projectEditFormFragment, actionManager);
        }
        showFragment(projectEditFormFragment);

        actionManager.registerActionListener(projectEditActionListener);

        return true;
    }

    @Override
    public boolean remove(long id) {
        return false;
    }

    @Override
    public boolean details(long id) {
        return false;
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
            if (projectAddFormFragment == null) {
                projectAddFormFragment = ProjectAddFormFragment.newInstance();
            }
            if (projectAddActionListener == null) {
                projectAddActionListener = new ProjectAddActionListener(projectAddFormFragment, actionManager);
            }

            showFragment(projectAddFormFragment);

            actionManager.registerActionListener(projectAddActionListener);
        }
        return true;
    }

    private void showFragment(ActionFragment fragment) {
        String tag = fragment.getActionTag();
        if (fragmentManager.findFragmentByTag(tag) == null) {

            fragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.item_animation_from_left, R.anim.item_animation_to_left)
                    .replace(R.id.main_container, fragment, tag)
                    .addToBackStack(tag)
                    .show(fragment)
                    .commit();
        }
    }
}
