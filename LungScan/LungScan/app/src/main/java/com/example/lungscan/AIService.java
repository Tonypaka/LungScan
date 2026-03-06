package com.example.lungscan;

import android.content.Context;
import android.graphics.Bitmap;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class AIService {

    public static class Result {
        public String stage;
        public float confidence;
        public int color;

        public Result(String stage, float confidence, int color) {
            this.stage = stage;
            this.confidence = confidence;
            this.color = color;
        }
    }

    private static final String[] STAGES = {
        "ไม่พบมะเร็ง",
        "ระยะที่ 1 : ความเสี่ยงต่ำ",
        "ระยะที่ 2 : ความเสี่ยงปานกลาง",
        "ระยะที่ 3 : ความเสี่ยงสูง"
    };

    private static final int[] COLORS = {
        0xFF4CAF50, // เขียว
        0xFF8BC34A, // เขียวอ่อน
        0xFFFF9800, // ส้ม
        0xFFF44336  // แดง
    };

    // ขนาด input ที่ model ต้องการ
    private static final int IMG_SIZE = 224;
    private static final int NUM_CHANNELS = 3;
    private static final int NUM_CLASSES = 4;

    public static Result analyze(Context context, Bitmap bitmap) {
        try {
            // โหลด TFLite model จาก assets
            Interpreter interpreter = new Interpreter(loadModelFile(context));

            // Resize bitmap เป็น 224x224
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);

            // แปลง Bitmap เป็น ByteBuffer (RGB float normalized 0-1)
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * NUM_CHANNELS);
            inputBuffer.order(ByteOrder.nativeOrder());

            int[] pixels = new int[IMG_SIZE * IMG_SIZE];
            resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE);

            for (int pixel : pixels) {
                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8) & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;
                inputBuffer.putFloat(r);
                inputBuffer.putFloat(g);
                inputBuffer.putFloat(b);
            }

            // รัน inference
            float[][] output = new float[1][NUM_CLASSES];
            interpreter.run(inputBuffer, output);
            interpreter.close();

            // หา class ที่มี probability สูงสุด
            int bestIdx = 0;
            float bestScore = output[0][0];
            for (int i = 1; i < NUM_CLASSES; i++) {
                if (output[0][i] > bestScore) {
                    bestScore = output[0][i];
                    bestIdx = i;
                }
            }

            float confidence = bestScore * 100f;
            return new Result(STAGES[bestIdx], confidence, COLORS[bestIdx]);

        } catch (Exception e) {
            // ถ้า model error ให้ fallback
            return new Result("วิเคราะห์ไม่สำเร็จ", 0f, 0xFF9E9E9E);
        }
    }

    private static MappedByteBuffer loadModelFile(Context context) throws IOException {
        android.content.res.AssetFileDescriptor fd =
            context.getAssets().openFd("lung_model.tflite");
        FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = fis.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }
}
