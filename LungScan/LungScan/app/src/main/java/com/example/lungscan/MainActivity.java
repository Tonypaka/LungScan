package com.example.lungscan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    Uri selectedUri = null;
    Bitmap selectedBitmap = null;
    ActivityResultLauncher<String> pickerLauncher;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivPreview  = findViewById(R.id.ivPreview);
        Button btnSelect     = findViewById(R.id.btnSelectImage);
        Button btnScan       = findViewById(R.id.btnScan);
        ProgressBar progress = findViewById(R.id.progressBar);
        TextView tvLoading   = findViewById(R.id.tvLoading);

        pickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedUri = uri;
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
                        byte[] bytes = baos.toByteArray();
                        selectedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ivPreview.setImageBitmap(selectedBitmap);
                        ivPreview.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "✅ เลือกรูปสำเร็จ", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "โหลดรูปไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        btnSelect.setOnClickListener(v -> pickerLauncher.launch("image/*"));

        btnScan.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, "กรุณาเลือกรูป X-ray ก่อน", Toast.LENGTH_SHORT).show();
                return;
            }

            progress.setVisibility(View.VISIBLE);
            tvLoading.setVisibility(View.VISIBLE);
            btnScan.setEnabled(false);
            btnSelect.setEnabled(false);

            Bitmap bitmapToAnalyze = selectedBitmap;

            executor.execute(() -> {
                // รัน TFLite inference ใน background — ทำงาน Offline 100%
                AIService.Result result = AIService.analyze(MainActivity.this, bitmapToAnalyze);

                handler.post(() -> {
                    progress.setVisibility(View.GONE);
                    tvLoading.setVisibility(View.GONE);
                    btnScan.setEnabled(true);
                    btnSelect.setEnabled(true);

                    ResultDialog dialog = new ResultDialog(this, result, () -> {
                        ivPreview.setVisibility(View.GONE);
                        selectedBitmap = null;
                        selectedUri = null;
                    });
                    dialog.show();
                });
            });
        });
    }
}
