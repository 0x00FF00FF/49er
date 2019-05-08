package org.rares.miner49er.ui.fragments.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import org.rares.miner49er.R;

public class TacPpFragment extends Fragment {
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tac_pp, container, false);
        unbinder = ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.tac_text)
    void showTaC() {
        Toast.makeText(getContext(), "You hereby agree with our terms.", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.pp_text)
    void showPrivacyPolicy() {
        Toast.makeText(getContext(), "You trust us with your data.", Toast.LENGTH_LONG).show();
    }
}
