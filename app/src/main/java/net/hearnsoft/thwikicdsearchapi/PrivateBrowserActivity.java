package net.hearnsoft.thwikicdsearchapi;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.hearnsoft.thwikicdsearchapi.databinding.BrowserActivityBinding;

public class PrivateBrowserActivity extends AppCompatActivity {

    private BrowserActivityBinding binding;
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BrowserActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        url = getIntent().getStringExtra("url");
        binding.webview.loadUrl(url);
        initWebChromeClient();
    }


    private void initWebChromeClient() {
        WebSettings settings = binding.webview.getSettings();
        settings.setJavaScriptEnabled(true);
        binding.webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                binding.webview.loadUrl(request.getUrl().toString());
                return true;
            }

        });
        binding.webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                binding.webTitle.setText(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100){
                    binding.webProgress.setVisibility(View.GONE);
                } else {
                    binding.webProgress.setVisibility(View.VISIBLE);
                    binding.webProgress.setProgress(newProgress);
                }
            }
        });
    }

}
