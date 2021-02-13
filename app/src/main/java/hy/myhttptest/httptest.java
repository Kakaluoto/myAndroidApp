package hy.myhttptest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class httptest extends BaseActivity implements View.OnClickListener {
    private TextView responseText;
    private TextView loginStatus;
    private Button back;
    private Button verify;
    private Button upload;
    private Button reset_password;
    private EditText webAccount;
    private EditText webPassword;
    private EditText nickname;
    private EditText postCode;
    private EditText msgtoPost;
    private RadioGroup moodSelect;

    private void iniView() {
        back = (Button) findViewById(R.id.title_back);
        verify = (Button) findViewById(R.id.verify);
        upload = (Button) findViewById(R.id.upload);
        reset_password = (Button) findViewById(R.id.reset_password);
        webAccount = (EditText) findViewById(R.id.webAccount);
        webPassword = (EditText) findViewById(R.id.webPassword);
        responseText = (TextView) findViewById(R.id.response_text);
        loginStatus = (TextView) findViewById(R.id.loginStatus);
        moodSelect = (RadioGroup) findViewById(R.id.mood);
        nickname = (EditText) findViewById(R.id.nickname);
        postCode = (EditText) findViewById(R.id.postCode);
        msgtoPost = (EditText) findViewById(R.id.msg_to_post);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_httptest);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }
        iniView();
        back.setOnClickListener(this);
        verify.setOnClickListener(this);
        upload.setOnClickListener(this);
        reset_password.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                finish();
                break;
            case R.id.verify:
                RequestBody requestBodyLogin = new FormBody.Builder()
                        .add("user_id", webAccount.getText().toString())
                        .add("user_pass", webPassword.getText().toString())
                        .build();
                HttpUtil.sendOkHttpRequest("http://hyofficial.xyz/verify/", requestBodyLogin, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        showResponse("错误信息" + e.toString());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseData = response.body().string();
                        authenticate(responseData,"verify");
                        showResponse(responseData);
                    }
                });
                break;
            case R.id.upload:
                HashMap<String, String> params = getFormParams();
                RequestBody requestBodyForPost = new FormBody.Builder()
                        .add("user_id", params.get("user_id"))
                        .add("user_pass", params.get("user_pass"))
                        .add("mood", params.get("mood"))
                        .add("user_post", params.get("user_post"))
                        .build();
                HttpUtil.sendOkHttpRequest("http://hyofficial.xyz/article/", requestBodyForPost, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        showResponse("错误信息" + e.toString());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseData = response.body().string();
                        authenticate(responseData,"upload");
                        showResponse(responseData);
                    }
                });
                break;
            case R.id.reset_password:
                webAccount.setText("");
                webPassword.setText("");
                break;
            default:
                break;

        }
    }

    private void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //在这里进行UI操作，将结果显示到界面上
                responseText.setText(response);
            }
        });
    }

    private void authenticate(final String response, String method) {
        if(method.equals("verify")){
            Document htmlResponse = Jsoup.parse(response);
            Elements elements = htmlResponse.getElementsByClass("login_status");
            Element meta_element = elements.first();
            String status = meta_element.attr("content");
            if (status.equals("YES")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //在这里进行UI操作，将结果显示到界面上
                        loginStatus.setText("已登录");
                    }
                });
            }else {
                Looper.prepare();
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }else if(method.equals("upload")){
            Document htmlResponse = Jsoup.parse(response);
            Elements elements = htmlResponse.getElementsByClass("post_status");
            Element div_element = elements.first();
            String status = div_element.attr("content");
            if (status.equals("YES")){
                Looper.prepare();
                Toast.makeText(this, "提交成功", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }else {
                Looper.prepare();
                Toast.makeText(this, "提交失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

        }else {
            showResponse("错误参数请求");
        }


    }

    private HashMap<String, String> getFormParams(){
         HashMap<String, String> paramList = new HashMap<>();
        for (int i = 0; i < moodSelect.getChildCount(); i++) {
            RadioButton mood = (RadioButton) moodSelect.getChildAt(i);
            if (mood.isChecked()) {
                paramList.put("mood",mood.getText().toString());
                break;
            }
        }
        paramList.put("user_id",nickname.getText().toString());
        paramList.put("user_pass",postCode.getText().toString());
        paramList.put("user_post",msgtoPost.getText().toString());
        return paramList;
    }

}
