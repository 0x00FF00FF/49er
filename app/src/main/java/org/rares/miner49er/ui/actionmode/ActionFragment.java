package org.rares.miner49er.ui.actionmode;

import android.view.View;
import android.widget.ScrollView;
import androidx.fragment.app.Fragment;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.Unbinder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import lombok.Getter;
import lombok.Setter;
import org.rares.miner49er.R;
import org.rares.miner49er.persistence.dao.AbstractViewModel;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.transitions.ActionFragmentTransition;
import org.rares.miner49er.ui.actionmode.transitions.TranslationTransition;

import java.util.List;

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

    @BindColor(R.color.indigo_100_blacked)
    protected int snackbarBackgroundColor;

    @BindColor(R.color.pureWhite)
    protected int snackbarTextColor;

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

    protected ScrollView rootView;
    protected Unbinder unbinder;


    protected boolean validateEmptyText(TextInputEditText editText, TextInputLayout layout) {
        if (!"".equals(editText.getText().toString().trim())) {
            layout.setError("");
        } else {
            editText.setText("");
            layout.setError(errRequired);
            return false;
        }
        return true;
    }

    protected boolean validateCharacters(TextInputEditText editText, TextInputLayout layout) {
        if (!editText.getText().toString().contains("#")) {
            layout.setError("");
        } else {
            layout.setError(errCharacters);
            return false;
        }
        return true;
    }

    protected boolean validateExistingName(
            TextInputEditText editText,
            TextInputLayout layout,
            AsyncGenericDao<? extends AbstractViewModel> dao) {

        List<?> entities = dao.getMatching(editText.getEditableText().toString(), true).blockingGet();      //

        if (entities == null || entities.isEmpty()) {
            layout.setError("");
            return true;
        } else {
            layout.setError(errExists);
        }
        return false;
    }
}
