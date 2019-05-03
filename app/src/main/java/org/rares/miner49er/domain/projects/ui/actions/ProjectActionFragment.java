package org.rares.miner49er.domain.projects.ui.actions;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tbruyelle.rxpermissions2.RxPermissions;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.CompositeDisposable;
import org.rares.miner49er.R;
import org.rares.miner49er.cache.cacheadapter.InMemoryCacheAdapterFactory;
import org.rares.miner49er.domain.projects.model.ProjectData;
import org.rares.miner49er.domain.users.model.UserData;
import org.rares.miner49er.domain.users.userlist.UserInterfaces;
import org.rares.miner49er.domain.users.userlist.UserListFragmentEdit;
import org.rares.miner49er.domain.users.userlist.UserListFragmentPureRv;
import org.rares.miner49er.persistence.dao.AsyncGenericDao;
import org.rares.miner49er.ui.actionmode.ActionFragment;
import org.rares.miner49er.ui.actionmode.ToolbarActionManager;
import org.rares.miner49er.util.FileUtils;
import org.rares.miner49er.util.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.text.InputType.TYPE_NULL;
import static org.rares.miner49er.BaseInterfaces.UTFEnc;

public abstract class ProjectActionFragment
        extends ActionFragment
        implements UserInterfaces.SelectedUsersListConsumer {

    public static final String TAG = ProjectActionFragment.class.getSimpleName();

    @BindView(R.id.content_container)
    protected ConstraintLayout container;

    @BindView(R.id.project_name_input_layout)
    protected TextInputLayout inputLayoutProjectName;
    @BindView(R.id.project_name_input_layout_edit)
    protected TextInputEditText editTextProjectName;
    @BindView(R.id.project_short_name_input_layout)
    protected TextInputLayout inputLayoutProjectShortName;
    @BindView(R.id.project_short_name_input_layout_edit)
    protected TextInputEditText editTextProjectShortName;
    @BindView(R.id.project_description_input_layout)
    protected TextInputLayout inputLayoutProjectDescription;
    @BindView(R.id.project_description_input_layout_edit)
    protected TextInputEditText editTextProjectDescription;
    @BindView(R.id.project_icon_input_layout)
    protected TextInputLayout inputLayoutProjectIcon;
    @BindView(R.id.project_icon_input_layout_edit)
    protected TextInputEditText editTextProjectIcon;
    @BindView(R.id.project_owner_input_layout)
    protected TextInputLayout inputLayoutProjectOwner;
    @BindView(R.id.project_owner_input_layout_edit)
    protected TextInputEditText editTextProjectOwner;

    protected String TAG_NAME = String.valueOf(R.id.project_name_input_layout_edit);
    protected String TAG_SHORT_NAME = String.valueOf(R.id.project_short_name_input_layout_edit);
    protected String TAG_DESCRIPTION = String.valueOf(R.id.project_description_input_layout_edit);
    protected String TAG_ICON = String.valueOf(R.id.project_icon_input_layout_edit);
    protected String TAG_OWNER = String.valueOf(R.id.project_owner_input_layout_edit);

    @BindView(R.id.project_icon_image)
    protected CircleImageView projectIconImage;

    @BindView(R.id.btn_add_project)
    protected MaterialButton btnApply;
    @BindView(R.id.btn_cancel_add_project)
    protected MaterialButton btnCancel;

    @BindView(R.id.btn_edit_users)
    protected AppCompatImageView btnEditUsers;

    protected UserListFragmentPureRv userListFragment;
    protected UserListFragmentEdit userListFragmentEdit;

    protected AsyncGenericDao<ProjectData> projectsDAO;
    protected AsyncGenericDao<UserData> usersDAO;

    protected ProjectData projectData = null;
    protected List<UserData> team;

    private int PICK_IMAGE = 14;

    private RxPermissions rxPermissions;
    private CompositeDisposable disposables;

    protected View createView(LayoutInflater inflater, ViewGroup container) {

        rootView = (ScrollView) inflater.inflate(R.layout.fragment_project_edit, container, false);
        setReplacedView(container.findViewById(R.id.scroll_views_container));        //

        userListFragment = (UserListFragmentPureRv) getChildFragmentManager().findFragmentById(R.id.users_rv);

        unbinder = ButterKnife.bind(this, rootView);
        prepareEntry();
        rootView.setSmoothScrollingEnabled(true);

        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        };

        InputFilter[] filters = new InputFilter[]{filter};

        editTextProjectOwner.setFilters(filters);
        editTextProjectName.setFilters(filters);
        editTextProjectShortName.setFilters(filters);
        editTextProjectDescription.setFilters(filters);
        editTextProjectIcon.setFilters(filters);

        editTextProjectIcon.setInputType(TYPE_NULL);
        editTextProjectIcon.setOnTouchListener(selectIconOnTouch);

        editTextProjectIcon.setOnFocusChangeListener((view, focused) -> {
            if (focused) {
                TextUtils.hideKeyboardFrom(editTextProjectIcon);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        projectsDAO = InMemoryCacheAdapterFactory.ofType(ProjectData.class);
        usersDAO = InMemoryCacheAdapterFactory.ofType(UserData.class);
        disposables = new CompositeDisposable();
        rxPermissions = new RxPermissions(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.dispose();
        unbinder.unbind();
        editTextProjectOwner = null;
        editTextProjectName = null;
        editTextProjectShortName = null;
        editTextProjectDescription = null;
        editTextProjectIcon = null;
        container = null;
        inputLayoutProjectName = null;
        inputLayoutProjectShortName = null;
        inputLayoutProjectDescription = null;
        inputLayoutProjectIcon = null;
        inputLayoutProjectOwner = null;
        selectIconOnTouch = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        rootView.smoothScrollTo(0, 0);
    }

    @Override
    public void prepareExit() {

        Context context = getContext();
        if (context != null) {
            TextUtils.hideKeyboardFrom(rootView.findFocus());
        }

        actionFragmentTransition.prepareExitAnimation(getView(), replacedView);
        resultListener.onFragmentDismiss();
        FragmentManager fm  = getFragmentManager();
        if (fm != null) {
            fm.beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void prepareEntry() {
        actionFragmentTransition.prepareEntryAnimation(replacedView);
    }

    @OnClick(R.id.btn_cancel_add_project)
    public void cancelAction() {
        prepareExit();
    }


    protected void updateProjectData() {
        projectData.setName(editTextProjectName.getEditableText().toString());
        projectData.setDescription(editTextProjectDescription.getEditableText().toString());
        String iconUrl = editTextProjectIcon.getEditableText().toString();
        try {
            iconUrl = URLEncoder.encode(editTextProjectIcon.getEditableText().toString(), UTFEnc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        projectData.setIcon(iconUrl);
        projectData.setPicture(iconUrl);
        projectData.setTeam(userListFragment.getUsers());
    }

    protected void resetFields() {
        rootView.smoothScrollTo(0, 0);
        clearErrors();
        editTextProjectName.setText("");
        editTextProjectShortName.setText("");
        editTextProjectDescription.setText("");
        editTextProjectIcon.setText("");
        editTextProjectOwner.setText("");
    }

    protected void clearErrors() {
        inputLayoutProjectName.setError("");
        inputLayoutProjectShortName.setError("");
        inputLayoutProjectDescription.setError("");
        inputLayoutProjectIcon.setError("");
        inputLayoutProjectOwner.setError("");
    }

    @OnLongClick(R.id.user_list_container)
    @OnClick(R.id.btn_edit_users)
    public void showUsersEditFragment() {
        btnEditUsers.setEnabled(false);
        List<UserData> users = userListFragment.getUsers();
        long[] ids = getUsersIds(users);
        if (userListFragmentEdit == null) {
            userListFragmentEdit = UserListFragmentEdit.newInstance(ids, 0);
        } else {
            Bundle args = new Bundle();
            args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, ids);
            userListFragmentEdit.setArguments(args);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        userListFragmentEdit.show(fragmentManager, UserListFragmentEdit.TAG);
    }

    private OnTouchListener selectIconOnTouch = (v, event) -> {
//        final int DRAWABLE_LEFT = 0;
//        final int DRAWABLE_TOP = 1;
//        final int DRAWABLE_BOTTOM = 3;
        final int DRAWABLE_RIGHT = 2;
        v.performClick();   // gaah!
        AppCompatEditText editText;
        if (v instanceof AppCompatEditText) {
            editText = (AppCompatEditText) v;
            if (event.getAction() == MotionEvent.ACTION_UP) {

                PopupMenu popup = new PopupMenu(getContext(), v);

                popup.getMenuInflater()
                        .inflate(R.menu.context_menu_picture, popup.getMenu());
                ToolbarActionManager.addIconToMenuItem(getContext(), popup.getMenu(), R.id.upload_local, R.drawable.icon_path_cloud_upload, 0, 0);
                ToolbarActionManager.addIconToMenuItem(getContext(), popup.getMenu(), R.id.browse_existing, R.drawable.icon_path_cloud_download, 0, 0);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Log.d(TAG, "onMenuItemClick() called with: item = [" + item + "]");
                        if (R.id.upload_local == item.getItemId()) {
                            if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
                                disposables.add(rxPermissions.request(permission.READ_EXTERNAL_STORAGE/*, permission.WRITE_EXTERNAL_STORAGE*/)
                                        .subscribe(granted -> {
                                            if (granted) {
                                                startActivity();
                                            } else {
                                                Toast.makeText(getContext(), "Access denied.", Toast.LENGTH_LONG).show();
                                            }
                                        }));
                            } else {
                                startActivity();
                            }
                        }
                        return true;
                    }

                    private void startActivity() {
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
                });

                popup.setGravity(GravityCompat.END);

                popup.show();
            }
        }
        return false;
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(TAG, "onActivityResult: ");
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Context context = getContext();
            String filePath = FileUtils.getPath(context, data.getData());
            editTextProjectIcon.setText(filePath);
            if (context != null) {
                Glide.with(context)
                        .load(filePath)
                        .into(projectIconImage);
            }
        }
    }

    protected long[] getUsersIds(List<UserData> users) {
        if (users == null) {
            return new long[0];
        }
        long[] ids = new long[users.size()];
        for (int i = 0; i < users.size(); i++) {
            ids[i] = users.get(i).id;
        }
        return ids;
    }

    @Override
    public void UsersListFragmentClosed(String tag) {
        if (tag != null && tag.equals(UserListFragmentEdit.TAG)) {
            if (btnEditUsers != null) {
                btnEditUsers.setEnabled(true);
            }
            userListFragmentEdit = null;
        }
    }

    @Override
    public void setSelectedList(long[] selectedUsersList) {
        if (team == null) {
            team = new ArrayList<>();
        }
        team.clear();
        for (long userId : selectedUsersList) {
            team.add(usersDAO.get(userId, true).blockingGet().get());
        }
        Bundle args = userListFragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putLongArray(UserInterfaces.KEY_SELECTED_USERS, selectedUsersList);
        userListFragment.setArguments(args);
        userListFragment.refreshData();
    }
}
