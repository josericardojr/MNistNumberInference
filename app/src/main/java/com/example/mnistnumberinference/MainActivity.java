package com.example.mnistnumberinference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mnistnumberinference.ml.MnistModel;
import com.google.android.material.slider.Slider;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    Slider strokeSlider;
    DrawView drawView;
    Button btnClear, btnInfer;
    TextView txtResult;
    MnistModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
        loadML();
    }

    private void loadML() {
        try {
            model = MnistModel.newInstance(this);
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        model.close();
    }

    private void setup() {
        drawView = (DrawView) findViewById(R.id.drawView);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnInfer = (Button) findViewById(R.id.btnInfer);
        txtResult = (TextView) findViewById(R.id.txtResult);

        strokeSlider = (Slider) findViewById(R.id.sldStroke);
        strokeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                drawView.setDrawStroke(slider.getValue());
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {

            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.clearCanvas();
            }
        });

        btnInfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap bmp = drawView.getBitmap();
                bmp = Bitmap.createScaledBitmap(bmp, 28, 28, true);
                float[] values = toNormalizedGray(bmp);

                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bmp.getWidth() * bmp.getHeight() * 4);
                byteBuffer.order(ByteOrder.nativeOrder());

                for (int i = 0; i < values.length; i++) byteBuffer.putFloat(values[i]);

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 28, 28, 1}, DataType.FLOAT32);
                inputFeature0.loadBuffer(byteBuffer);

                // Runs model inference and gets result.
                MnistModel.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                float[] confidence = outputFeature0.getFloatArray();

                int maxPosition = 0;
                float maxConfidence = 0;

                for (int i = 0; i < confidence.length; i++){

                    if (confidence[i] > maxConfidence){
                        maxPosition = i;
                        maxConfidence = confidence[i];
                    }
                }


                String []digits = {"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven",
                    "Eight", "Nine"};


                txtResult.setText("Result: " + digits[maxPosition]);
            }
        });
    }

    private float[] toNormalizedGray(Bitmap bmp){
        int values[] = new int[bmp.getWidth() * bmp.getHeight()];
        bmp.getPixels(values, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        int pixel = 0;

        float normalizedGrayValues[] = new float[bmp.getWidth() * bmp.getHeight()];

        for (int i = 0; i < bmp.getHeight(); i++){
            for (int j = 0; j < bmp.getWidth(); j++){
                int val = values[pixel];

                int r = (val >> 16) & 0xff;
                int g = (val >> 8) & 0xff;
                int b = val & 0xff;

                float gray = (float) r / 255.0f * 0.299f + (float) g / 255.0f * 0.587f +
                        (float) b / 255.0f * 0.114f;

                normalizedGrayValues[pixel++] = gray;
            }
        }

        return normalizedGrayValues;
    }
}