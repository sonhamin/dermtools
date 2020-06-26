package org.techtown.mnist_sample;

import android.graphics.Bitmap;
import android.util.Log;

import java.nio.ByteBuffer;

public class Analysis {
    Bitmap bitmap;
    public static final int IMG_HEIGHT = 304;
    public static final int IMG_WIDTH = 304;
    private static final int NUM_CHANNEL = 1;

    private final ByteBuffer mImageData;
    private final int[] mImagePixels = new int[IMG_HEIGHT * IMG_WIDTH];

    public Analysis(Bitmap bitmap){
        mImageData = ByteBuffer.allocateDirect(
                4 * IMG_HEIGHT * IMG_WIDTH * NUM_CHANNEL);
        this.bitmap = bitmap;
    }

    public void convertBitmapToByteBuffer() {
        if (mImageData == null) {
            return;
        }
        mImageData.rewind();
        Log.d("imageString", bitmap.getWidth() + " and " + bitmap.getHeight());
        bitmap.getPixels(mImagePixels, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < IMG_WIDTH; ++i) {
            for (int j = 0; j < IMG_HEIGHT; ++j) {
                int value = mImagePixels[pixel++];
                mImageData.putFloat(convertPixel(value));
            }
        }
    }

    private static float convertPixel(int color) {
//        return (255-(((color >> 16) & 0xFF) * 0.299f
//                + ((color >> 8) & 0xFF) * 0.587f
//                + (color & 0xFF) * 0.114f)) / 255.0f;
        return ((color & 0xFF)) / 255.0f;
    }

    public final ByteBuffer getmImageData(){
        return mImageData;
    }
}
