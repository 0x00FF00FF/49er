package org.rares.miner49er.domain.projects.ui.control;

import android.util.Log;
import androidx.fragment.app.FragmentManager;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectAddActionListener;
import org.rares.miner49er.domain.projects.ui.actions.edit.ProjectEditFormFragment;
import org.rares.miner49er.ui.actionmode.GenericMenuActions;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;

public class ProjectMenuActionsProvider implements GenericMenuActions {

    private static final String TAG = ProjectMenuActionsProvider.class.getSimpleName();
    private FragmentManager fragmentManager;
    ToolbarActionManager toolbarActionManager;

    ProjectMenuActionsProvider(FragmentManager fragmentManager, ToolbarActionManager toolbarManager) {
        this.fragmentManager = fragmentManager;
        toolbarActionManager = toolbarManager;
        projectEditFormFragment = new ProjectEditFormFragment();
        projectAddActionListener = new ProjectAddActionListener(projectEditFormFragment);
    }

    private ProjectEditFormFragment projectEditFormFragment;
    private ProjectAddActionListener projectAddActionListener;

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
        Log.i(TAG, "menuAction:  here");


        if (menuActionId == R.id.action_add_user) {
            // show add user fragment
        }

        if (menuActionId == R.id.action_add_project) {

            if (fragmentManager.findFragmentByTag(ProjectEditFormFragment.TAG) == null) {
                fragmentManager
                        .beginTransaction()
                        .add(R.id.main_container, projectEditFormFragment, ProjectEditFormFragment.TAG)
                        .addToBackStack(ProjectEditFormFragment.TAG)
                        .show(projectEditFormFragment)
                        .commit();
            }
            toolbarActionManager.registerActionListener(projectAddActionListener);
        }
        return true;
    }
}
