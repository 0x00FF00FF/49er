package org.rares.miner49er.ui.fragments.login.animated;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.disposables.CompositeDisposable;

public class ImagePickUtil {

    public static final int PICK_IMAGE = 14;

    private Fragment fragment = null;
    private FragmentActivity fragmentActivity = null;
    private RxPermissions rxPermissions;
    private Context context;
    private CompositeDisposable disposables;

    public ImagePickUtil(Fragment fragment, CompositeDisposable disposable) {
        this.fragment = fragment;
        rxPermissions = new RxPermissions(fragment);
        context = fragment.getContext();
        disposables = disposable;
    }

    public ImagePickUtil(FragmentActivity fragmentActivity, CompositeDisposable disposable) {
        this.fragmentActivity = fragmentActivity;
        rxPermissions = new RxPermissions(fragmentActivity);
        context = fragmentActivity;
        disposables = disposable;
    }

    public void start() {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            disposables.add(rxPermissions.request(permission.READ_EXTERNAL_STORAGE/*, permission.WRITE_EXTERNAL_STORAGE*/)
                    .subscribe(granted -> {
                        if (granted) {
                            startImagePickActivity();
                        } else {
                            Toast.makeText(context, "Access denied.", Toast.LENGTH_LONG).show();
                        }
                    }));
        } else {
            startImagePickActivity();
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
        if (fragment != null) {
            fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        } else {
            fragmentActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }
}
