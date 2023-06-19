package net.hearnsoft.thwikicdsearchapi;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.hearnsoft.thwikicdsearchapi.databinding.CrashActivityBinding;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class CrashActivity extends AppCompatActivity {

    private CrashActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CrashActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        CaocConfig caocConfig = CustomActivityOnCrash.getConfigFromIntent(getIntent());
        String reason_text = CustomActivityOnCrash.getStackTraceFromIntent(getIntent());
        //binding.bugsView.playAnimation();
        binding.crashReasonText.setText(getString(R.string.crash_reason,reason_text));
        binding.exit.setOnClickListener(v -> {
            finish();
            System.exit(1);
        });
        binding.restart.setOnClickListener(v -> {
            CustomActivityOnCrash.restartApplication(this,caocConfig );
        });
    }
}
