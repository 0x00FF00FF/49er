package org.rares.miner49er.ui.fragments.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import butterknife.BindString;
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
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;

import java.util.concurrent.TimeUnit;


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

    @BindView(R.id.tac_text)
    AppCompatTextView tocTextView;

    @BindView(R.id.pp_text)
    AppCompatTextView ppTextView;

    @BindString(R.string.create_user_failed_no_email)
    String errorEmail;

    @BindString(R.string.login_failed_bad_u_p)
    String errorLogin;

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

        userNameEditText.setEnabled(false);

        disposables.add(usersDao.get(12, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(u -> {
                    if (u.isPresent()) {
                        userData = u.get();
                        populateFields(userData);
                    }
                }));
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
//                    .validate(s -> s, this::validateEmail, errorText, errorEmail)
                    .get();
        } catch (FormValidationException e) {
            errorText.setText(errorEmail);
            if (errorText.getAlpha() != 1) {
                errorText.animate().alpha(1).start();
                disposables.add(
                        Single.just("")
                                .delay(2, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(s -> errorText.animate().alpha(0).start()));
            }
            return;
        }

        validator = FormValidator.of(userPassword);
        try {
            validator
                    .validate(s -> s, s -> s.length() > 4, errorText, errorLogin)
                    .get();
        } catch (FormValidationException e) {
            errorText.setText(errorLogin);
            if (errorText.getAlpha() != 1) {
                errorText.animate().alpha(1).start();
                disposables.add(
                        Single.just("")
                                .delay(2, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(s -> errorText.animate().alpha(0).start()));
            }
            return;
        }
/*
        disposables.add(
                usersDao.getMatching(
                        userNameEditText.getEditableText().toString(), Optional.of(null), true)
                        .subscribe(
                                list -> {
                                    if (list.size() == 1) {
                                        userData = list.get(0);
                                        signInListener.signIn(userData);
                                    } else {
                                        errorText.setText(R.string.login_failed_bad_u_p);
                                        if (errorText.getAlpha() != 1) {
                                            errorText.animate().alpha(1).start();
                                            disposables.add(
                                                    Single.just("")
                                                            .delay(2, TimeUnit.SECONDS)
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(s -> errorText.animate().alpha(0).start()));
                                        }
                                    }
                                }
                        ));
*/

        signInListener.signIn(userData);  // // TODO: 05.05.2019 remove once password matching works
    }

    private boolean validateEmail(String email) {
        return !email.contains("@");
    }

    private void populateFields(UserData userData) {
        userNameEditText.setText(userData.getName());
    }
}
