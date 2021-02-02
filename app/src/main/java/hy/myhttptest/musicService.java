package hy.myhttptest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class musicService extends Service {

    private MediaPlayer mediaPlayer;
    private List<String> musicPath;
    private List<String> musicName;
    private List<String> artistName;
    private mediaBinder mBinder;
    private RemoteViews remoteViews;
    private Notification notification;
    private PendingIntent pendingIntent1;
    //private NotificationReceiver notificationReceiver；
    private int PosPath;
    private boolean isInit;
    private int mode_selector = 0;
    private final int ANT_PLAY = 0;//随机播放
    private final int CIRCLE_LIST = 1;//列表循环
    private final int CONTINUE_ONE = 2;//单曲循环
    private final int ONCEPLAY = 3;//单次播放

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mBinder = new mediaBinder();
        notification_init();
        NotifyReceiverInit();
        Log.d("进入服务", "创建服务");
        Log.d("播放器的地址onCreat", mediaPlayer.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Log.d("退出服务", "服务销毁");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("服务解绑", "服务解绑");
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        musicPath = intent.getStringArrayListExtra("Path");
        musicName = intent.getStringArrayListExtra("musicName");
        artistName = intent.getStringArrayListExtra("artistName");
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("进入播放完成监听当前播放状态", Integer.toString(mode_selector));
                mBinder.nextMusic();
            }
        });
        return mBinder;
    }

    public class mediaBinder extends Binder {
        public void playMusic() {
            if (!isInit) {
                Log.d("没有初始化过的", "先初始化初始化");
                Log.d("播放器的地址playmusic", mediaPlayer.toString());
                isInit = initMediaPlayer(PosPath);
                mediaPlayer.start();
            } else {
                Log.d("初始化过的", "不再初始化");
                Log.d("播放器的地址playmusic", mediaPlayer.toString());
                //initMediaPlayer(PosPath);
                mediaPlayer.start();
            }
        }

        public void pauseMusic() {
            if (mediaPlayer.isPlaying())
                mediaPlayer.pause();
            Log.d("播放器暂停", "进入pauseMusic");
        }

        public void nextMusic() {
            if (mediaPlayer != null && PosPath >= 0 && PosPath <= (musicPath.size() - 1)) {
                //mediaPlayer.reset();
                switch (mode_selector) {
                    case ANT_PLAY: {
                        Random rand = new Random();
                        PosPath = rand.nextInt(musicPath.size());//产生0到size-1的标号
                        Log.d("获取音乐数量SIZE", Integer.toString(musicPath.size()));
                        Log.d("这是第几首", Integer.toString(PosPath));
                        isInit = initMediaPlayer(PosPath);//更新初始化标志位
                        playMusic();
                    }
                    break;
                    case CIRCLE_LIST: {
                        if (PosPath == musicPath.size() - 1) {
                            PosPath = 0;
                        } else {
                            PosPath += 1;
                        }
                        Log.d("获取音乐数量SIZE", Integer.toString(musicPath.size()));
                        Log.d("这是第几首", Integer.toString(PosPath));
                        isInit = initMediaPlayer(PosPath);//更新初始化标志位
                        playMusic();
                    }
                    break;
                    case CONTINUE_ONE: {
                        PosPath = PosPath;
                        Log.d("获取音乐数量SIZE", Integer.toString(musicPath.size()));
                        Log.d("这是第几首", Integer.toString(PosPath));
                        isInit = initMediaPlayer(PosPath);//更新初始化标志位
                        playMusic();
                    }
                    break;
                    case ONCEPLAY: {
                    }
                    break;
                    default:
                        break;
                }


            }
        }

        public void previousMusic() {
            if (mediaPlayer != null && PosPath >= 0 && PosPath <= (musicPath.size() - 1)) {
                //mediaPlayer.reset();

                if (PosPath == 0) {
                    PosPath = musicPath.size() - 1;
                } else {
                    PosPath -= 1;
                }
                isInit = initMediaPlayer(PosPath);
                playMusic();
            }
        }

        public void killMusic() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                //mediaPlayer.release();
            }
        }

        public void setInitFalse() {
            isInit = false;
        }

        public int getTimeLong() {
            return mediaPlayer.getDuration();
        }

        public int getPlayPosition() {
            return mediaPlayer.getCurrentPosition();
        }

        public void seekToPosition(int ms) {
            mediaPlayer.seekTo(ms);
        }

        public void resetMusic() {
            mediaPlayer.reset();
        }

        public boolean isPlaying() {
            return mediaPlayer.isPlaying();
        }

        public boolean initMediaPlayer(int PosPath) {
            try {
                Log.d("进入初始化", "下一步是利用Remote更新UI");
                //remoteViews = new RemoteViews(getPackageName(),R.layout.notification_layout);
                remoteViews.setTextViewText(R.id.notify_music_information, musicName.get(PosPath));
                remoteViews.setTextViewText(R.id.notify_Artist_information, artistName.get(PosPath));
                reBuild_Start(remoteViews);
                Log.d("观察音乐名", musicName.get(PosPath));
                Log.d("观察艺术家", artistName.get(PosPath));
                Log.d("在媒体初始化中观察RemoteView地址", remoteViews.toString());
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicPath.get(PosPath));
                mediaPlayer.prepare();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public void setPosPath(int Pos) {
            PosPath = Pos;
        }

        public int getPosPath() {
            return PosPath;
        }


        public void setMode(int mode) {
            mode_selector = mode;
        }

        public int getMode() {
            return mode_selector;
        }

        public void playerGetPrepare_atPresent(int PosPath) {
            try {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(musicPath.get(PosPath));
                    mediaPlayer.prepare();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void notification_init() {
        //跳转意图

        //建立一个RemoteView的布局，并通过RemoteView加载这个布局
        //为remoteView设置文本
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        Log.d("在通知中观察RemoteView地址", remoteViews.toString());
        remoteViews.setTextViewText(R.id.notify_music_information, "Nice to meet you!");
        remoteViews.setTextViewText(R.id.notify_music_information, "Nice to meet");
        //设置PendingIntent
        Intent toSongList = new Intent(this, song_list.class);
        pendingIntent1 = PendingIntent.getActivity(this, 0, toSongList, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent play_or_not = new Intent("start_pause");
        PendingIntent start_pause = PendingIntent.getBroadcast(this, 1, play_or_not, 0);
        remoteViews.setOnClickPendingIntent(R.id.notify_start_pause, start_pause);

        Intent skip_to_next = new Intent("next");
        PendingIntent jump_to_next = PendingIntent.getBroadcast(this, 2, skip_to_next, 0);
        remoteViews.setOnClickPendingIntent(R.id.notify_skip_next, jump_to_next);

        Intent skip_to_previous = new Intent("previous");
        PendingIntent jump_to_previous = PendingIntent.getBroadcast(this, 3, skip_to_previous, 0);
        remoteViews.setOnClickPendingIntent(R.id.notify_skip_previous, jump_to_previous);


        notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(false)
                .setCustomContentView(remoteViews) //将RemoteView作为Notification的布局
                .setContentIntent(pendingIntent1)//将pendingIntent1作为Notification的intent，这样当点击其他部分时，也能实现跳转
                .build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String Control_Code = intent.getAction();
            switch (Control_Code) {
                case "start_pause":
                    if (mBinder.isPlaying()) {//暂停被按下，切换图片成播放，并暂停音乐
                        remoteViews.setImageViewResource(R.id.notify_start_pause, R.mipmap.start_round);
                        reBuild_Start(remoteViews);
                        mBinder.pauseMusic();
                    } else if (!mBinder.isPlaying()) {
                        remoteViews.setImageViewResource(R.id.notify_start_pause, R.mipmap.pause_round);
                        reBuild_Start(remoteViews);
                        mBinder.playMusic();
                    }
                    break;
                case "next":
                    mBinder.nextMusic();
                    break;
                case "previous":
                    mBinder.previousMusic();
                    break;
                default:
                    Log.d("广播没有收到,打印Action内容", Control_Code);
                    break;

            }
        }
    }

    public void NotifyReceiverInit() {
        IntentFilter intentfilter = new IntentFilter();
        NotificationReceiver notificationReceiver;
        notificationReceiver = new musicService.NotificationReceiver();
        intentfilter.addAction("start_pause");
        intentfilter.addAction("next");
        intentfilter.addAction("previous");
        registerReceiver(notificationReceiver, intentfilter);
    }

    public void reBuild_Start(RemoteViews newRemoteView) {
        notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(false)
                .setCustomContentView(newRemoteView) //将RemoteView作为Notification的布局
                .setContentIntent(pendingIntent1)//将pendingIntent1作为Notification的intent，这样当点击其他部分时，也能实现跳转
                .build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);
    }


}
