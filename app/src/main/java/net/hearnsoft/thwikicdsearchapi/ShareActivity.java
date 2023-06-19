package net.hearnsoft.thwikicdsearchapi;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String content_text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isShareAction",true);
        intent.putExtra("text",content_text);
        startActivity(intent);
        finish();
    }
}
