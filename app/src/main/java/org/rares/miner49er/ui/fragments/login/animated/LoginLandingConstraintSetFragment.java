package org.rares.miner49er.ui.fragments.login.animated;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import butterknife.BindFont;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er.R;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.ui.fragments.login.simple.SignInFragment.SignInListener;
import org.rares.miner49er.util.FileUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

// TODO: 10.05.2019 - use a single layout and add constraints for the sets programmatically?
public class LoginLandingConstraintSetFragment extends Fragment {

    public static final String TAG = LoginLandingConstraintSetFragment.class.getSimpleName();

    @BindView(R.id.sign_up_button)
    ExtendedFloatingActionButton signUpFab;

    @BindView(R.id.sign_in_button)
    ExtendedFloatingActionButton signInFab;

    @BindView(R.id.app_logo_image)
    AppCompatImageView appLogo;

    @BindView(R.id.login_floating_button)
    ExtendedFloatingActionButton loginButton;

    @BindView(R.id.error_text)
    AppCompatTextView errorText;

    @BindView(R.id.welcome_text)
    AppCompatTextView welcomeText;

    @BindView(R.id.progress_circular)
    ContentLoadingProgressBar progressBar;

    @BindView(R.id.create_account_btn)
    ExtendedFloatingActionButton createAccountButton;

    @BindView(R.id.user_image_hint_text)
    AppCompatTextView userImageHintTextView;

    @BindView(R.id.user_image)
    CircleImageView userImage;

    @BindView(R.id.user_name_edit_text)
    AppCompatEditText userNameEditText;

    @BindView(R.id.user_email_edit_text)
    AppCompatEditText userEmailEditText;

    @BindView(R.id.user_password_edit_text)
    AppCompatEditText userPasswordEditText;

    @BindFont(R.font.futura_book_bt)
    Typeface passwordTypeface;


    private Unbinder unbinder;
    private FadeListener fadeListener;

    private UserData userData;
    private SignInListener signInListener;
    private CompositeDisposable disposables;

    private UserAccountHelper userAccountHelper;

    private ImagePickUtil imagePickUtil;

    private static final String TAG_PAGE = "page";

    private static final int duration = 250;

    private static final int LANDING = R.layout.content_layout_landing;
    private static final int SIGN_UP = R.layout.content_layout_sign_up;
    private static final int SIGN_IN = R.layout.content_layout_sign_in;

    public static LoginLandingConstraintSetFragment newInstance() {

        Bundle args = new Bundle();

        LoginLandingConstraintSetFragment fragment = new LoginLandingConstraintSetFragment();
        args.putInt(TAG_PAGE, LANDING);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            signInListener = (SignInListener) getParentFragment();
        } else {
            signInListener = (SignInListener) context;
        }
        disposables = new CompositeDisposable();
        fadeListener = new FadeListener();
        userData = new UserData();
        imagePickUtil = new ImagePickUtil(this, disposables);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ConstraintLayout baseLayout = (ConstraintLayout) inflater.inflate(R.layout.layout_landing_cs, container, false);
        unbinder = ButterKnife.bind(this, baseLayout);

        signInFab.setOnClickListener(v -> showSignIn());
        signUpFab.setOnClickListener(v -> showSignUp());
        userImageHintTextView.setOnClickListener(v -> imagePickUtil.start());
        userImage.setOnClickListener(v -> imagePickUtil.start());

        userAccountHelper = new UserAccountHelper(errorText, disposables);

        new PasswordEditTextCustomizer(userPasswordEditText, passwordTypeface).customize(); // static call?

