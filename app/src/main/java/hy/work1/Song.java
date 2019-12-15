package hy.work1;

/**
 * Created by 贺昱 on 2019/11/17.
 */

public class Song {
    private String musicName;
    private String Singer;
    private String address;
    private int imageId;
    public Song(String musicName,String Singer,String address,int imageId){
        this.musicName = musicName;
        this.Singer = Singer;
        this.address = address;
        this.imageId = imageId;
    }
    public String getMusicName(){
        return musicName;
    }
    public String getSinger(){
        return Singer;
    }
    public String getAddress(){ return address;}
    public int getImageId(){
        return imageId;
    }
}
