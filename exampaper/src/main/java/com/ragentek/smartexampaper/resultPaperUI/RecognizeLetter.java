package com.ragentek.smartexampaper.resultPaperUI;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecognizeLetter {
    private static final String MODEL_FILE = "file:///android_asset/letters.pb";

    private static final int IN_COL = 1;
    private static final int IN_ROW = 28 * 28;
    private static final int OUT_COL = 1;
    private static final int OUT_ROW = 1;

    private static final String inputName = "input/x_input";
    private static final String outputName = "output";

    private TensorFlowInferenceInterface mInterface;

    static {
        System.loadLibrary("tensorflow_inference");
    }

    RecognizeLetter(AssetManager assetManager) {
        mInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
    }

    public int[] getPredict(Bitmap bitmap) {
        float[] inputData = bitmapToFloatArray(bitmap, 28, 28);
        mInterface.feed(inputName, inputData, IN_COL, IN_ROW);

        float[] keep_prob = new float[1];
        keep_prob[0] = (float) 1.0;
        mInterface.feed("keep_prob", keep_prob);

        String[] outputNames = new String[]{outputName};
        mInterface.run(outputNames);

        int[] outputs = new int[OUT_COL * OUT_ROW];
        mInterface.fetch(outputName, outputs);
        Log.i("RecognizeLetter", "getPredict: outputs.length=" + outputs.length);
        return outputs;
    }

    public char toLetter(int idx) {
        Log.i("RecognizeLetter", "toLetter: idx=" + idx);
        return (char) ((int) 'A' + idx - 1);
    }

    private float[] bitmapToFloatArray(Bitmap bitmap, int rx, int ry) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        float scaleWidth = ((float) rx) / width;
        float scaleHeight = ((float) ry) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        height = bitmap.getHeight();
        width = bitmap.getWidth();

        float[] result = new float[height * width];
        int k = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int argb = bitmap.getPixel(i, j);
                int r = Color.red(argb);
                result[k++] = r / 255.0f;
            }
        }

        return result;
    }
}
