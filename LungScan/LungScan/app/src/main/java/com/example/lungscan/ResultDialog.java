package com.example.lungscan;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class ResultDialog extends Dialog {

    private final AIService.Result result;
    private final Runnable onScanAgain;

    public ResultDialog(@NonNull Context context, AIService.Result result, Runnable onScanAgain) {
        super(context);
        this.result = result;
        this.onScanAgain = onScanAgain;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_result);
        setCancelable(false);

        // 🔥 จัด dialog ไม่ให้เละ + อยู่กลาง
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );

            getWindow().setDimAmount(0.6f);
        }

        TextView tvStage      = findViewById(R.id.tvStage);
        TextView tvConfidence = findViewById(R.id.tvConfidence);
        ImageView ivIcon      = findViewById(R.id.ivResultIcon);
        Button btnScanAgain   = findViewById(R.id.btnScanAgain);
        Button btnClose       = findViewById(R.id.btnClose);

        tvStage.setText(result.stage);
        tvStage.setTextColor(result.color);
        tvConfidence.setText(String.format("ความมั่นใจ %.1f%%", result.confidence));

        if (result.stage.equals("ไม่พบมะเร็ง")) {
            ivIcon.setImageResource(android.R.drawable.ic_menu_info_details);
            ivIcon.setColorFilter(Color.parseColor("#4CAF50"));
        } else if (result.stage.contains("ระยะที่ 1")) {
            ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            ivIcon.setColorFilter(Color.parseColor("#8BC34A"));
        } else if (result.stage.contains("ระยะที่ 2")) {
            ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            ivIcon.setColorFilter(Color.parseColor("#FF9800"));
        } else {
            ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            ivIcon.setColorFilter(Color.parseColor("#F44336"));
        }

        btnScanAgain.setOnClickListener(v -> {
            dismiss();
            if (onScanAgain != null) onScanAgain.run();
        });

        btnClose.setOnClickListener(v -> {
            dismiss();
            if (getContext() instanceof Activity) {
                ((Activity) getContext()).finish();
            }
        });
    }
}
