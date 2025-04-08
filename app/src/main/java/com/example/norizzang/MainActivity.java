package com.example.norizzang;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.widget.ImageButton;


public class MainActivity extends BridgeActivity {
    private WebView webView;

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack(); // 웹 뒤로가기
        } else {
            showExitConfirmation(); // 앱 종료 확인 팝업 띄우기
        }
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = bridge.getWebView();

        // ✅ 이미지 버튼 생성
        ImageButton scrollToTopButton = new ImageButton(this);
        scrollToTopButton.setImageResource(R.drawable.ic_scroll_top); // 넣은 이미지 연결
        scrollToTopButton.setBackgroundColor(Color.TRANSPARENT); // 배경 제거
        scrollToTopButton.setScaleType(ImageButton.ScaleType.FIT_CENTER); // 이미지 가운데 정렬
        scrollToTopButton.setAdjustViewBounds(true); // 이미지 비율 유지

        // ✅ 위치 설정
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                200, 200 // 버튼 크기
        );
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(0, 0, 30, 80); // 오른쪽 하단 여백

        // ✅ 클릭 시 동작
        scrollToTopButton.setOnClickListener(v -> {
            webView.evaluateJavascript("window.scrollTo({top: 0, behavior: 'smooth'});", null);
        });

        // ✅ 버튼 추가
        FrameLayout rootView = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.addView(scrollToTopButton, params);
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("앱 종료")
                .setMessage("앱을 종료하시겠어요?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // 앱 종료
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 팝업만 닫기
                    }
                })
                .setCancelable(true)
                .show();
    }
}
