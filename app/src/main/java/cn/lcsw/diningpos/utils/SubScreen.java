package cn.lcsw.diningpos.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.util.Log;
import cn.lcsw.diningpos.R;

public class SubScreen {
	private static final String mOledDataFilePath = String
			.format("/dev/sub_lcm");
	private Drawable mOledCallIncoming = null;
	private Paint mOledPaint;
	static final int LCM_WDTH = 480;
	static final int LCM_HEIGTH = 320;
	static final String TAG="zzm";


	@SuppressLint("NewApi")
	public  void show(Bitmap newBitmap) {

		final int iconSize = 24;
		int startX = 0;


		Log.e(TAG,"1+++++++++++++++++++++");
		StringBuffer sBuffer = new StringBuffer();
		int mPixel;
		String sPixel;
		

        char[] chars = "0123456789ABCDEF".toCharArray();  
        int bytes = newBitmap.getByteCount();

        ByteBuffer buf = ByteBuffer.allocate(bytes);
        newBitmap.copyPixelsToBuffer(buf);

        byte[] byteArray = buf.array();
    
        for (int i = 0; i < bytes/2; i++) {  
        	sBuffer.append(chars[(byteArray[2*i+1] & 0xf0) >> 4]);  
             
            sBuffer.append(chars[byteArray[2*i+1] & 0x0f]); 
        	sBuffer.append(chars[(byteArray[2*i] & 0xf0) >> 4]);  
             
            sBuffer.append(chars[byteArray[2*i] & 0x0f]);   
        }  

		Log.e(TAG,bytes+"zzm2+++++++++++++++++++++"+sBuffer.toString());

	
		try {
			String oledDataFilePath = String.format(mOledDataFilePath);
			FileWriter fstream = new FileWriter(oledDataFilePath);
			BufferedWriter out = new BufferedWriter(fstream);
			
			for (int m = 0; m < 80; m++) {

				out.write(sBuffer.toString(), m * 7680, 7680);

				out.flush();
			}

			Log.e(TAG,"3+++++++++++++++++++++");
			out.close();
			fstream.close();
		} catch (Exception e) {
		}

	}

	
	public Drawable GetBg(Context context) {
		
		Drawable bg = context.getResources().getDrawable(R.drawable.sub_wallpaper_white);
		bg.setBounds(0, 0, LCM_WDTH, LCM_HEIGTH);
		return bg;
	}
	

	
	
}

