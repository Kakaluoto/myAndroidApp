package hy.work1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaMetadataRetriever;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.List;

public class song_list extends AppCompatActivity implements View.OnClickListener{

    private List<Song> songList = new ArrayList<>();
    //private MediaPlayer mediaPlayer = new MediaPlayer();
    private ImageView start_pause;
    private Button back;
    private ImageView stop_play;
    private ImageView next_music;
    private ImageView previous_music;
    private TextView welcome;
    //private boolean isInit;
    Intent toMediaService;
    private musicService.mediaBinder mediaBinder;
    private ArrayList<String> musicPath = new ArrayList<>();
    //private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

    SongAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        ActionBar actionbar = getSupportActionBar();
        if(actionbar!=null){
            actionbar.hide();
        }
        Intent name = getIntent();
        String text = name.getStringExtra("name");

        iniView();

        toMediaService = new Intent(this,musicService.class);

        back.setOnClickListener(this);
        start_pause.setOnClickListener(this);
        stop_play.setOnClickListener(this);
        next_music.setOnClickListener(this);
        previous_music.setOnClickListener(this);
        welcome.setText("欢迎您,"+text+"!");
        //initSongs();
        adapter = new SongAdapter(song_list.this,R.layout.song_item,songList);
        ListView listView = (ListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            readSongs();
            toMediaService.putStringArrayListExtra("Path",musicPath);
            bindService(toMediaService,connection,BIND_AUTO_CREATE);
            startService(toMediaService);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song musicAddr = songList.get(position);
                //mediaBinder.resetMusic();
                mediaBinder.setInitFalse();//消除初始化标志，让服务播放的时候重新初始化，音乐从头播放
                mediaBinder.setPosPath(position);
                mediaBinder.playMusic();
                start_pause.setImageResource(R.mipmap.pause_round);//进入播放状态，更新图标
                Toast.makeText(song_list.this,musicAddr.getAddress(),Toast.LENGTH_SHORT).show();
            }
        });
        /*mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
               mediaBinder.killMusic();
            }
        });*/
    }
    /*
    private boolean initMediaPlayer(String mediaPath){
        try{
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mediaPath);
            mediaPlayer.prepare();
            return true;//用来返回值判断是否成功初始化
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back :
                finish();
                break;
            case R.id.start_pause :
                if(mediaBinder.isPlaying()){//暂停被按下，切换图片成播放，并暂停音乐
                    start_pause.setImageResource(R.mipmap.start_round);
                    Log.d("暂停被按下，切换图片成播放，并暂停音乐","进入前一个分支");
                    mediaBinder.pauseMusic();
                }else if(!mediaBinder.isPlaying()){
                    start_pause.setImageResource(R.mipmap.pause_round);
                    Log.d("播放被按下，切换图片成暂停，并播放音乐","进入后一个分支");
                    mediaBinder.playMusic();
                }
                break;
            case R.id.stop :
                mediaBinder.killMusic();
                start_pause.setImageResource(R.mipmap.start_round);
                break;
            case R.id.skip_next :
                mediaBinder.nextMusic();
                break;
            case R.id.skip_previous :
                mediaBinder.previousMusic();
                break;
            default:
                break;

        }
    }

    private void readSongs(){
        Cursor cursor = null;
        try {
           cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                   null,null,null,null);
           if(cursor!=null){
               while (cursor.moveToNext()){
                   String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                   String addr = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                   String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                   musicPath.add(addr);
                   songList.add(new Song(title,artist,addr,R.drawable.untitled));
               }
               adapter.notifyDataSetChanged();
           }
        }catch (Exception e){
           e.printStackTrace();
        }finally {
           if(cursor!=null){
               cursor.close();
           }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1 :
                 if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    readSongs();
                 }else{
                     Toast.makeText(this,"你拒绝了读取音乐",Toast.LENGTH_SHORT).show();
                 } break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            unbindService(connection);
    }

    private void iniView(){
        welcome = (TextView)findViewById(R.id.welcome);
        start_pause = (ImageView)findViewById(R.id.start_pause);
        back = (Button)findViewById(R.id.title_back);
        stop_play = (ImageView)findViewById(R.id.stop);
        next_music = (ImageView)findViewById(R.id.skip_next);
        previous_music = (ImageView)findViewById(R.id.skip_previous);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           mediaBinder = (musicService.mediaBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(song_list.this,"后台服务已断开",Toast.LENGTH_SHORT).show();
        }
    };

    /*
    private void setloadCover(String path,ImageView image) {//获取专辑封面
        mediaMetadataRetriever.setDataSource(path);
        byte[] cover = mediaMetadataRetriever.getEmbeddedPicture();
        Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
        image.setImageBitmap(bitmap);    }
    */

}
