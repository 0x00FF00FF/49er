package org.rares.miner49er.domain.projects.ui.actions.edit;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import org.rares.miner49er.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectEditFormFragment extends DialogFragment {

    public static final String TAG = ProjectEditFormFragment.class.getSimpleName();
//    private FragmentFocusListener focusListener = new FragmentFocusListener();
//    private FragmentTouchListener touchListener = new FragmentTouchListener();
//    private FragmentMotionEventListener motionEventListener = new FragmentMotionEventListener();

    public ProjectEditFormFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_project_edit, container, false);

//        v.setOnFocusChangeListener(focusListener);
//        v.setOnTouchListener(touchListener);
//        v.setOnGenericMotionListener(motionEventListener);

        return v;
    }

    @Override
    public void setStyle(int style, int theme) {
        super.setStyle(STYLE_NORMAL, theme);
    }

    public boolean validate() {
        return false;
    }

    public boolean addProject() {
//        repository.add()
        return true;
    }

    class FragmentFocusListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.d(TAG, "onFocusChange() called with: v = [" + v + "], hasFocus = [" + hasFocus + "]");
            if (v != null && v.equals(getView())) {
                if (!hasFocus) {
                    dismiss();
                }
            }
        }
    }

    class FragmentMotionEventListener implements View.OnGenericMotionListener {

        @Override
        public boolean onGenericMotion(View v, MotionEvent event) {
            return true;
        }
    }

    class FragmentTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v != null) {
                if (v.equals(getView())) {
                    Log.i(TAG, "onTouch: >>>> on root view");
                    return true;
                } else {
                    Log.d(TAG, "onTouch: >>>> on view: " + v);
                    v.performClick();
                }
            }
            return false;
        }
    }

}
