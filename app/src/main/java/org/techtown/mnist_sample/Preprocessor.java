package org.techtown.mnist_sample;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Preprocessor {
    int[] coverImageIntArray1D;
    public Preprocessor()
    {

    }


    public Bitmap[] decompose(Bitmap croppedBitmap) {
        coverImageIntArray1D = new int[304 * 304];
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


    public float[][][][][] make_inputs(int bithw, Bitmap resizedBitmap, Bitmap croppedBitmap2, Bitmap croppedBitmap3)
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
}

