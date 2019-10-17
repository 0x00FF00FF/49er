package org.rares.miner49er.ui.fragments.login.animated;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;
import com.pushtorefresh.storio3.Optional;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.persistence.UserSpecificDao;
import org.rares.miner49er.network.rest.auth.NetworkUserAccountService;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.custom.validation.FormValidationException;
import org.rares.miner49er.ui.custom.validation.FormValidator;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Helper class that separates View logic
 * (e.g. fragment events, user input etc.)
 * from model manipulation (e.g. validation,
 * model action) and provides reactions to
 * the viewModel changes.
 */
class UserAccountHelper {

  private final String TAG = UserAccountHelper.class.getSimpleName();

  private AppCompatTextView errorMessageView;
  private CompositeDisposable disposables;
  private AsyncGenericDao<UserData> usersDao;

  private void setAndShowErrorMessage(@StringRes int errorMessageRes) {
    errorMessageView.setText(errorMessageRes);
    showErrorMessage();
  }

  private void setAndShowErrorMessage(String errorMessage) {
    errorMessageView.setText(errorMessage);
    showErrorMessage();
  }

  private void showErrorMessage() {
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
        setAndShowErrorMessage(error);
        return false;
      }
      error = exceptions.get("email");
      if (error != null) {
        setAndShowErrorMessage(error);
        return false;
      }
      error = exceptions.get("password");
      if (error != null) {
        setAndShowErrorMessage(error);
        return false;
      }
    }
    return true;
  }

  void createAccount(
      @NonNull final UserData userData,
      final Consumer<Long> onSuccess,
      final Consumer<Throwable> onError) {

    disposables.add(
        ((UserSpecificDao) usersDao).getByEmail(userData.getEmail())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(userOptional -> {
              if (!userOptional.isPresent()) {

                disposables.add(
                    NetworkUserAccountService.createAccount(userData, usersDao)
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((insertedUser, throwable) -> {
                              if (throwable != null) {
                                Log.e(TAG, "createAccount: failed :(", throwable);
                                onError.accept(throwable);
                                if (throwable instanceof ConnectException) {
                                  // set message to something like:
                                  // "Cannot connect to service.
                                  // Please try again later if
                                  // the internet connection is
                                  // working, but the server is
                                  // not responding."
                                  setAndShowErrorMessage(R.string.create_user_failed_no_internet);
                                } else {
                                  if (throwable.getMessage() != null) {
                                    setAndShowErrorMessage(throwable.getMessage());
                                  } else {
                                    setAndShowErrorMessage(R.string.err_operation_not_completed_try_again);
                                  }
                                }
                              } else {
                                long insertId = insertedUser.id;
                                if (insertId > 0) {
                                  if (errorMessageView.getAlpha() != 0) {
                                    errorMessageView.setAlpha(0);
                                  }
                                } else {    //
                                  setAndShowErrorMessage(R.string.err_operation_not_completed_try_again);
                                }
                                login(insertedUser,
                                    (success) -> {
                                      onSuccess.accept(insertId);
                                    },
                                    onError);
                              }
                            }
                        ));
              } else {
                disposables.add(Single.just(1)
                    .delay(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(e -> {
                      setAndShowErrorMessage(R.string.create_user_failed_user_exists);
                      onError.accept(null);
                    }));
              }
            }));
  }

  void login(@NonNull final UserData userData, final Consumer<UserData> onSuccess, final Consumer<Throwable> onError) {
    final int SESSION_TIMEOUT = 10 * 1000; // in seconds
    disposables.add(
        ((UserSpecificDao) usersDao).getByEmail(userData.getEmail())
            .subscribeOn(Schedulers.io())
            .flatMap(opt -> {
              //noinspection ConstantConditions
              if (!opt.isPresent()
                  || opt.get().getApiKey() == null
                  || opt.get().getApiKey().equals("")
                  || (opt.get().lastUpdated + SESSION_TIMEOUT < System.currentTimeMillis() && /*online*/false)) {

                return NetworkUserAccountService.login(userData, usersDao)
                    .map(Optional::of);
              }
              Log.i(TAG, "login: user exists in the local db.");
              return Single.just(opt);
            })
            .doOnError(Throwable::printStackTrace)
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe((opt, throwable) -> {
                  if (opt != null && opt.isPresent() && userData.getPassword().equals(opt.get().getPassword())) {
                    onSuccess.accept(opt.get());
                  } else {
                    setAndShowErrorMessage(R.string.login_failed_bad_u_p);
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
