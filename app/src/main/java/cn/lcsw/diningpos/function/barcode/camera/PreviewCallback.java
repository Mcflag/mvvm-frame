package cn.lcsw.diningpos.function.barcode.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * desc:二维码解码的回调类
 * Author: znq
 * Date: 2016-11-03 16:24
 */

public class PreviewCallback implements Camera.PreviewCallback {
    private static final String TAG = "PreviewCallback";
    private Handler childHandler;
    private int messageWhat;

    public PreviewCallback() {
    }

    public void setHandler(Handler childHandler, int messageWhat) {
        this.messageWhat = messageWhat;
        this.childHandler = childHandler;
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        Handler theChildHandler = childHandler;
        Camera.Size size = null;
        try {
            size = camera.getParameters().getPreviewSize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (size != null && theChildHandler != null) {
            Message message = theChildHandler.obtainMessage(messageWhat, size.width, size.height, data);
            Log.e("zzm","zzm"+size.width+"height"+size.height+"length"+data.length);
            message.sendToTarget();
            childHandler = null;
        }

    }


}
