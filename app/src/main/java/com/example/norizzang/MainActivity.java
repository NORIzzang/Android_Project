package com.example.norizzang;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private WebView webView;
    private static final String PREF_NAME = "FontPrefs";
    private static final String KEY_FONT_SIZE = "fontSize";
    private int currentFontSize = 16;

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            showExitConfirmation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = bridge.getWebView();

        // ✅ JavaScript Bridge for printing
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new PrintBridge(this), "Android");

        FrameLayout rootView = findViewById(android.R.id.content);
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        currentFontSize = prefs.getInt(KEY_FONT_SIZE, 16);

        // 🔹 Font Size Slider
        SeekBar fontSizeSeekBar = new SeekBar(this);
        fontSizeSeekBar.setMax(30);
        fontSizeSeekBar.setProgress(currentFontSize);
        fontSizeSeekBar.setVisibility(View.GONE);
        fontSizeSeekBar.getProgressDrawable().setColorFilter(
                Color.parseColor("#5F5FCE"), android.graphics.PorterDuff.Mode.SRC_IN);
        fontSizeSeekBar.getThumb().setColorFilter(
                Color.parseColor("#5F5FCE"), android.graphics.PorterDuff.Mode.SRC_IN);

        float scale = getResources().getDisplayMetrics().density;
        int pxWidth = (int) (250 * scale + 0.5f);
        int pxHeight = (int) (48 * scale + 0.5f);
        fontSizeSeekBar.setTranslationY(10f * scale);

        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(pxWidth, pxHeight);
        fontSizeSeekBar.setLayoutParams(seekBarParams);
        fontSizeSeekBar.setThumbOffset(0);

        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fontSize = Math.max(progress, 10);
                prefs.edit().putInt(KEY_FONT_SIZE, fontSize).apply();
                String jsCode = "(() => {" +
                        "const tags = ['body','p','span','div','td','th','li','a','button','input','h1','h2','h3','h4','h5','h6'];" +
                        "tags.forEach(tag => {" +
                        "  document.querySelectorAll(tag).forEach(el => {" +
                        "    el.style.fontSize = '" + fontSize + "px';" +
                        "  });" +
                        "});})();";
                webView.evaluateJavascript(jsCode, null);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 🔹 Font Toggle Button
        ImageButton fontToggleButton = new ImageButton(this);
        fontToggleButton.setImageResource(R.drawable.ic_font_toggle);
        fontToggleButton.setBackgroundColor(Color.TRANSPARENT);
        fontToggleButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        fontToggleButton.setAdjustViewBounds(true);
        fontToggleButton.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        fontToggleButton.setVisibility(View.GONE);

        fontToggleButton.setOnClickListener(v -> {
            fontSizeSeekBar.setVisibility(
                    fontSizeSeekBar.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        LinearLayout fontControlLayout = new LinearLayout(this);
        fontControlLayout.setOrientation(LinearLayout.HORIZONTAL);
        fontControlLayout.addView(fontSizeSeekBar);
        fontControlLayout.addView(fontToggleButton);

        FrameLayout.LayoutParams fontLayoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        fontLayoutParams.gravity = Gravity.END | Gravity.BOTTOM;
        fontLayoutParams.setMargins(0, 0, 30, 215);
        rootView.addView(fontControlLayout, fontLayoutParams);

        // 🔹 Scroll-to-top button
        ImageButton scrollToTopButton = new ImageButton(this);
        scrollToTopButton.setImageResource(R.drawable.ic_scroll_top);
        scrollToTopButton.setBackgroundColor(Color.TRANSPARENT);
        scrollToTopButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        scrollToTopButton.setAdjustViewBounds(true);
        scrollToTopButton.setVisibility(View.GONE);

        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(200, 200);
        buttonParams.gravity = Gravity.BOTTOM | Gravity.END;
        buttonParams.setMargins(0, 0, 30, 80);
        rootView.addView(scrollToTopButton, buttonParams);

        scrollToTopButton.setOnClickListener(v ->
                webView.evaluateJavascript("window.scrollTo({top: 0, behavior: 'smooth'});", null)
        );

        // 🔹 WebView Load Events
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.evaluateJavascript("document.body.style.paddingTop='22px';", null);

                String jsInitFont = "(() => {" +
                        "const tags = ['body','p','span','div','td','th','li','a','button','input','h1','h2','h3','h4','h5','h6'];" +
                        "tags.forEach(tag => {" +
                        "  document.querySelectorAll(tag).forEach(el => {" +
                        "    el.style.fontSize = '" + currentFontSize + "px';" +
                        "  });" +
                        "});})();";
                view.evaluateJavascript(jsInitFont, null);
                view.evaluateJavascript("tryShowMain && tryShowMain();", null);
                webView.setBackgroundColor(Color.TRANSPARENT);


                fontToggleButton.setVisibility(View.VISIBLE);
                scrollToTopButton.setVisibility(View.VISIBLE);
                // ✅ WebView에서 JS 함수 호출: 로딩 화면 제거!
                view.evaluateJavascript("if (typeof hideLoader === 'function') hideLoader();", null);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                view.loadUrl("about:blank");
                view.setBackgroundColor(Color.WHITE);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("네트워크 오류")
                        .setMessage("인터넷에 연결할 수 없습니다.\n연결 상황을 확인해주세요.")
                        .setCancelable(false)
                        .setPositiveButton("확인", (dialog, which) -> finish())
                        .show();
            }
        });
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("앱 종료")
                .setMessage("앱을 종료하시겠어요?")
                .setPositiveButton("확인", (dialog, which) -> finish())
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    // ✅ Print 기능을 위한 Bridge 클래스
    public static class PrintBridge {
        private final Context context;

        public PrintBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void printHtml(String html) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                WebView printWebView = new WebView(context);
                printWebView.getSettings().setJavaScriptEnabled(true);
                printWebView.getSettings().setUseWideViewPort(true);
                printWebView.getSettings().setLoadWithOverviewMode(true);
                printWebView.setInitialScale(100); // 정확히 100% 비율로 출력

                // HTML을 감싸는 스타일 적용 (강제 확대)
                String wrappedHtml = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<style>" +
                        "body { margin:0; padding:0; font-size:16pt; width:100vw; max-width:100%; font-family:'Malgun Gothic', sans-serif; }" +
                        "table { width:100%; border-collapse:collapse; }" +
                        "th, td { border:1px solid black; padding:10px; font-size:16pt; }" +
                        "</style></head><body>" + html + "</body></html>";

                printWebView.loadDataWithBaseURL(null, wrappedHtml, "text/HTML", "UTF-8", null);

                printWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                        PrintDocumentAdapter printAdapter = printWebView.createPrintDocumentAdapter("콘티 출력");

                        PrintAttributes.Builder builder = new PrintAttributes.Builder();
                        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
                        builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

                        printManager.print("콘티 출력", printAdapter, builder.build());
                    }
                });
            });
        }
    }
}
