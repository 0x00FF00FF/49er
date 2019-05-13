package org.rares.miner49er.ui.fragments.login.simple;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import org.rares.miner49er.R;

public class LoginLandingFragment extends Fragment {

    public static final String TAG = LoginLandingFragment.class.getSimpleName();

    private Unbinder unbinder;

    @BindView(R.id.sign_up_button)
    ExtendedFloatingActionButton signUpFab;
    @BindView(R.id.sign_in_button)
    ExtendedFloatingActionButton signInFab;

    private LandingListener landingListener;

    public static LoginLandingFragment newInstance() {
        Bundle args = new Bundle();
        LoginLandingFragment fragment = new LoginLandingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginLandingFragment() {
    }

    /**
     * Interface that allows to act upon a user signing in or signing up in the app.
     */
    public interface LandingListener {
        void showSignUp();

        void showSignIn();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            landingListener = (LandingListener) getParentFragment();
        } else {
            landingListener = (LandingListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_landing, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        unbinder = null;
    }

    @OnClick(R.id.sign_up_button)
    void signUp() {
        landingListener.showSignUp();
    }

    @OnClick(R.id.sign_in_button)
    void signIn() {
        landingListener.showSignIn();
    }
}
