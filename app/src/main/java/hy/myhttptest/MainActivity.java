package hy.myhttptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private EditText account;
    private EditText password;
    private Button back;
    private Button login;
    //private IntentFilter intentfilter;
    private HeadPhoneReceiver headPhoneReceiver;
    private SharedPreferences pref;
    //private SharedPreferences.Editor editor;
    private CheckBox remember_name_check;

    @Override
    public void onClick(View v) {
        String account_name = account.getText().toString();
        SharedPreferences.Editor editor;
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.login:
                if (password.getText().toString().equals("123456")) {
                    editor = pref.edit();
                    if (remember_name_check.isChecked()) {
                        editor.putBoolean("remember_name", true);
                        editor.putString("account", account_name);
                    } else {
                        editor.clear();
                    }
                    editor.apply();
                    Intent toList = new Intent(MainActivity.this, song_list.class);
                    toList.putExtra("name", account.getText().toString());
                    startActivity(toList);
                } else {
                    Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        IntentFilter intentfilter;

        intentfilter = new IntentFilter();
        intentfilter.addAction("android.intent.action.HEADSET_PLUG");
        headPhoneReceiver = new HeadPhoneReceiver();
        registerReceiver(headPhoneReceiver, intentfilter);

        iniView();

        back.setOnClickListener(this);
        login.setOnClickListener(this);

        boolean isRemember = pref.getBoolean("remember_name", false);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }
        if (isRemember) {
            String name = pref.getString("account", "");
            account.setText(name);
            remember_name_check.setChecked(true);
        }

    }


    class HeadPhoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (0 == intent.getIntExtra("state", 0)) {
                    Toast.makeText(context, "耳机已断开", Toast.LENGTH_SHORT).show();
                } else if (1 == intent.getIntExtra("state", 0)) {
                    Toast.makeText(context, "耳机已连接", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(headPhoneReceiver);
    }

    private void iniView() {
        remember_name_check = (CheckBox) findViewById(R.id.remember_name_check);
        back = (Button) findViewById(R.id.back);
        login = (Button) findViewById(R.id.login);
        account = (EditText) findViewById(R.id.account);
        password = (EditText) findViewById(R.id.Password);
    }


}
