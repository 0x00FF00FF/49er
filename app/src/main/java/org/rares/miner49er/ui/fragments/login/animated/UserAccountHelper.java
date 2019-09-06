package org.rares.miner49er.ui.fragments.login.animated;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.UserSpecificDao;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserAccountHelper {

    private final String TAG = UserAccountHelper.class.getSimpleName();

    private AppCompatTextView errorMessageView;
    private CompositeDisposable disposables;
    private AsyncGenericDao<UserData> usersDao;

    private void showErrorMessage(@StringRes int errorMessageRes) {
        errorMessageView.setText(errorMessageRes);
        if (errorMessageView.getAlpha() != 1) {
            errorMessageView.animate().alpha(1).start();
            disposables.add(
                    Single.just("")
                            .delay(2, TimeUnit.SECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s -> errorMessageView.animate().alpha(0).start()));
        }
    }

    public boolean validateUserData(@NonNull final UserData userData) {
        FormValidator<UserData> userValidator = FormValidator.of(userData);
        try {
            userValidator
                    .validate(UserData::getName, n -> n != null && !n.isEmpty(), "name", R.string.create_user_failed_no_user_name)
                    .validate(UserData::getEmail, e -> e.matches(".+@.+"), "email", R.string.create_user_failed_no_email)
                    .validate(UserData::getPassword, p -> p != null && !p.isEmpty() && p.length() > 4, "password", R.string.create_user_failed_no_password)
                    .get();
        } catch (FormValidationException e) {
            Map<Object, Integer> exceptions = e.getInvalidFieldsInt();
            Integer error = exceptions.get("name");
            if (error != null) {
                showErrorMessage(error);
                return false;
            }
            error = exceptions.get("email");
            if (error != null) {
                showErrorMessage(error);
                return false;
            }
            error = exceptions.get("password");
            if (error != null) {
                showErrorMessage(error);
                return false;
            }
        }
        return true;
    }

    public void createAccount(
            @NonNull final UserData userData,
            final Consumer<Long> onSuccess,
            final Consumer<Throwable> onError) {
        disposables.add(
                ((UserSpecificDao) usersDao).getByEmail(userData.getEmail())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userOptional -> {
                            if (!userOptional.isPresent()) {
                                disposables.add(
                                        // TODO: service call
                                        usersDao.insert(userData)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .delay(1, TimeUnit.SECONDS)
                                                .subscribe((insertId, throwable) -> {
                                                            if (throwable != null) {
                                                                onError.accept(throwable);
                                                                showErrorMessage(R.string.err_operation_not_completed_try_again);
                                                            } else {
                                                                if (insertId > 0) {
//                                                                        userData.id = insertId;
                                                                    if (errorMessageView.getAlpha() != 0) {
                                                                        errorMessageView.setAlpha(0);
                                                                    }
                                                                } else {    //
                                                                    showErrorMessage(R.string.err_operation_not_completed_try_again);
                                                                }
                                                                onSuccess.accept(insertId);
                                                            }
                                                        }
                                                ));
                            } else {
                                disposables.add(Single.just(1)
                                        .delay(1, TimeUnit.SECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(e -> {
                                            showErrorMessage(R.string.create_user_failed_user_exists);
                                            onError.accept(null);
                                        }));
                            }
                        }));
    }

    public void login(@NonNull final UserData userData, final Consumer<UserData> onSuccess, final Consumer<Throwable> onError) {
        disposables.add(
                ((UserSpecificDao) usersDao).getByEmail(userData.getEmail())
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((opt, throwable) -> {
                                    if (opt.isPresent()) {
                                        onSuccess.accept(opt.get());
                                    } else {
                                        showErrorMessage(R.string.login_failed_bad_u_p);
                                        onError.accept(throwable);
                                    }
                                }
                        ));
    }

    public UserAccountHelper(AppCompatTextView errorMessageView, CompositeDisposable disposables) {
        this.errorMessageView = errorMessageView;
        this.disposables = disposables;
        usersDao = InMemoryCacheAdapterFactory.ofType(UserData.class);
    }
}
