package org.rares.miner49er.ui.fragments.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.pushtorefresh.storio3.Optional;
import de.hdodenhof.circleimageview.CircleImageView;
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

public class SignUpFragment extends Fragment {

    public static final String TAG = SignUpFragment.class.getSimpleName();

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

    @BindView(R.id.error_text)
    AppCompatTextView errorText;

    @BindView(R.id.tac_text)
    AppCompatTextView tocTextView;

    @BindView(R.id.pp_text)
    AppCompatTextView ppTextView;

    @BindString(R.string.create_user_failed_user_exists)
    String errorUserExists;
    @BindString(R.string.create_user_failed_no_password)
    String errorNoPassword;
    @BindString(R.string.create_user_failed_no_user_name)
    String errorNoUserName;
    @BindString(R.string.create_user_failed_no_email)
    String errorNoEmail;

    private Unbinder unbinder;
    private UserData userData;
    private AsyncGenericDao<UserData> usersDao;
    private SignUpListener signUpListener;
    private CompositeDisposable disposables;

    public static SignUpFragment newInstance() {
        Bundle args = new Bundle();

        SignUpFragment fragment = new SignUpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SignUpFragment() {
    }

    public interface SignUpListener {
        void signUp(UserData userData);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            signUpListener = (SignUpListener) getParentFragment();
        } else {
            signUpListener = (SignUpListener) context;
        }

        usersDao = InMemoryCacheAdapterFactory.ofType(UserData.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        disposables = new CompositeDisposable();
        View rootView = inflater.inflate(R.layout.layout_sign_up, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.dispose();
        unbinder.unbind();
        disposables = null;
        unbinder = null;
    }

    @OnClick(R.id.create_account_btn)
    void createAccount() {
        String userName = userNameEditText.getEditableText().toString();
        String userEmail = userEmailEditText.getEditableText().toString();
        String userPassword = userPasswordEditText.getEditableText().toString();
        FormValidator<String> validator = FormValidator.of(userName);
        try {
            validator
                    .validate(s -> s, name -> !name.isEmpty(), errorText, errorNoUserName)
                    .get();
        } catch (FormValidationException e) {
            errorText.setText(errorNoUserName);
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

        validator = FormValidator.of(userEmail);
        try {
            validator
                    .validate(s -> s, s -> s.contains("@"), errorText, errorNoPassword)
                    .get();
        } catch (FormValidationException e) {
            errorText.setText(errorNoEmail);
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
                    .validate(s -> s, s -> s.length() > 4, errorText, errorNoPassword)
                    .get();
        } catch (FormValidationException e) {
            errorText.setText(errorNoPassword);
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

        disposables.add(
                usersDao.getMatching(userEmailEditText.getEditableText().toString(), Optional.of(null), true)
                        .subscribe(users -> {
                            if (users.size() == 0) {
                                disposables.add(
                                        usersDao.insert(userData)
                                                .subscribe(insertId -> {
                                                            if (insertId > 0) {
                                                                userData.id = insertId;
                                                                signUpListener.signUp(userData);
                                                                if (errorText.getAlpha() != 0) {
                                                                    errorText.setAlpha(0);
                                                                }
                                                            } else {
                                                                errorText.setText(R.string.err_operation_not_completed_try_again);
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
                            } else {
                                errorText.setText(R.string.create_user_failed_user_exists);
                                if (errorText.getAlpha() != 1) {
                                    errorText.animate().alpha(1).start();
                                    disposables.add(
                                            Single.just("")
                                                    .delay(2, TimeUnit.SECONDS)
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(s -> errorText.animate().alpha(0).start()));
                                }
                            }
                        }));
    }
}
