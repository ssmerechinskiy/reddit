package com.sergey.redditreader.ui;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;

public class PermissionHelper {

    private WeakReference<Activity> mWeakActivity;

    private WeakReference<RequestPermissionCallback> mWeakCurrentRequestPermissionCallback;

    private int mRequestCode = -1000;
    private boolean isRequestPermissionInProgress = false;
    private int mRequestedPermissionCount = 0;

    private int mGrantedPermissionCount = 0;
    private int mNotGrantedPermissionCount = 0;

    public PermissionHelper(Activity activity) {
        mWeakActivity = new WeakReference<>(activity);
        mGrantedPermissionCount = 0;
        mNotGrantedPermissionCount = 0;
    }

    public void requestPermission(int requestCode, RequestPermissionCallback callback, String... permissions) {
        if(isRequestPermissionInProgress) return;

        isRequestPermissionInProgress = true;
        mRequestedPermissionCount = permissions.length;
        mRequestCode = requestCode;

        mWeakCurrentRequestPermissionCallback = new WeakReference<>(callback);

        if(mWeakActivity.get() == null || mWeakCurrentRequestPermissionCallback.get() == null) {
            isRequestPermissionInProgress = false;
            mRequestedPermissionCount = 0;
            return;
        }

        mNotGrantedPermissionCount = 0;

        Activity activity = mWeakActivity.get();
        RequestPermissionCallback requestPermissionCallback = mWeakCurrentRequestPermissionCallback.get();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                mNotGrantedPermissionCount++;
            } else {
                mGrantedPermissionCount++;
                requestPermissionCallback.onPermissionGranted(permission, mRequestCode);
            }
        }

        if(mGrantedPermissionCount == mRequestedPermissionCount) {
            notifyComplete(requestCode, requestPermissionCallback);
            return;
        }

        String[] requestedPermissions = new String[mNotGrantedPermissionCount];
        int counter = 0;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                requestedPermissions[counter] = permission;
                counter++;
            }
        }

        if (mNotGrantedPermissionCount != 0) {
            ActivityCompat.requestPermissions(activity, requestedPermissions, mRequestCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        RequestPermissionCallback requestPermissionCallback = mWeakCurrentRequestPermissionCallback.get();
        if(requestPermissionCallback == null) return;
        for (int i = 0, j = 0; i < permissions.length && j < grantResults.length; i++, j++) {
            if (grantResults[j] == PackageManager.PERMISSION_GRANTED) {
                mGrantedPermissionCount ++;
                mNotGrantedPermissionCount --;
                requestPermissionCallback.onPermissionGranted(permissions[i], mRequestCode);
            } else {
                requestPermissionCallback.onPermissionDenied(permissions[i], mRequestCode);
            }
        }
        notifyComplete(requestCode, requestPermissionCallback);
    }

    private void notifyComplete(int requestCode, RequestPermissionCallback callback){
        isRequestPermissionInProgress = false;
        int granted = mGrantedPermissionCount;
        mGrantedPermissionCount = 0;
        callback.onRequestPermissionComplete(requestCode, granted, mRequestedPermissionCount);
    }

    public interface RequestPermissionCallback {
        void onPermissionGranted(String permission, int requestCode);
        void onPermissionDenied(String permission, int requestCode);
        void onRequestPermissionComplete(int requestCode, int grantedCount, int requestedCount);
    }

}
