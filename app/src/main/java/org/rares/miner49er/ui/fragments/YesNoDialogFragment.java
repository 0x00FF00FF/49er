package org.rares.miner49er.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import lombok.Setter;
import org.rares.miner49er.BaseInterfaces.Messenger;
import org.rares.miner49er.R;

public class YesNoDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = YesNoDialogFragment.class.getSimpleName();

    private static final String QUESTION_TEMPLATE = "template";
    private static final String QUESTION_DETAILS = "details";
    private static final String QUESTION_PARAM = "param";

    @Setter
    private Listener listener;
    private Unbinder unbinder;

    @BindView(R.id.dialog_question)
    AppCompatTextView questionTextView;

    @BindView(R.id.question_details)
    AppCompatTextView questionDetailsTextView;

    public static YesNoDialogFragment newInstance(String param, int template, int details) {
        final YesNoDialogFragment fragment = new YesNoDialogFragment();
        final Bundle args = new Bundle();
        args.putString(QUESTION_PARAM, param);
        args.putInt(QUESTION_TEMPLATE, template);
        args.putInt(QUESTION_DETAILS, details);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_item_list_dialog, container, false);
        unbinder = ButterKnife.bind(this, root);
        String template = getContext().getResources().getString(getArguments().getInt(QUESTION_TEMPLATE));
        String details = getContext().getResources().getString(getArguments().getInt(QUESTION_DETAILS));
        questionTextView.setText(String.format(template, getArguments().getString(QUESTION_PARAM)));
        questionDetailsTextView.setText(details);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        FrameLayout bottomSheet =
                getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @OnClick(R.id.btn_yes)
    void yesPressed() {
        Context context = getContext();
        if (listener != null) {
            listener.onYes();
        } else {
            ((Messenger) context).showMessage(
                    context.getResources().getString(R.string.err_operation_not_completed_try_again),
                    Messenger.DISMISSIBLE,
                    null);
        }
        dismiss();
    }

    @OnClick(R.id.btn_no)
    void noPressed() {
        if (listener != null) {
            listener.onNo();
        }
        dismiss();
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
        unbinder.unbind();
        unbinder = null;
    }

    public interface Listener {
        void onYes();

        void onNo();
    }
}
