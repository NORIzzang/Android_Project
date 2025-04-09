package com.example.norizzang;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.getcapacitor.BridgeActivity;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.widget.SeekBar;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageButton;
import android.graphics.Color;
import android.annotation.SuppressLint;

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

        // ✅ Capacitor WebView 연결
        webView = bridge.getWebView();

        // ✅ 루트 뷰 (전체 뷰를 감싸는 FrameLayout)
        FrameLayout rootView = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);

        // ✅ 슬라이더 (SeekBar) 생성
        SeekBar fontSizeSeekBar = new SeekBar(this);
        fontSizeSeekBar.setMax(30);
        fontSizeSeekBar.setProgress(16);
        fontSizeSeekBar.setBackgroundColor(Color.LTGRAY);

        // ✅ 처음엔 숨기기
        fontSizeSeekBar.setVisibility(View.GONE);

        // ✅ 슬라이더 Layout 설정 (상단 고정)
        FrameLayout.LayoutParams seekBarParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        seekBarParams.gravity = Gravity.TOP;

        // ✅ 슬라이더 이벤트
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fontSize = Math.max(progress, 10); // 최소 10px
                String jsCode = "document.body.style.fontSize='" + fontSize + "px';";
                webView.evaluateJavascript(jsCode, null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // ✅ 슬라이더 추가
        rootView.addView(fontSizeSeekBar, seekBarParams);

        // ✅ 웹페이지 로딩 완료 시 슬라이더 보이기 + padding 추가
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // padding-top으로 슬라이더 공간 확보 (겹침 방지)
                view.evaluateJavascript("document.body.style.paddingTop='22px';", null);

                // 슬라이더 보이게 설정
                fontSizeSeekBar.setVisibility(View.VISIBLE);
            }
        });

        // ✅ 이미지 버튼 생성 (스크롤 상단 이동)
        ImageButton scrollToTopButton = new ImageButton(this);
        scrollToTopButton.setImageResource(R.drawable.ic_scroll_top); // drawable 폴더에 이미지 필요
        scrollToTopButton.setBackgroundColor(Color.TRANSPARENT);
        scrollToTopButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        scrollToTopButton.setAdjustViewBounds(true);

        // ✅ 버튼 위치 (오른쪽 하단)
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                200, 200
        );
        buttonParams.gravity = Gravity.BOTTOM | Gravity.END;
        buttonParams.setMargins(0, 0, 30, 80);

        // ✅ 버튼 동작
        scrollToTopButton.setOnClickListener(v -> {
            webView.evaluateJavascript("window.scrollTo({top: 0, behavior: 'smooth'});", null);
        });

        // ✅ 버튼 추가
        rootView.addView(scrollToTopButton, buttonParams);
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
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}
