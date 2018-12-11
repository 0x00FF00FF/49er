package org.rares.miner49er.ui.actionmode;

import android.view.View;
import androidx.fragment.app.Fragment;
import lombok.Setter;

public abstract class ActionFragment extends Fragment {

    public abstract boolean applyAction();

    public abstract boolean validateForm();

    public abstract void prepareEntry();

    public abstract void prepareExit();

    protected View replacedView;

    @Setter
    protected FragmentDismissListener dismissListener;

    public interface FragmentDismissListener {
        void onFragmentDismiss();
    }
}
