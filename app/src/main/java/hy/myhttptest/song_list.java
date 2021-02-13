package hy.myhttptest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaMetadataRetriever;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.List;

public class song_list extends BaseActivity implements View.OnClickListener {

    private List<Song> songList = new ArrayList<>();
    //private MediaPlayer mediaPlayer = new MediaPlayer();
    private ImageView start_pause;
    private Button back;
    private ImageView mode_change;
    private ImageView next_music;
    private ImageView previous_music;
    private ImageView to_http_test;
    private TextView welcome;
    private TextView full_time;
    private TextView present_time;
    private Handler myHandler = new Handler();
    private SeekBar mySeekBar;
    private song_list.HeadPhoneReceiver headPhoneReceiver;

    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    //private boolean isInit;
    Intent toMediaService;
    private musicService.mediaBinder mediaBinder;
    private ArrayList<String> musicPath = new ArrayList<>();
    private ArrayList<String> musicName = new ArrayList<>();
    private ArrayList<String> artistName = new ArrayList<>();
    //private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    private int changeOrder = 0;//模式更换顺序
    private final int ANT_PLAY = 0;//随机播放
    private final int CIRCLE_LIST = 1;//列表循环
    private final int CONTINUE_ONE = 2;//单曲循环
    private final int ONCEPLAY = 3;//单次播放
    SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }
        Intent name = getIntent();
        String text = name.getStringExtra("name");

        iniView();

        toMediaService = new Intent(this, musicService.class);

        back.setOnClickListener(this);
        start_pause.setOnClickListener(this);
        mode_change.setOnClickListener(this);
        next_music.setOnClickListener(this);
        previous_music.setOnClickListener(this);
        to_http_test.setOnClickListener(this);
        welcome.setText("欢迎您," + text + "!");
        //initSongs();
        adapter = new SongAdapter(song_list.this, R.layout.song_item, songList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        if (ContextCompat.checkSelfPermission(this,//运行时权限申请
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);//进行申请
        } else {
            readSongs();//第一次打开活动不会被执行，因为没有权限
            toMediaService.putStringArrayListExtra("Path", musicPath);
            toMediaService.putStringArrayListExtra("musicName", musicName);
            toMediaService.putStringArrayListExtra("artistName", artistName);
            Log.d("接下来是建立服务", "已经获得权限，这时第二次开启活动");
            bindService(toMediaService, connection, BIND_AUTO_CREATE);
            startService(toMediaService);
        }
        /*toMediaService.putStringArrayListExtra("Path",musicPath);
        toMediaService.putStringArrayListExtra("musicName",musicName);
        toMediaService.putStringArrayListExtra("artistName",artistName);
        Log.d("接下来是建立服务","接下来是建立服务");
        bindService(toMediaService,connection,BIND_AUTO_CREATE);
        startService(toMediaService);*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song musicAddr = songList.get(position);
                //mediaBinder.resetMusic();
                mediaBinder.setInitFalse();//消除初始化标志，让服务播放的时候重新初始化，音乐从头播放
                mediaBinder.setPosPath(position);
                mediaBinder.playMusic();
                start_pause.setImageResource(R.mipmap.pause_round);//进入播放状态，更新图标
                Toast.makeText(song_list.this, musicAddr.getAddress(), Toast.LENGTH_SHORT).show();
            }
        });
        /*mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
               mediaBinder.killMusic();
            }
        });*/
        IntentFilter intentfilter;

        intentfilter = new IntentFilter();
        intentfilter.addAction("android.intent.action.HEADSET_PLUG");
        headPhoneReceiver = new song_list.HeadPhoneReceiver();
        registerReceiver(headPhoneReceiver, intentfilter);
    }

    class HeadPhoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (0 == intent.getIntExtra("state", 0)) {
                    Toast.makeText(context, "耳机已断开", Toast.LENGTH_SHORT).show();
                    if (mediaBinder != null) {
                        mediaBinder.pauseMusic();
                    }
                } else if (1 == intent.getIntExtra("state", 0)) {
                    Toast.makeText(context, "耳机已连接", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                finish();
                break;
            case R.id.start_pause:
                if (mediaBinder.isPlaying()) {//暂停被按下，切换图片成播放，并暂停音乐
                    start_pause.setImageResource(R.mipmap.start_round);
                    Log.d("暂停被按下，切换图片成播放，并暂停音乐", "进入前一个分支");
                    mediaBinder.pauseMusic();
                } else if (!mediaBinder.isPlaying()) {
                    start_pause.setImageResource(R.mipmap.pause_round);
                    Log.d("播放被按下，切换图片成暂停，并播放音乐", "进入后一个分支");
                    mediaBinder.playMusic();
                }
                break;
            case R.id.Mode:
                mode_state_change();
                break;
            case R.id.skip_next:
                mediaBinder.nextMusic();
                break;
            case R.id.skip_previous:
                mediaBinder.previousMusic();
                break;
            case R.id.list_to_play:
                Intent toHttpTest = new Intent(song_list.this, httptest.class);
                startActivity(toHttpTest);
                break;
            default:
                break;

        }
    }

    private void readSongs() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String addr = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    if (title.equals("tmp")) {
                        //因为总是会读取到tmp的媒体，又不能播放，简单过滤掉
                    } else {
                        musicPath.add(addr);
                        musicName.add(title);
                        artistName.add(artist);
                        songList.add(new Song(title, artist, addr, R.drawable.yinfu));
                    }
                }
                adapter.notifyDataSetChanged();//更新listview
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//权限获取成功
                    readSongs();
                    toMediaService.putStringArrayListExtra("Path", musicPath);
                    toMediaService.putStringArrayListExtra("musicName", musicName);
                    toMediaService.putStringArrayListExtra("artistName", artistName);
                    Log.d("接下来是建立服务", "此前未获取权限，这时第一次开启活动");
                    bindService(toMediaService, connection, BIND_AUTO_CREATE);//启动服务顺便发送歌曲路径
                    startService(toMediaService);
                } else {
                    Toast.makeText(this, "你拒绝了读取音乐", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacks(myRunnable);
        Log.d("活动销毁", "循环回调终止");
        unbindService(connection);
        ActivityCtrl.finishAll();

    }

    private void iniView() {
        welcome = (TextView) findViewById(R.id.welcome);
        start_pause = (ImageView) findViewById(R.id.start_pause);
        back = (Button) findViewById(R.id.title_back);
        mode_change = (ImageView) findViewById(R.id.Mode);
        next_music = (ImageView) findViewById(R.id.skip_next);
        previous_music = (ImageView) findViewById(R.id.skip_previous);
        mySeekBar = (SeekBar) findViewById(R.id.my_seek_bar);
        full_time = (TextView) findViewById(R.id.full_time);
        present_time = (TextView) findViewById(R.id.present_time);
        to_http_test = (ImageView) findViewById(R.id.list_to_play);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaBinder = (musicService.mediaBinder) service;
            Log.d("获取到Binder地址", mediaBinder.toString());
            seek_bar_init();
            mediaBinder.setMode(CIRCLE_LIST);
            myHandler.post(myRunnable);//开启回调循环，每次都会被开启
            Log.d("绑定连接", "循环回调启动");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(song_list.this, "后台服务已断开", Toast.LENGTH_SHORT).show();
        }
    };

    public void seek_bar_init() {
        mediaBinder.playerGetPrepare_atPresent(mediaBinder.getPosPath());
        //因为获取总时长必须要播放器准备好，以防万一先让播放器准备好
        mySeekBar.setMax(mediaBinder.getTimeLong());
        mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaBinder.seekToPosition(mySeekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /*
    private void setloadCover(String path,ImageView image) {//获取专辑封面
        mediaMetadataRetriever.setDataSource(path);
        byte[] cover = mediaMetadataRetriever.getEmbeddedPicture();
        Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
        image.setImageBitmap(bitmap);    }
    */

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            //切歌之后默认对播放器进行了初始化，所以播放器是准备好的，可以获取时长
            mySeekBar.setMax(mediaBinder.getTimeLong());
            mySeekBar.setProgress(mediaBinder.getPlayPosition());
            present_time.setText(time.format(mediaBinder.getPlayPosition()));
            full_time.setText(time.format(mediaBinder.getTimeLong()));
            myHandler.postDelayed(myRunnable, 1000);
        }
    };

    public void mode_state_change() {
        changeOrder = (changeOrder + 1) % 4;
        switch (changeOrder) {
            case ANT_PLAY: {//随机播放
                mode_change.setImageResource(R.drawable.any_play);
                mediaBinder.setMode(ANT_PLAY);
            }
            break;
            case CIRCLE_LIST: {//列表循环
                mode_change.setImageResource(R.drawable.circle_list);
                mediaBinder.setMode(CIRCLE_LIST);
            }
            break;
            case CONTINUE_ONE: {//单曲循环
                mode_change.setImageResource(R.drawable.continue_one);
                mediaBinder.setMode(CONTINUE_ONE);
            }
            break;
            case ONCEPLAY: {//单次播放
                mode_change.setImageResource(R.drawable.onceplay);
                mediaBinder.setMode(ONCEPLAY);
            }
            break;
            default: {
                mode_change.setImageResource(R.drawable.circle_list);
                mediaBinder.setMode(CIRCLE_LIST);
            }
            break;
        }
    }

}
