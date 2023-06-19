package net.hearnsoft.thwikicdsearchapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tencent.bugly.crashreport.CrashReport;

import net.hearnsoft.thwikicdsearchapi.adapter.SongAdapter;
import net.hearnsoft.thwikicdsearchapi.bean.DataBean;
import net.hearnsoft.thwikicdsearchapi.databinding.AboutDialogBinding;
import net.hearnsoft.thwikicdsearchapi.databinding.ActivityMainBinding;
import net.hearnsoft.thwikicdsearchapi.databinding.CircleInputBinding;
import net.hearnsoft.thwikicdsearchapi.widget.GoTopFABBehavior;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<String> songList,circleList;
    private ActivityMainBinding binding;
    private CircleInputBinding circleBinding;
    private final OkHttpClient client = new OkHttpClient();
    private AlertDialog dialogs;
    private Handler handler;
    private String queryText;
    private boolean isResultShowed = false;
    private boolean addCircleSearch = false;
    private int totalResultCount;
    private int pageCount;
    private int offset = 0;
    private int itemOffset = 5;
    private int itemOffsetBackup;
    private int current_page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化崩溃组件处理
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                .enabled(true)
                .trackActivities(true)
                .errorActivity(CrashActivity.class)
                .apply();
        //初始化Bugly
        buglyInit();
        //初始化UI
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        circleBinding = CircleInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //隐藏掉对应的组件
        binding.listTop.hide();
        binding.btnPriv.setEnabled(false);
        binding.btnNext.setEnabled(false);
        //AutoCompleteTextView的一些设置
        //设置在输入多少个字符后弹出列表
        binding.queryText.setThreshold(1);
        circleBinding.queryCircleText.setThreshold(1);
        //原曲搜索框的候选词列表事件
        binding.queryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                CompletableFuture<Boolean> future = getAutoCompleteData(binding.queryText.getText().toString(),0);
                future.thenAccept(isOK -> {
                    if (isOK) {
                        Log.d(TAG, "autoCompleteListCount:" + songList.size());
                        //binding.queryText.showDropDown();
                    }
                });
            }
        });
        //对社团搜索框做同样的事情
        circleBinding.queryCircleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                CompletableFuture<Boolean> future = getAutoCompleteData(circleBinding.queryCircleText.getText().toString(),1);
                future.thenAccept(isOK -> {
                    if (isOK) {
                        Log.d(TAG, "autoCompleteListCount:" + circleList.size());
                        //circleBinding.queryCircleText.showDropDown();
                    }
                });
            }
        });
        //自动补全框的init
        autoCompleteDropDownInit();
        binding.listTop.setOnClickListener(v -> {
            //一键到顶
            binding.resultList.smoothScrollToPosition(0);
            binding.listTop.hide();
        });
        //把FABBehavior绑定到RecyclerView上
        binding.resultList.addOnScrollListener(new GoTopFABBehavior(binding.listTop));
        binding.search.setOnClickListener(v -> {
            //通常输入框文本在决定搜索以后就暂时不会变化了。
            //将一些数据重置为初始状态
            current_page = 1;
            offset = 0;
            //将当前的offset做一个备份
            itemOffsetBackup = binding.itemCount.getSelectedItemPosition();
            //获取输入框里的文本并搜索
            getQuertTextAndSearch();
        });
        if (BuildConfig.DEBUG){
            binding.search.setOnLongClickListener(v -> {
                //测试用的，长按来一个crash
                CrashReport.testJavaCrash();
                return true;
            });
        }
        //初始化View时候如果父布局已经有了子布局则移除掉
        LinearLayout parent = binding.inputTab;
        View view = circleBinding.getRoot();
        ViewParent parent1 = view.getParent();
        if (parent1 != null) {
            ((LinearLayout) parent1).removeView(view);
        }
        //添加社团搜索按钮的事件
        binding.addSearch.setOnClickListener(v -> {
            if (addCircleSearch) {
                parent.removeView(view);
                addCircleSearch = false;
                binding.addSearch.setImageResource(R.drawable.ic_add);
            } else {
                parent.addView(view);
                addCircleSearch = true;
                binding.addSearch.setImageResource(R.drawable.ic_remove);
            }
        });
        //设置每页显示的数量的选择事件
        binding.itemCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isResultShowed && position != itemOffsetBackup) {
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle(R.string.research_title)
                            .setMessage(R.string.research_msg)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                itemOffset = Integer.parseInt(countId2int(position));
                                current_page = 1;
                                offset = 0;
                                getQuertTextAndSearch();
                            })
                            .setNegativeButton(android.R.string.cancel, (dialog, which) ->
                                    binding.itemCount.setSelection(itemOffsetBackup))
                            .show();
                } else {
                    itemOffset = Integer.parseInt(countId2int(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                itemOffset = 5;
            }
        });
        //检测是否有传入的搜索词
        if (getIntent().getBooleanExtra("isShareAction",false)
                && getIntent().getStringExtra("text") != null){
            //通常输入框文本在决定搜索以后就暂时不会变化了。
            //将一些数据重置为初始状态
            current_page = 1;
            offset = 0;
            //将当前的offset做一个备份
            itemOffsetBackup = binding.itemCount.getSelectedItemPosition();
            binding.queryText.setText(getIntent().getStringExtra("text"));
            //获取输入框里的文本并搜索
            getQuertTextAndSearch();
        }
        //初始化Handler
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0:
                        //准备从接口获得的数据
                        String text = msg.obj.toString();
                        //Log.d(TAG,"text:" +text);
                        //如果返回的是空的，在没做判断情况下会导致反序列化失败，所以在这里先判断一下
                        if (TextUtils.isEmpty(text)){
                            dialogs.dismiss();
                            Snackbar.make(binding.resultList, R.string.query_result_empty, Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        //初始化Gson
                        Gson gson = new GsonBuilder()
                                .disableHtmlEscaping()
                                .setLenient()
                                .serializeNulls()
                                .setPrettyPrinting()
                                .enableComplexMapKeySerialization()
                                .create();
                        //反序列化获得的json数据
                        DataBean bean = gson.fromJson(text, DataBean.class);
                        if (bean.getTota() == 0) {
                            //如果什么都没搜到，也要取消这个弹窗哦
                            dialogs.dismiss();
                            Snackbar.make(binding.resultList, R.string.query_result_empty, Snackbar.LENGTH_LONG).show();
                        } else {
                            //如果搜到了，就准备显示结果
                            binding.itemDisp.setText(getString(R.string.item_count_text,
                                    String.valueOf(bean.getTota()), String.valueOf(totalResultCount)));
                            DataBean.ResuBean data = bean.getResu();
                            pageCount = (totalResultCount +
                                    Integer.parseInt(countId2int(binding.itemCount.getSelectedItemId())) - 1)
                                    / Integer.parseInt(countId2int(binding.itemCount.getSelectedItemId()));
                            Log.d(TAG, "可翻页的页数：" + pageCount);
                            loadResult(data);
                        }
                        break;
                    case 1:
                        //预留的消息，暂时没用
                        //binding.result.setText("Error");
                        break;
                    case 3:
                        //通知UI显示候选结果
                        if (songList != null){
                            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(MainActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, songList);
                            binding.queryText.setAdapter(adapter1);
                            adapter1.notifyDataSetChanged();
                        }
                        break;
                    case 4:
                        //这个也是
                        if (circleList != null){
                            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(MainActivity.this,
                                    android.R.layout.simple_dropdown_item_1line, circleList);
                            circleBinding.queryCircleText.setAdapter(adapter2);
                            adapter2.notifyDataSetChanged();
                        }
                        break;
                }
            }
        };
    }

    private void buglyInit() {
        //初始化Bugly和SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        //如果设置里没检测到对话框已弹出过，就弹出对话框让用户选择
        if (!sharedPref.getBoolean("dialog_showed", false)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.start_perm_dialog_title)
                    .setMessage(R.string.start_perm_dialog_msg)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        //写入设置并初始化Bugly
                        editor.putBoolean("init_bugly", true);
                        editor.putBoolean("dialog_showed", true);
                        editor.apply();
                        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(MainActivity.this);
                        strategy.setDeviceModel(Build.DEVICE);
                        CrashReport.setUserSceneTag(MainActivity.this, 1001);
                        CrashReport.initCrashReport(getApplicationContext(), strategy);
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        //写入设置
                        editor.putBoolean("init_bugly", false);
                        editor.putBoolean("dialog_showed", true);
                        editor.apply();
                    })
                    .show();
        }
        //如果设置里检测到初始化Bugly的选项，就进行初始化Bugly
        if (sharedPref.getBoolean("init_bugly", false)) {
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(MainActivity.this);
            strategy.setDeviceModel(Build.DEVICE);
            CrashReport.setUserSceneTag(MainActivity.this, 1001);
            CrashReport.initCrashReport(getApplicationContext(), strategy);
        }
    }

    private void autoCompleteDropDownInit(){
        //当获取到焦点时显示下拉候选词列表
        binding.queryText.setOnFocusChangeListener((v, hasFocus) -> {
            //当输入框获取到焦点且Activity没有结束且Activity有窗口焦点时显示下拉列表
            if (hasFocus && !this.isFinishing() && this.hasWindowFocus()) {
                binding.queryText.showDropDown();
            } else {
                binding.queryText.dismissDropDown();
            }
        });
        //这个和上面的一样
        circleBinding.queryCircleText.setOnFocusChangeListener((v, hasFocus) -> {
            //当输入框获取到焦点且Activity没有结束且Activity有窗口焦点时显示下拉列表
            if (hasFocus && !this.isFinishing() && this.hasWindowFocus()) {
                circleBinding.queryCircleText.showDropDown();
            } else {
                circleBinding.queryCircleText.dismissDropDown();
            }
        });
    }

    private void getQuertTextAndSearch(){
        //获取搜索框的内容
        if (addCircleSearch){//如果添加了社团搜索
            if (!TextUtils.isEmpty(binding.queryText.getText().toString())
                    && !TextUtils.isEmpty(circleBinding.queryCircleText.getText().toString())){//如果原曲搜索框社团搜索框都不为空
                queryText = "Q1" + binding.queryText.getText().toString()
                        + "\nB1" + circleBinding.queryCircleText.getText().toString();
                //点击按钮时通常从0偏移量开始搜索，也就是从头开始
                startSearch("0");
            } else if (!TextUtils.isEmpty(binding.queryText.getText().toString())
                    && TextUtils.isEmpty(circleBinding.queryCircleText.getText().toString())) {//如果原曲搜索框不为空，社团搜索框为空
                queryText = "Q1" + binding.queryText.getText().toString();
                //点击按钮时通常从0偏移量开始搜索，也就是从头开始
                startSearch("0");
            } else if (TextUtils.isEmpty(binding.queryText.getText().toString())
                    && !TextUtils.isEmpty(circleBinding.queryCircleText.getText().toString())){//如果原曲搜索框为空，社团搜索框不为空
                queryText = "B1" + circleBinding.queryCircleText.getText().toString();
                //点击按钮时通常从0偏移量开始搜索，也就是从头开始
                startSearch("0");
            } else {
                Snackbar.make(binding.resultList, R.string.snack_input_empty, Snackbar.LENGTH_LONG).show();
            }
        } else {
            //如果压根就没加社团搜索
            if (!TextUtils.isEmpty(binding.queryText.getText().toString())){//如果原曲搜索框不为空
                queryText = "Q1" + binding.queryText.getText().toString();
                //点击按钮时通常从0偏移量开始搜索，也就是从头开始
                startSearch("0");
            } else {
                Snackbar.make(binding.resultList, R.string.snack_input_empty, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_about_app) {
            AboutDialogBinding about_binding = AboutDialogBinding.inflate(LayoutInflater.from(this), null, false);
            about_binding.versionName.setText(BuildConfig.VERSION_NAME);
            about_binding.legal.setOnClickListener(v ->
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle(R.string.about_view_legal)
                            .setMessage(R.string.legal_text)
                            .setPositiveButton(android.R.string.ok, null)
                            .show());
            about_binding.icon.setImageResource(R.mipmap.ic_launcher);
            new MaterialAlertDialogBuilder(this)
                    .setView(about_binding.getRoot())
                    .show();
        }
        return true;
    }

    private void startSearch(String offset) {
        Log.d(TAG, queryText);
        itemOffsetBackup = binding.itemCount.getSelectedItemPosition();
        if (!TextUtils.isEmpty(queryText)) {
            //显示加载弹窗
            MaterialAlertDialogBuilder loading_builder = new MaterialAlertDialogBuilder(this);
            loading_builder.setCancelable(false)
                    .setView(R.layout.dialog_loading);
            dialogs = loading_builder.show();
            queryResult(queryText, offset);
        } else {
            Snackbar.make(binding.resultList, R.string.snack_input_empty, Snackbar.LENGTH_LONG).show();
        }
    }

    private void loadResult(DataBean.ResuBean resuBean) {
        //一旦结果加载完成，就取消显示弹窗。
        dialogs.dismiss();
        isResultShowed = true;
        binding.resultList.setLayoutManager(new GridLayoutManager(this, 1));
        binding.resultList.setAdapter(new SongAdapter(resuBean));
        binding.pageCount.setText(getString(R.string.page_count_text, String.valueOf(current_page), String.valueOf(pageCount)));
        //如果有多页，就显示翻页按钮
        if (pageCount > 1) {
            //如果是第一页，就禁用上一页按钮
            if (current_page == 1) {
                binding.btnPriv.setEnabled(false);
                binding.btnNext.setEnabled(true);
            } else if (current_page == pageCount) {
                //如果是最后一页，就禁用下一页按钮
                binding.btnPriv.setEnabled(true);
                binding.btnNext.setEnabled(false);
            } else {
                //如果是中间页，就都启用
                binding.btnPriv.setEnabled(true);
                binding.btnNext.setEnabled(true);
            }
            //设置翻页按钮的监听器
            PageBtnListener listener = new PageBtnListener(pageCount);
            binding.btnPriv.setOnClickListener(listener);
            binding.btnNext.setOnClickListener(listener);
        } else {
            //如果只有一页，就禁用所有翻页按钮
            binding.btnPriv.setEnabled(false);
            binding.btnNext.setEnabled(false);
        }
    }

    private class PageBtnListener implements View.OnClickListener {
        private int pageCount;
        private static final int btn_priv = R.id.btn_priv;
        private static final int btn_next = R.id.btn_next;

        public PageBtnListener(int count) {
            this.pageCount = count;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == btn_priv) {
                //如果当前页数大于1，就可以翻页
                if (current_page > 1) {
                    if (offset <= 0) {
                        offset = 0;
                    }
                    offset -= itemOffset;
                    current_page--;
                }
                Log.d(TAG, "上一页的offset是" + offset);
            } else if (v.getId() == btn_next) {
                //如果当前页数小于总页数，就可以翻页
                if (current_page != pageCount) {
                    binding.btnPriv.setEnabled(true);
                    offset += itemOffset;
                    Log.d(TAG, "下一页的offset是" + offset);
                    current_page++;
                }
            }
            //更新页数显示
            binding.pageCount.setText(getString(R.string.page_count_text, String.valueOf(current_page), String.valueOf(pageCount)));
            //翻页操作本质也是重新搜索，只不过是在原来的基础上加上变更过的offset
            startSearch(String.valueOf(offset));
        }
    }

    //主要引导方法，该方法获取所有结果总数和token
    private void queryResult(String text, String offset) {
        RequestBody body = new FormBody.Builder()
                .add("action", "uask")
                .add("pre", "曲目")
                .add("query", text)
                .add("sort", "")
                .build();
        Request request = new Request.Builder()
                .url("https://thwiki.cc/api.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "request failed. reason:" + e);
                if (e.toString().contains("timeout")){
                    //超时
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "获取token超时了。", Toast.LENGTH_SHORT).show());
                } else {
                    //其他错误
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "获取token失败了。原因：\n"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                }
                dialogs.dismiss();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                //Log.d("Get Response",response.toString());
                try {
                    String body = response.body().string();
                    //Log.d(TAG,"GetBody: "+body);
                    //获得的总数和token传入给下一个方法
                    sendPost(body, text, offset);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //拿到token以后就直接去thb cd站请求结果
    private void sendPost(String text, String query, String offset) {
        totalResultCount = Integer.parseInt(getTokenAndData(text)[0]);
        String token = getTokenAndData(text)[1];
        if (token != null) {
            RequestBody body = new FormBody.Builder()
                    .add("action", "uask")
                    .add("pre", "曲目")
                    .add("query", query)
                    .add("result", "deBIWXYQfgTl")
                    .add("token", token)
                    .add("sort", "")
                    .add("order", "")
                    .add("limit", countId2int(binding.itemCount.getSelectedItemId()))
                    .add("offset", offset)
                    .build();
            Request request = new Request.Builder()
                    .url("https://thwiki.cc/api.php")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "request failed. reason:" + e);
                    runOnUiThread(() -> {
                        if (e.toString().contains("timeout")) {
                            Toast.makeText(MainActivity.this, "获取结果超时了。", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "获取结果失败了。", Toast.LENGTH_SHORT).show();
                        }
                        dialogs.dismiss();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    //Log.d("Get Response",response.toString());
                    try {
                        String body = response.body().string();
                        //Log.d(TAG,"GetBody: "+body);
                        //截取到json数据后通过Handler和Message发送到UI Thread
                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = body;
                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //获取输入框自动补全建议
    @NonNull
    private CompletableFuture<Boolean> getAutoCompleteData(String text, int searchModeInt) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String searchMode = "曲目原曲建议";
        if (addCircleSearch){
            if (searchModeInt == 0){
                searchMode = "曲目原曲建议";
            } else if (searchModeInt == 1){
                searchMode = "制作方建议";
            }
        }
        RequestBody body = new FormBody.Builder()
                .add("action", "inopt")
                .add("title", searchMode)
                .add("value", text)
                .build();
        Request request = new Request.Builder()
                .url("https://thwiki.cc/ajax.php")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to get auto complete text");
                //怎么会G呢？
                future.complete(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String body = response.body().string();
                    //Log.d(TAG,"Get AutoComplete Body: "+body);
                    //此处进行Jsoup解析html内容
                    Document doc = Jsoup.parse(body);
                    Elements lists = doc.getElementsByTag("div");
                    ArrayList<String> wordList = new ArrayList<>();
                    //遍历获得到的html element里的所有内容
                    for (Element element : lists) {
                        wordList.add(element.text());
                    }
                    //将获得的列表丢给非内部类
                    setAutoCompleteList(wordList,searchModeInt);
                    future.complete(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    future.complete(false);
                }
            }
        });
        return future;
    }

    //设置候选词列表
    private void setAutoCompleteList(ArrayList<String> list, int mode) {
        //发一个empty message通知handler干活了
        switch (mode){
            case 0:
                handler.sendEmptyMessage(3);
                songList = list;
                break;
            case 1:
                handler.sendEmptyMessage(4);
                circleList = list;
                break;
        }

    }

    //将spinner的position转换为对应的limit值
    private String countId2int(long position) {
        String itemLimit;
        switch ((int) position) {
            case 1:
                itemLimit = "10";
                break;
            case 2:
                itemLimit = "20";
                break;
            case 3:
                itemLimit = "25";
                break;
            case 4:
                itemLimit = "50";
                break;
            case 5:
                itemLimit = "100";
                break;
            case 0:
            default:
                itemLimit = "5";
                break;
        }
        return itemLimit;
    }

    //获取token和item总数
    private String[] getTokenAndData(String text) {
        String regex = "(\\d+)\\s+(.*)";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(text);
        if (m.find()) {
            String[] result = new String[2];
            result[0] = m.group(1);
            result[1] = m.group(2);
            Log.d(TAG, "Get Total result:" + result[0]
                    + ", Get token:" + result[1]);
            return result;
        }
        return new String[]{"0", ""};
    }

}