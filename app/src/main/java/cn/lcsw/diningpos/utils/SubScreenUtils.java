package cn.lcsw.diningpos.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import cn.lcsw.diningpos.R;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class SubScreenUtils {

    public static void showText(Context context, String s) {

        Bitmap newBitmap = Bitmap.createBitmap(480, 320, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        //canvas.drawColor(0x0000);
        new SubScreen().GetBg(context).draw(canvas);

        Paint mOledPaint = new Paint();
        mOledPaint.setColor(Color.BLACK);
        mOledPaint.setTextSize(50f);
        mOledPaint.setTextAlign(Paint.Align.CENTER);
        RectF rectF=new RectF(0, 0, 480, 320);
        Paint.FontMetrics fontMetrics=mOledPaint.getFontMetrics();
        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        float baseline=rectF.centerY()+distance;
        canvas.drawText(s, rectF.centerX(), baseline, mOledPaint);

        canvas.save();
        canvas.restore();
        new SubScreen().show(newBitmap);
        newBitmap.recycle();
    }

    public static void showBarcodeText(Context context, String s) {

        Bitmap newBitmap = Bitmap.createBitmap(480, 320, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        //canvas.drawColor(0x0000);
        new SubScreen().GetBg(context).draw(canvas);

        Paint mOledPaint = new Paint();
        mOledPaint.setColor(Color.BLACK);
        mOledPaint.setTextSize(50f);
        mOledPaint.setTextAlign(Paint.Align.CENTER);
        RectF rectF=new RectF(0, 0, 480, 320);
        Paint.FontMetrics fontMetrics=mOledPaint.getFontMetrics();
        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        float baseline=rectF.centerY()+distance;
        canvas.drawText("请出示付款码", rectF.centerX(), 100, mOledPaint);
        canvas.drawText(s, rectF.centerX(), baseline, mOledPaint);

        canvas.save();
        canvas.restore();
        new SubScreen().show(newBitmap);
        newBitmap.recycle();
    }

//    public static void oledUpdate320x480(Context context, String string) {
//
//        String mOledDataFilePath = String.format("/dev/sub_lcm");
//        Drawable mOledCallIncoming = context.getResources().getDrawable(R.drawable.sub_wallpaper);
//
//        final int w = 480;
//        final int h = 320;
//        final int iconSize = 24;
//        int startX = 0;
//
//        // create bitmap
//        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(newBitmap);
//        canvas.drawColor(0XFF000000);
//
//        canvas.save();
//        mOledCallIncoming.setBounds(0, 0, 480, 320);
//        mOledCallIncoming.draw(canvas);
//
//        Paint mOledPaint = new Paint();
//        mOledPaint.setColor(Color.WHITE);
//        mOledPaint.setTextSize(50f);
//        mOledPaint.setTextAlign(Paint.Align.CENTER);
//        RectF rectF=new RectF(0, 0, 480, 320);
//        Paint.FontMetrics fontMetrics=mOledPaint.getFontMetrics();
//        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
//        float baseline=rectF.centerY()+distance;
//        canvas.drawText(string, rectF.centerX(), baseline, mOledPaint);
//        canvas.save();
//        canvas.restore();
//        StringBuffer sBuffer = new StringBuffer();
//        int mPixel;
//        String sPixel;
//        for (int j = 0; j < h; j++) {
//            for (int i = 0; i < w; i++) {
//                mPixel = newBitmap.getPixel(i, j);
//                sPixel = RGB888ToRGB565(mPixel);
//                sBuffer.append(sPixel);
//            }
//        }
//
//        newBitmap.recycle();
//
//        // write file for driver
//        try {
//            String oledDataFilePath = String.format(mOledDataFilePath);
//            FileWriter fstream = new FileWriter(oledDataFilePath);
//            BufferedWriter out = new BufferedWriter(fstream);
//
//            for (int m = 0; m < 120; m++) {
//                out.write(sBuffer.toString(), m * 7680, 7680);
//
//                out.flush();
//            }
//            out.close();
//            fstream.close();
//        } catch (Exception e) {
//        }
//
//    }
//
//    public static void oledUpdatePure320x480(Context context, String string) {
//
//        String mOledDataFilePath = String.format("/dev/sub_lcm");
//        Drawable mOledCallIncoming = context.getResources().getDrawable(R.drawable.sub_wallpaper);
//
//        final int w = 480;
//        final int h = 320;
//        final int iconSize = 24;
//        int startX = 0;
//
//        // create bitmap
//        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(newBitmap);
//        canvas.drawColor(0XFFFFFFFF);
//
//        canvas.save();
//        mOledCallIncoming.setBounds(0, 0, 480, 320);
//        mOledCallIncoming.draw(canvas);
//
//        Paint mOledPaint = new Paint();
//        mOledPaint.setColor(Color.BLACK);
//        mOledPaint.setTextSize(50f);
//        mOledPaint.setTextAlign(Paint.Align.CENTER);
//        RectF rectF=new RectF(0, 0, 480, 320);
//        Paint.FontMetrics fontMetrics=mOledPaint.getFontMetrics();
//        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
//        float baseline=rectF.centerY()+distance;
//        canvas.drawText(string, rectF.centerX(), baseline, mOledPaint);
//        canvas.save();
//        canvas.restore();
//        StringBuffer sBuffer = new StringBuffer();
//        int mPixel;
//        String sPixel;
//        for (int j = 0; j < h; j++) {
//            for (int i = 0; i < w; i++) {
//                mPixel = newBitmap.getPixel(i, j);
//                sPixel = RGB888ToRGB565(mPixel);
//                sBuffer.append(sPixel);
//            }
//        }
//
//        newBitmap.recycle();
//
//        // write file for driver
//        try {
//            String oledDataFilePath = String.format(mOledDataFilePath);
//            FileWriter fstream = new FileWriter(oledDataFilePath);
//            BufferedWriter out = new BufferedWriter(fstream);
//
//            for (int m = 0; m < 120; m++) {
//                out.write(sBuffer.toString(), m * 7680, 7680);
//
//                out.flush();
//            }
//            out.close();
//            fstream.close();
//        } catch (Exception e) {
//        }
//
//    }
//
//    public static void oledUpdatePay320x480(Context context, String string) {
//
//        String mOledDataFilePath = String.format("/dev/sub_lcm");
//        Drawable mOledCallIncoming = context.getResources().getDrawable(R.drawable.sub_wallpaper);
//
//        final int w = 480;
//        final int h = 320;
//        final int iconSize = 24;
//        int startX = 0;
//
//        // create bitmap
//        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(newBitmap);
//        canvas.drawColor(0XFFFFFFFF);
//
//        canvas.save();
//        mOledCallIncoming.setBounds(0, 0, 480, 320);
//        mOledCallIncoming.draw(canvas);
//
//        Paint mOledPaint = new Paint();
//        mOledPaint.setColor(Color.BLACK);
//        mOledPaint.setTextSize(50f);
//        mOledPaint.setTextAlign(Paint.Align.CENTER);
//        RectF rectF=new RectF(0, 0, 480, 320);
//        Paint.FontMetrics fontMetrics=mOledPaint.getFontMetrics();
//        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
//        float baseline=rectF.centerY()+distance;
//        canvas.drawText("请出示付款码", rectF.centerX(), 100, mOledPaint);
//        canvas.drawText(string, rectF.centerX(), baseline, mOledPaint);
//        canvas.save();
//        canvas.restore();
//        StringBuffer sBuffer = new StringBuffer();
//        int mPixel;
//        String sPixel;
//        for (int j = 0; j < h; j++) {
//            for (int i = 0; i < w; i++) {
//                mPixel = newBitmap.getPixel(i, j);
//                sPixel = RGB888ToRGB565(mPixel);
//                sBuffer.append(sPixel);
//            }
//        }
//
//        newBitmap.recycle();
//
//        // write file for driver
//        try {
//            String oledDataFilePath = String.format(mOledDataFilePath);
//            FileWriter fstream = new FileWriter(oledDataFilePath);
//            BufferedWriter out = new BufferedWriter(fstream);
//
//            for (int m = 0; m < 120; m++) {
//                out.write(sBuffer.toString(), m * 7680, 7680);
//
//                out.flush();
//            }
//            out.close();
//            fstream.close();
//        } catch (Exception e) {
//        }
//
//    }
//
//    private static String RGB888ToRGB565(int n888Color) {
//        short n565Color = 0;
//        short cRed = (short) ((n888Color & 0x00ff0000) >> 19);
//        short cGreen = (short) ((n888Color & 0x0000ff00) >> 10);
//        short cBlue = (short) ((n888Color & 0x000000ff) >> 3);
//
//        n565Color = (short) ((cRed << 11) + (cGreen << 5) + (cBlue << 0));
//
//        String s = Integer.toHexString((n888Color & 0x00FFFFFF) | 0xFF000000)
//                .substring(2);
//
//        return s;
//    }
}
