package org.techtown.mnist_sample;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Preprocessor {
    public Preprocessor()
    {

    }


    public Bitmap[] decompose(Bitmap croppedBitmap) {
        int[] coverImageIntArray1D = new int[304 * 304];
        Bitmap resizedBitmap = null;
        resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 304, 304, true);

        resizedBitmap.getPixels(coverImageIntArray1D, 0, 304, 0, 0, 304, 304);

        float[][][] input = new float[304][304][3];

        for (int i = 0; i < 304; i++) {
            for (int j = 0; j < 304; j++) {
                int rgb = coverImageIntArray1D[i * 304 + j];
                int R = Color.red(rgb);
                int G = Color.green(rgb);
                int B = Color.blue(rgb);
                input[i][j][0] = R;
                input[i][j][1] = G;
                input[i][j][2] = B;
            }
        }

        float[][] C = new float[3][304*304];

        int counter = 0;
        for(int i=0; i<304; i++)
            for(int j=0; j<304; j++) {
                C[0][counter] = (float) -Math.log(input[i][j][0]/255.0);
                C[1][counter] = (float) -Math.log(input[i][j][1]/255.0);
                C[2][counter] = (float) -Math.log(input[i][j][2]/255.0);
                counter += 1;
            }


        float[][] C_add = new float[2][3];
        float[][] C_b = new float[2][1];

        C_add[0][0] = (float) 49.498073;
        C_add[0][1] = (float) -26.59762992;
        C_add[0][2] = (float) 15.80793166;
        C_add[1][0] = (float) -23.99799413;
        C_add[1][1] = (float) 21.06695593;
        C_add[1][2] = (float) -1.91282112;

        C_b[0][0] = (float) 1.1601;
        C_b[1][0] = (float) 2.8347;

        float[][] q = new float[3][92416];
        for(int i=0; i<2; i++)
            for(int j=0; j<304*304; j++)
            {
                q[i][j] = C_add[i][0]*C[0][j];
                q[i][j] += C_add[i][1]*C[1][j];
                q[i][j] += C_add[i][2]*C[2][j];
            }

        for (int i=0; i<2; i++)
            for(int j=0; j<304*304; j++)
            {
                q[i][j] = q[i][j] - C_b[i][0];
            }

        float[][] p = new float[3][2];

        p[0][0] = (float) 0.0246;
        p[0][1] = (float) 0.0;
        p[1][0] = (float) 0.0316;
        p[1][1] = (float) 0.0;
        p[2][0] = (float) 0.0394;
        p[2][1] = (float) 0.0;

        float[][] I1 = new float[92416][3];

        for(int i=0; i<92416; i++)
            for(int j=0; j<3; j++)
            {
                I1[i][j] = (float) Math.exp(-(p[j][0] * q[0][i]));

                if(I1[i][j] < 0.0){    I1[i][j] = 0;   }
                if(I1[i][j] > 1.0){    I1[i][j] = 1;   }

            }

        Bitmap bitmap_mela = Bitmap.createBitmap(304, 304, Bitmap.Config.ARGB_8888);

        for(int i=0; i<304; i++)
            for(int j=0; j<304; j++)
            {
                bitmap_mela.setPixel(j, i, Color.rgb(I1[304*i + j][0], I1[304*i + j][1], I1[304*i + j][2]));
            }

        float[][] pp = new float[3][2];

        pp[0][0] = (float) 0.0;
        pp[0][1] = (float) 0.0193;
        pp[1][0] = (float) 0.0;
        pp[1][1] = (float) 0.0755;
        pp[2][0] = (float) 0.0;
        pp[2][1] = (float) 0.0666;

        float[][] I2 = new float[92416][3];

        for(int i=0; i<92416; i++)
            for(int j=0; j<3; j++)
            {
                I2[i][j] = (float) Math.exp(-(pp[j][1] * q[1][i]));
                if(I2[i][j] < 0.0){    I2[i][j] = 0;   }
                if(I2[i][j] > 1.0){    I2[i][j] = 1;   }
            }

        Bitmap bitmap_hemo = Bitmap.createBitmap(304, 304, Bitmap.Config.ARGB_8888);

        for(int i=0; i<304; i++)
            for(int j=0; j<304; j++)
            {
                bitmap_hemo.setPixel(j, i, Color.rgb(I2[304*i + j][0], I2[304*i + j][1], I2[304*i + j][2]));
            }
        Bitmap[] combined = new Bitmap[2];
        combined[0] = bitmap_hemo;
        combined[1] = bitmap_mela;
        return combined;
    }


    public float[][][][][] make_inputs_unet(int bithw, Bitmap resizedBitmap, Bitmap croppedBitmap2, Bitmap croppedBitmap3)
    {
        int[] coverImageIntArray1D1 = new int[bithw * bithw];
        int[] coverImageIntArray1D2 = new int[bithw * bithw];
        int[] coverImageIntArray1D3 = new int[bithw * bithw];
        resizedBitmap.getPixels(coverImageIntArray1D1, 0,bithw, 0, 0, bithw, bithw);
        croppedBitmap2.getPixels(coverImageIntArray1D2, 0, bithw, 0, 0, bithw, bithw);
        croppedBitmap3.getPixels(coverImageIntArray1D3, 0, bithw, 0, 0, bithw, bithw);



        float [][][][][] comb_input = new float[3][1][304][304][3];
        for(int i=0;i<304;i++){
            for(int j=0;j<304;j++){
                int rgb1 = coverImageIntArray1D1[i*304+j];
                int rgb2 = coverImageIntArray1D2[i*304+j];
                int rgb3 = coverImageIntArray1D3[i*304+j];
                int R1 = Color.red(rgb1);
                int G1 = Color.green(rgb1);
                int B1 = Color.blue(rgb1);
                int R2 = Color.red(rgb2);
                int G2 = Color.green(rgb2);
                int B2 = Color.blue(rgb2);
                int R3 = Color.red(rgb3);
                int G3 = Color.green(rgb3);
                int B3 = Color.blue(rgb3);
                comb_input[0][0][i][j][0] = (float) (R1 / 255.0);
                comb_input[0][0][i][j][1] = (float) (G1 / 255.0);
                comb_input[0][0][i][j][2] = (float) (B1 / 255.0);
                comb_input[1][0][i][j][0] = (float) (R2 / 255.0);
                comb_input[1][0][i][j][1] = (float) (G2 / 255.0);
                comb_input[1][0][i][j][2] = (float) (B2 / 255.0);
                comb_input[2][0][i][j][0] = (float) (R3 / 255.0);
                comb_input[2][0][i][j][1] = (float) (G3 / 255.0);
                comb_input[2][0][i][j][2] = (float) (B3 / 255.0);
            }
        }
        return comb_input;
    }




    public List<MatOfPoint> get_contours(Bitmap mask_bitmap)
    {
        int[] unet_result = new int[304 * 304];
        mask_bitmap.getPixels(unet_result, 0, 304, 0, 0, 304, 304);
        Mat contour_input = new Mat();
        Mat contour_input22 = new Mat();
        Utils.bitmapToMat(mask_bitmap, contour_input);

        Imgproc.cvtColor(contour_input, contour_input22, Imgproc.COLOR_BGR2GRAY);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(contour_input22, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);


        return contours;
    }

    public Rect adjustRectangles(Rect rect, Mat orig_img)
    {
        if(rect.x >= 10){ rect.x-=10; }
        if(rect.y >= 10){ rect.y-=10; }

        if(rect.x + rect.width + 20 < orig_img.cols()) { rect.width+=20; }
        else{rect.width=orig_img.cols()-rect.x;}

        if(rect.y + rect.height + 20 < orig_img.rows()){ rect.height+=20; }
        else{rect.height=orig_img.rows()-rect.y;}


        return rect;
    }


    public Bitmap crop_segments(ContentResolver contentResolver, Uri imageUri1, Rect rect, int width, int height)
    {

        Bitmap originalBitmap = null;
        try {
            originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mat originImg = new Mat(originalBitmap.getWidth() ,originalBitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(originalBitmap, originImg);
        Mat subImg = originImg.submat(rect);
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(subImg, bmp);

        Bitmap ret = Bitmap.createScaledBitmap(bmp, 224, 224, true);

        return ret;
    }

    public int[] getOriginalDims(Rect rect) {

        int [] orig = new int [4];
        orig[0] = rect.x;
        orig[1] = rect.y;
        orig[2] = rect.height;
        orig[3] = rect.width;

        return orig;
    }

    public void drawContourRects(Mat orig_img, List<MatOfPoint> contour, Rect rect) {
        Imgproc.drawContours(orig_img, contour, 0, new Scalar(0,255,0), 1);
        Imgproc.rectangle(orig_img, rect.tl(), rect.br(), new Scalar(255,0,0), 2);
    }

    public RectangleRange getRectangleRange(int[] originalDims) {
        return new RectangleRange(originalDims[1], originalDims[1]+originalDims[2], originalDims[0], originalDims[0]+originalDims[3]);
    }

    public Mat getResizedMat(Bitmap resizedBitmap) {
        Mat orig_img = new Mat();
        Utils.bitmapToMat(resizedBitmap, orig_img);
        return orig_img;
    }

    public float[][][] initBmpOutputs(List<MatOfPoint> contours) {
        return new float[contours.size()][1][18];
    }

    public Bitmap[] initBmps(List<MatOfPoint> contours) {
        return new Bitmap[contours.size()];
    }

    public Bitmap maskBitmap(float[][][][] output2) {
        Bitmap mask_bitmap = Bitmap.createBitmap(304, 304, Bitmap.Config.ARGB_8888);

        for(int i=0;i<304;i++){
            for(int j=0;j<304;j++){
                if(output2[0][i][j][0]>=0.10){          mask_bitmap.setPixel(j, i, Color.WHITE);           }
                else                         {          mask_bitmap.setPixel(j, i, Color.BLACK);           }
            }
        }
        return mask_bitmap;
    }

    public float[][][][] make_inputs_effnet(Bitmap bmp) {
        int[] temp_eff = new int[224*224];
        bmp.getPixels(temp_eff, 0, 224, 0, 0, 224, 224);
        float [][][][] effin = new float[1][224][224][3];
        for(int a=0; a<224; a++)
            for(int b=0; b<224; b++)
            {
                int rgb1 = temp_eff[a*224+b];
                int R1 = Color.red(rgb1);
                int G1 = Color.green(rgb1);
                int B1 = Color.blue(rgb1);
                effin[0][a][b][0] = (float) (R1);
                effin[0][a][b][1] = (float) (G1);
                effin[0][a][b][2] = (float) (B1);

            }

        return effin;
    }
}

