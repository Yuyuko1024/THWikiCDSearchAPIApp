package net.hearnsoft.thwikicdsearchapi.utils;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import net.hearnsoft.thwikicdsearchapi.R;

public class DataToClipboard {

    public static void copyToClipboard(Context context,String data){
        ClipboardManager clip = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Label", data);
        clip.setPrimaryClip(clipData);
        Toast.makeText(context, context.getString(R.string.toast_copy_success)+data,
                Toast.LENGTH_SHORT).show();
    }

}
