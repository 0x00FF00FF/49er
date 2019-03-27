package org.rares.miner49er.ui.actionmode;

import android.view.View;
import android.widget.ScrollView;
import androidx.fragment.app.Fragment;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.Unbinder;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.ui.actionmode.transitions.ActionFragmentTransition;
import org.rares.miner49er.ui.actionmode.transitions.TranslationTransition;

public abstract class ActionFragment extends Fragment implements ActionEnforcer {

    @BindString(R.string.error_field_required)
    protected String errRequired;
    @BindString(R.string.error_field_contains_illegal_characters)
    protected String errCharacters;
    @BindString(R.string.error_field_already_exists)
    protected String errExists;
    @BindString(R.string.success_project_add)
    protected String successfulAdd;
    @BindString(R.string.success_project_save)
    protected String successfulUpdate;
    @BindString(R.string.entry_removed)
    protected String entryRemoved;
    @BindString(R.string.err_entry_not_removed)
    protected String errNotRemoved;
    @BindString(R.string.err_operation_not_completed)
    protected String errNotCompleted;

    @BindColor(R.color.indigo_100_blacked)
    protected int snackbarBackgroundColor;

    @BindColor(R.color.pureWhite)
    protected int snackbarTextColor;

    @BindColor(R.color.error_color)
    protected int errorTextColor;

    public abstract boolean validateForm();

    public abstract void applyAction();

    public abstract String getActionTag();

//    public abstract void onSuccess();
//    public abstract void onFailure();

//    public abstract void populateFields(long entityId);

    @Setter
    protected View replacedView;

    @Setter
    protected ActionFragmentTransition actionFragmentTransition = new TranslationTransition();

    @Getter
    @Setter
    protected ActionEnforcer.FragmentResultListener resultListener;

    protected ScrollView rootView;      // root view should not necessarily be a ScrollView
    protected Unbinder unbinder;
}
