package net.hearnsoft.thwikicdsearchapi;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class PhotoViewActivity extends AppCompatActivity {

    PhotoView photo;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);
        photo=findViewById(R.id.photo);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        String url = getIntent().getStringExtra("URL");
        String title = getIntent().getStringExtra("title");
        setTitle(title);
        try {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_unknown_pic)
                    .into(photo);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
