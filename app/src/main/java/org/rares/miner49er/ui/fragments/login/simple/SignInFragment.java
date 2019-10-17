package org.rares.miner49er.ui.fragments.login.simple;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.UserSpecificDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.concurrent.TimeUnit;


@Deprecated
public class SignInFragment extends Fragment {

    public static final String TAG = SignInFragment.class.getSimpleName();

    @BindView(R.id.app_logo_image)
    AppCompatImageView appLogo;

    @BindView(R.id.user_name_edit_text)
    AppCompatEditText userNameEditText;

    @BindView(R.id.user_password_edit_text)
    AppCompatEditText userPasswordEditText;

    @BindView(R.id.login_floating_button)
    ExtendedFloatingActionButton loginButton;

    @BindView(R.id.error_text)
    AppCompatTextView errorText;

    @BindView(R.id.progress_circular)
    ContentLoadingProgressBar progressBar;

    private UserData userData;
    private AsyncGenericDao<UserData> usersDao;

    private Unbinder unbinder;
    private SignInListener signInListener;

    private CompositeDisposable disposables;

    public static SignInFragment newInstance() {
        Bundle args = new Bundle();

        SignInFragment fragment = new SignInFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SignInFragment() {
    }

    public interface SignInListener {
        void signIn(UserData userData);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            signInListener = (SignInListener) getParentFragment();
        } else {
            signInListener = (SignInListener) context;
        }

        usersDao = InMemoryCacheAdapterFactory.ofType(UserData.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        disposables = new CompositeDisposable();
        View rootView = inflater.inflate(R.layout.layout_sign_in, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                TextUtils.hideKeyboardFrom(v);
                login();
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.dispose();
        disposables = null;
        unbinder.unbind();
        unbinder = null;
    }

    @OnClick(R.id.login_floating_button)
    void login() {
        String userName = userNameEditText.getEditableText().toString();
        String userPassword = userPasswordEditText.getEditableText().toString();
        FormValidator<String> validator = FormValidator.of(userName);
        try {
            validator
                    .validate(s -> s, s -> !s.isEmpty(), null, null)
                    .get();
        } catch (FormValidationException e) {
            showErrorMessage(R.string.create_user_failed_no_email);
            return;
        }

        validator = FormValidator.of(userPassword);
        try {
            validator
                    .validate(s -> s, s -> s.length() > 4, null, null)
                    .get();
        } catch (FormValidationException e) {
            showErrorMessage(R.string.login_failed_bad_u_p);
            return;
        }
        disposables.add(
                ((UserSpecificDao)usersDao).getByEmail(userNameEditText.getEditableText().toString())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                opt -> {
                                    if (opt.isPresent()) {
                                        userData = opt.get();
                                        disableAll();
                                        loginButton.setIcon(getContext().getResources().getDrawable(R.drawable.icon_path_placeholder));
                                        loginButton.setIconSize((int) UiUtil.pxFromDp(getContext(), 48));
                                        loginButton.shrink();
                                        progressBar.show();
                                        disposables.add(
                                                Single.just(1)
                                                        .delay(2, TimeUnit.SECONDS)
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe((s) -> {
                                                            signInListener.signIn(userData);
                                                            progressBar.hide();
                                                        })
                                        );
                                    } else {
                                        showErrorMessage(R.string.login_failed_bad_u_p);
                                    }
                                }
                        ));
    }

//    private void populateFields(UserData userData) {
//        userNameEditText.setText(userData.getName());
//    }

    private void showErrorMessage(@StringRes int errorMessageRes) {
        errorText.setText(errorMessageRes);
        if (errorText.getAlpha() != 1) {
            errorText.animate().alpha(1).start();
            disposables.add(
                    Single.just("")
                            .delay(2, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s -> errorText.animate().alpha(0).start()));
        }
    }

    private void disableAll() {
        userNameEditText.setEnabled(false);
        userPasswordEditText.setEnabled(false);
        loginButton.setEnabled(false);
    }

}
