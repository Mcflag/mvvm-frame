package cn.lcsw.mvvm.base.view.activity

import android.Manifest
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import permissions.dispatcher.*

@RuntimePermissions
abstract class PermissionActivity : AppCompatActivity() {

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @OnPermissionDenied(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    )
    fun onDenied() {
    }

    @OnNeverAskAgain(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    )
    fun onNeverAskAgain() {
    }

    fun requestPhoneState() {
        showPhoneStateWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    open fun showPhoneState() {
    }

    @OnShowRationale(Manifest.permission.READ_PHONE_STATE)
    fun showPhoneStateRationale(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setTitle("请求读取设备序列号，开启以正常使用功能")
            .setPositiveButton("确定") { dialog, which -> request.proceed() }
            .setNegativeButton("取消") { dialog, which -> request.cancel() }
            .show()
    }

    fun requestCamera() {
        showCameraWithPermissionCheck()
    }

    @OnPermissionDenied(Manifest.permission.READ_PHONE_STATE)
    open fun onPhoneDenied() {
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    open fun showCamera() {
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showCameraRationale(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setTitle("开启摄像头权限，以正常使用功能")
            .setPositiveButton("确定") { dialog, which -> request.proceed() }
            .setNegativeButton("取消") { dialog, which -> request.cancel() }
            .show()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    open fun onCameraDenied() {
    }

    fun requestStorage() {
        writeStorageWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    open fun writeStorage() {
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    open fun writeStorageRationale(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setTitle("开启读写权限，以正常使用功能")
            .setPositiveButton("确定") { dialog, which -> request.proceed() }
            .setNegativeButton("取消") { dialog, which -> request.cancel() }
            .show()
    }

    fun requestLocation() {
        getLocationWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun getLocation() {
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun showLocationRationale(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setTitle("应用需要获取位置信息，以正常使用")
            .setPositiveButton("确定") { dialog, which -> request.proceed() }
            .setNegativeButton("取消") { dialog, which -> request.cancel() }
            .show()
    }

}