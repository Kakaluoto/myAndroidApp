package hy.work1;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import android.media.MediaMetadataRetriever;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;

/**
 * Created by 贺昱 on 2019/11/17.
 */

public class SongAdapter extends ArrayAdapter<Song> {
    private int resourceId;
    //private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    public SongAdapter(Context context, int textViewResourceId, List<Song> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Song song = getItem(position);
        View view;
        ViewHolder viewHolder;
        //Log.d("成功进入getView","Into_getView");
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            //viewHolder.addr = song.getAddress();
            viewHolder.picture = (ImageView) view.findViewById(R.id.picture);
            viewHolder.music = (TextView)view.findViewById(R.id.music);
            viewHolder.singer = (TextView)view.findViewById(R.id.singer);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        //setloadCover(viewHolder.addr,viewHolder.picture);
        viewHolder.picture.setImageResource(song.getImageId());
        viewHolder.music.setText(song.getMusicName());
        viewHolder.singer.setText(song.getSinger());
        //Log.d("展示音乐名",viewHolder.music.getText().toString());
        return view;
    }

    /*private void setloadCover(String path,ImageView image) {//J将给定路径的媒体封面取出，并显示在给定的view
        mediaMetadataRetriever.setDataSource(path);
        byte[] cover = mediaMetadataRetriever.getEmbeddedPicture();
        Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
        image.setImageBitmap(bitmap);    }*/

    class ViewHolder{
        ImageView picture;
        TextView music;
        TextView singer;
        //String addr;
    }
}
