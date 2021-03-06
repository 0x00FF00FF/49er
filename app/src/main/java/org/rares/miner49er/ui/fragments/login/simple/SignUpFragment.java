package org.rares.miner49er.ui.fragments.login.simple;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import butterknife.BindFont;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.tbruyelle.rxpermissions2.RxPermissions;
import de.hdodenhof.circleimageview.CircleImageView;
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
import org.rares.miner49er.util.FileUtils;
import org.rares.miner49er.util.TextUtils;
import org.rares.miner49er.util.UiUtil;

import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;

@Deprecated
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

    @BindView(R.id.progress_circular)
    ContentLoadingProgressBar progressBar;

    @BindFont(R.font.futura_book_bt)
    Typeface passwordTypeface;

    private int PICK_IMAGE = 14;

    private Unbinder unbinder;
    private UserData userData;
    private AsyncGenericDao<UserData> usersDao;
    private SignUpListener signUpListener;
    private CompositeDisposable disposables;
    private RxPermissions rxPermissions;

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

        userData = new UserData();
        usersDao = InMemoryCacheAdapterFactory.ofType(UserData.class);
        rxPermissions = new RxPermissions(this);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                TextUtils.hideKeyboardFrom(v);
                createAccount();
            }
            return false;
        });
        userPasswordEditText.setOnTouchListener(passwordPeekListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.dispose();
        unbinder.unbind();
        disposables = null;
        unbinder = null;
    }

    @OnClick({R.id.user_image_hint_text, R.id.user_image})
    void addImage() {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            disposables.add(rxPermissions.request(permission.READ_EXTERNAL_STORAGE/*, permission.WRITE_EXTERNAL_STORAGE*/)
                    .subscribe(granted -> {
                        if (granted) {
                            startImagePickActivity();
                        } else {
                            Toast.makeText(getContext(), "Access denied.", Toast.LENGTH_LONG).show();
                        }
                    }));
        } else {
            startImagePickActivity();
        }
    }

    @OnClick(R.id.create_account_btn)
    void createAccount() {
        String userName = userNameEditText.getEditableText().toString();
        String userEmail = userEmailEditText.getEditableText().toString();
        String userPassword = userPasswordEditText.getEditableText().toString();
        FormValidator<String> validator = FormValidator.of(userName);
        try {
            validator
                    .validate(s -> s, name -> !name.isEmpty(), null, null)
                    .get();
        } catch (FormValidationException e) {
            showErrorMessage(R.string.create_user_failed_no_user_name);
            return;
        }

        validator = FormValidator.of(userEmail);
        try {
            validator
                    .validate(s -> s, s -> s.matches(".+@.+"), null, null)
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
            showErrorMessage(R.string.create_user_failed_no_password);
            return;
        }

        disposables.add(
                ((UserSpecificDao) usersDao).getByEmail(userEmailEditText.getEditableText().toString())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(userOptional -> {
                            if (!userOptional.isPresent()) {
                                if (userData == null) {
                                    userData = new UserData();
                                }
                                userData.setName(userName);
                                userData.setEmail(userEmail);
                                userData.setActive(true);
                                userData.setObjectId("0000-0000-0A0B0C0D0E0F0F0F");
                                disposables.add(
                                        // TODO: service call
                                        usersDao.insert(userData)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(insertId -> {
                                                            if (insertId > 0) {
                                                                userData.id = insertId;
                                                                if (errorText.getAlpha() != 0) {
                                                                    errorText.setAlpha(0);
                                                                }
                                                                disableAll();
                                                                createAccountButton.setIcon(getContext().getResources().getDrawable(R.drawable.icon_path_placeholder));
                                                                createAccountButton.setIconSize((int) UiUtil.pxFromDp(getContext(), 48));
                                                                createAccountButton.setPadding(0,0,0,0);    // because of the padding set in the xml
                                                                createAccountButton.shrink();
                                                                progressBar.show();
                                                                disposables.add(
                                                                        Single.just(1)
                                                                                .delay(2, TimeUnit.SECONDS)
                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                .subscribe((s) -> {
                                                                                    signUpListener.signUp(userData);
                                                                                    progressBar.hide();
                                                                                })
                                                                );
                                                            } else {
                                                                showErrorMessage(R.string.err_operation_not_completed_try_again);
                                                            }
                                                        }
                                                ));
                            } else {
                                showErrorMessage(R.string.create_user_failed_user_exists);
                            }
                        }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Context context = getContext();
            String filePath = FileUtils.getPath(context, data.getData());
            userData.setPicture(filePath);
            userImageHintTextView.setVisibility(View.GONE);
            if (context != null) {
                Glide.with(context)
                        .load(filePath)
                        .into(userImage);
            }
        }
    }

    private void startImagePickActivity() {
        // newer api (the user can choose between installed gallery apps)
//                            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                            getIntent.setType("image/*");
//
//                            Intent pickIntent = new Intent(Intent.ACTION_PICK);
//                            pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//
//                            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
//                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
//
//                            startActivityForResult(chooserIntent, PICK_IMAGE);

        // faster (smaller selection of apps, may skip gallery selection app screen)
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

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
        userEmailEditText.setEnabled(false);
        userNameEditText.setEnabled(false);
        userPasswordEditText.setEnabled(false);
        createAccountButton.setEnabled(false);
    }

    private void enableAll() {
        userEmailEditText.setEnabled(true);
        userNameEditText.setEnabled(true);
        userPasswordEditText.setEnabled(true);
//        createAccountButton.setEnabled(true);
    }

    private PasswordTransformationMethod passwordOn = new PasswordTransformationMethod();
    private OnTouchListener passwordPeekListener = new OnTouchListener() {
        private int selectionStart;
        private int selectionEnd;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!v.equals(userPasswordEditText)) {
                return false;
            }
            int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getRawX() >= (
                        userPasswordEditText.getRight() - userPasswordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    userPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                    userPasswordEditText.setTypeface(passwordTypeface);
                    userPasswordEditText.setTransformationMethod(null);
                    selectionStart = userPasswordEditText.getSelectionStart();
                    selectionEnd = userPasswordEditText.getSelectionEnd();
                    return true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (
                        userPasswordEditText.getRight() - userPasswordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    userPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    userPasswordEditText.setTransformationMethod(passwordOn);
                    if (selectionStart == selectionEnd && selectionStart == 0) {
                        selectionStart = userPasswordEditText.getEditableText().length();
                        selectionEnd = selectionStart;
                    }
                    userPasswordEditText.setSelection(selectionStart, selectionEnd);
                    return true;
                }
            }
            return false;
        }
    };

}
