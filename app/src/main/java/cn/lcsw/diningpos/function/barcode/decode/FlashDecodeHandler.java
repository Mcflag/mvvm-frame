/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.lcsw.diningpos.function.barcode.decode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.lcsw.diningpos.R;
import cn.lcsw.diningpos.ui.flash.FlashActivity;
import cn.lcsw.diningpos.ui.setting.SettingsActivity;
import com.dtr.zbar.build.ZBarDecoder;

final class FlashDecodeHandler extends Handler {

    private static final String TAG = FlashDecodeHandler.class.getSimpleName();

    private final FlashActivity activity;
    private ZBarDecoder zBarDecoder;

    private boolean running = true;

    FlashDecodeHandler(FlashActivity activity) {
        zBarDecoder = new ZBarDecoder();
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        if (message.what == R.id.decode) {
            decode((byte[]) message.obj, message.arg1, message.arg2);

        } else if (message.what == R.id.quit) {
            running = false;
            Looper.myLooper().quit();
        }
    }


    /**
     * 解码
     */

    private void decode(byte[] data, int width, int height) {
       // long start = System.currentTimeMillis();
        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据

        String result =null;
        if (zBarDecoder != null) {
            try {
                result = zBarDecoder.decodeCrop(data, width, height, 0, 0, width, height);

            }catch (Exception ex){
                ex.printStackTrace();
                zBarDecoder=null;
            }
            Handler handler = activity.getHandler();
            if (result != null) {
               // long end = System.currentTimeMillis();
                if (handler != null) {
                    Message message = Message.obtain(handler,
                            R.id.decode_succeeded, result);
                    message.sendToTarget();
                }
            } else {
                if (handler != null) {
                    Message message = Message.obtain(handler, R.id.decode_failed, "1");
                    message.sendToTarget();
                }
            }
        }
    }


}