        return baseLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        signInFab.setOnClickListener(null);
        signUpFab.setOnClickListener(null);
        userImageHintTextView.setOnClickListener(null);
        userImage.setOnClickListener(null);
        disposables.dispose();
        disposables = null;
        unbinder.unbind();
        unbinder = null;
        fadeListener.fadeInViews.clear();
        fadeListener.fadeOutViews.clear();
        fadeListener.fadeInViews = null;
        fadeListener.fadeOutViews = null;
        fadeListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ImagePickUtil.PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Context context = getContext();
            String filePath = FileUtils.getPath(context, data.getData());
            UiUtil.sendViewToBack(userImageHintTextView);
            userImageHintTextView.setVisibility(View.GONE);
            userData.setPicture(filePath);
            if (context != null) {
                Glide.with(context)
                        .load(filePath)
                        .into(userImage);
            }
        }
    }

    public boolean onBackPressed() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return false;
        }
        if (arguments.getInt(TAG_PAGE) == SIGN_UP || arguments.getInt(TAG_PAGE) == SIGN_IN) {
            showLanding();
            return true;
        }
        return false;
    }

    private void showSignUp() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
        }
        arguments.putInt(TAG_PAGE, SIGN_UP);

        ConstraintLayout landingLayout = getView().findViewById(R.id.container);

        ConstraintSet signUpSet = new ConstraintSet();
        signUpSet.clone(getContext(), SIGN_UP);

        TransitionSet autoTransition = new TransitionSet();
        autoTransition
                .addListener(fadeListener)
                .addTransition(new ChangeBounds())
                .setOrdering(TransitionSet.ORDERING_TOGETHER);

        autoTransition.setDuration(duration);

        TransitionManager.beginDelayedTransition(landingLayout, autoTransition);
        fadeListener.fadeOutViews.clear();
        fadeListener.fadeInViews.clear();
        fadeListener.fadeOutViews.add(signInFab);
        fadeListener.fadeOutViews.add(signUpFab);
        fadeListener.fadeOutViews.add(welcomeText);
        fadeListener.fadeInViews.add(createAccountButton);
        fadeListener.fadeInViews.add(userImage);
        if (userData != null && userData.getPicture() != null) {
            landingLayout.removeView(userImageHintTextView);
//            fadeListener.fadeInViews.add(userImageHintTextView);
//            userImageHintTextView.setVisibility(View.GONE);
        }
        signUpSet.applyTo(landingLayout);
    }

    private void showSignIn() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
        }
        arguments.putInt(TAG_PAGE, SIGN_IN);

        ConstraintLayout landingLayout = getView().findViewById(R.id.container);

        ConstraintSet signInSet = new ConstraintSet();
        signInSet.clone(getContext(), SIGN_IN);

        TransitionSet autoTransition = new TransitionSet();
        autoTransition
                .addListener(fadeListener)
                .addTransition(new ChangeBounds())
                .setOrdering(TransitionSet.ORDERING_TOGETHER);
        autoTransition.setDuration(duration);

        TransitionManager.beginDelayedTransition(landingLayout, autoTransition);
        fadeListener.fadeOutViews.clear();
        fadeListener.fadeInViews.clear();
        fadeListener.fadeOutViews.add(signInFab);
        fadeListener.fadeOutViews.add(signUpFab);
        fadeListener.fadeOutViews.add(welcomeText);
        fadeListener.fadeInViews.add(loginButton);
        signInSet.applyTo(landingLayout);
    }

    private void showLanding() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
        }

        arguments.putInt(TAG_PAGE, LANDING);

        ConstraintLayout currentLayout = getView().findViewById(R.id.container);

        ConstraintSet landingSet = new ConstraintSet();
        landingSet.clone(getContext(), LANDING);

        TransitionSet autoTransition = new TransitionSet();
        autoTransition
                .addListener(fadeListener)
                .addTransition(new ChangeBounds())
                .addTransition(new Fade(Fade.OUT))
                .addTransition(new Fade(Fade.IN))
                .setOrdering(TransitionSet.ORDERING_TOGETHER);
        autoTransition.setDuration(duration);

        TransitionManager.beginDelayedTransition(currentLayout, autoTransition);
        fadeListener.fadeOutViews.clear();
        fadeListener.fadeInViews.clear();
        fadeListener.fadeInViews.add(signInFab);
        fadeListener.fadeInViews.add(signUpFab);
        fadeListener.fadeInViews.add(welcomeText);
        landingSet.applyTo(currentLayout);
    }

    @OnClick(R.id.create_account_btn)
    void createAccount() {

        if (userData == null) {
            userData = new UserData();
        }

        userData.setName(userNameEditText.getEditableText().toString());
        userData.setEmail(userEmailEditText.getEditableText().toString());
        userData.setPassword(userPasswordEditText.getEditableText().toString());

        if (userAccountHelper.validateUserData(userData)) {
            disableAll();
            createAccountButton.setIcon(getContext().getResources().getDrawable(R.drawable.icon_path_placeholder));
//            createAccountButton.setIconSize((int) UiUtil.pxFromDp(getContext(), 48));
//            createAccountButton.setPadding(0, 0, 0, 0);    // because of the padding set in the xml
            createAccountButton.shrink();
            progressBar.show();

            Consumer<Long> onSuccess = l -> disposables.add(
                    Single.just(1)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((s) -> {
                                userData.id = l;
                                signInListener.signIn(userData);
                                progressBar.hide();
                            }));

            Consumer<Throwable> onError = t -> {
                disposables.add(
                        Single.just(1)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((s) -> {
                                    progressBar.hide();
                                    int padding = (int) UiUtil.dpFromPx(getContext(), 12);
//                                    createAccountButton.setCompoundDrawablePadding(0);
//                                    createAccountButton.setPadding(padding, 0, padding, 0);    // because of the padding set in the xml
                                    createAccountButton.extend();
                                    createAccountButton.setIcon(null);
                                    enableAll();
                                }));
            };

            userAccountHelper.createAccount(userData, onSuccess, onError);
        }
    }

    @OnClick(R.id.login_floating_button)
    void login(){
        if (userData == null) {
            userData = new UserData();
        }

        userData.setName("o");
        userData.setEmail(userEmailEditText.getEditableText().toString());
        userData.setPassword(userPasswordEditText.getEditableText().toString());

        if (userAccountHelper.validateUserData(userData)) {
            disableAll();
            loginButton.setIcon(getContext().getResources().getDrawable(R.drawable.icon_path_placeholder));
//            loginButton.setIconSize((int) UiUtil.pxFromDp(getContext(), 48));
//            loginButton.setPadding(0, 0, 0, 0);    // because of the padding set in the xml
            loginButton.shrink();
            progressBar.show();

            Consumer<UserData> onSuccess = ud -> disposables.add(
                    Single.just(1)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((s) -> {
                                userData.updateData(ud);
                                signInListener.signIn(userData);
                                progressBar.hide();
                            }));

            Consumer<Throwable> onError = t -> {
                disposables.add(
                        Single.just(1)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((s) -> {
                                    progressBar.hide();
                                    loginButton.extend();
                                    loginButton.setIcon(null);
                                    enableAll();
                                }));
            };

            userAccountHelper.login(userData, onSuccess, onError);
        }
    }

    private void disableAll() {
        loginButton.setEnabled(false);
        createAccountButton.setEnabled(false);
        userImageHintTextView.setEnabled(false);
        userImage.setEnabled(false);
        userNameEditText.setEnabled(false);
        userEmailEditText.setEnabled(false);
        userPasswordEditText.setEnabled(false);
    }

    private void enableAll() {
        loginButton.setEnabled(true);
        createAccountButton.setEnabled(true);
        userImageHintTextView.setEnabled(true);
        userImage.setEnabled(true);
        userNameEditText.setEnabled(true);
        userEmailEditText.setEnabled(true);
        userPasswordEditText.setEnabled(true);
    }

    private class FadeListener extends TransitionListenerAdapter {
        List<View> fadeOutViews = new ArrayList<>();
        List<View> fadeInViews = new ArrayList<>();

        @Override
        public void onTransitionStart(@NonNull Transition transition) {
            for (View v : fadeOutViews) {
                if (v == null) {
                    continue;
                }
                v.setAlpha(1);
                v.animate()
                        .alpha(0)
                        .setDuration(duration)
                        .start();
            }
            for (View v : fadeInViews) {
                if (v == null) {
                    continue;
                }
                v.setAlpha(0);
                v.animate()
                        .alpha(1)
                        .setDuration(duration)
                        .start();
            }
        }

        @Override
        public void onTransitionEnd(@NonNull Transition transition) {
            for (View v : fadeOutViews) {
                if (v == null) {
                    continue;
                }
                v.setVisibility(View.GONE);
            }
        }
    }
}
