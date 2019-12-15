package hy.work1;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class musicService extends Service {

    private MediaPlayer mediaPlayer;
    private List<String> musicPath;
    private mediaBinder mBinder;
    private int PosPath;
    private boolean isInit;

   @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mBinder = new mediaBinder();
       Log.d("进入服务", "创建服务");
       Log.d("播放器的地址onCreat",mediaPlayer.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null){
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
        return mBinder;
    }

    public class mediaBinder extends Binder{
        public void playMusic() {
            if (!isInit) {
                Log.d("没有初始化过的", "先初始化初始化");
                Log.d("播放器的地址playmusic",mediaPlayer.toString());
                isInit = initMediaPlayer(PosPath);
                mediaPlayer.start();
            } else {
                Log.d("初始化过的", "不再初始化");
                Log.d("播放器的地址playmusic",mediaPlayer.toString());
                //initMediaPlayer(PosPath);
                mediaPlayer.start();
            }
        }

        public void pauseMusic(){
            if(mediaPlayer.isPlaying())
                mediaPlayer.pause();
                Log.d("播放器暂停","进入pauseMusic");
        }

        public void nextMusic(){
            if(mediaPlayer!=null&&PosPath>=0&&PosPath<musicPath.size()){
                //mediaPlayer.reset();
                if(PosPath>musicPath.size()){
                    PosPath = 0;
                }else{
                    PosPath += 1;
                }
                isInit = initMediaPlayer(PosPath);//更新初始化标志位
                playMusic();
            }
        }

        public void previousMusic(){
            if(mediaPlayer!=null&&PosPath>=0&&PosPath<musicPath.size()){
                //mediaPlayer.reset();

                if(PosPath==0){
                    PosPath = musicPath.size();
                }else{
                    PosPath -= 1;
                }
                isInit = initMediaPlayer(PosPath);
                playMusic();
            }
        }

        public void killMusic(){
            if(mediaPlayer!=null){
                mediaPlayer.stop();
                //mediaPlayer.release();
            }
        }

        public void setInitFalse(){
            isInit = false;
        }

        public int getTimeLong(){
            return mediaPlayer.getDuration();
        }

        public int getPlayPosition(){
            return mediaPlayer.getCurrentPosition();
        }

        public void seekToPosition(int ms){
            mediaPlayer.seekTo(ms);
        }

        public void resetMusic(){
            mediaPlayer.reset();
        }

        public boolean isPlaying(){
           return mediaPlayer.isPlaying();
        }

        public boolean initMediaPlayer(int PosPath){
            try{
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicPath.get(PosPath));
                mediaPlayer.prepare();
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        public void setPosPath(int Pos){
            PosPath = Pos;
        }

        public int getPosPath(){
            return PosPath;
        }

    }


}
