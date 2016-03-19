package com.esint.music.model;

import android.graphics.Bitmap;

/**   
* 类名称：LikeMusicModel   
* 类描述： 插入数据库喜欢音乐的bean文件  
* 创建人：bai   
* 创建时间：2016-3-18 上午11:55:31         
*/
public class LikeMusicModel {

	private String musicName;
	private String musicArtist;
	private Long musicTime;
	private String musicURL;// 音乐的播放地址
	private byte[] musicLocalImg;// 本地音乐转换成字节数组后的地址
	private Bitmap bitmap;

	public LikeMusicModel() {
		super();
	}

	public LikeMusicModel(String musicName, String musicArtist, Long musicTime,
			String musicURL) {
		super();
		this.musicName = musicName;
		this.musicArtist = musicArtist;
		this.musicTime = musicTime;
		this.musicURL = musicURL;
	}

	public String getMusicName() {
		return musicName;
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getMusicArtist() {
		return musicArtist;
	}

	public void setMusicArtist(String musicArtist) {
		this.musicArtist = musicArtist;
	}

	public Long getMusicTime() {
		return musicTime;
	}

	public void setMusicTime(Long musicTime) {
		this.musicTime = musicTime;
	}

	public String getMusicURL() {
		return musicURL;
	}

	public void setMusicURL(String musicURL) {
		this.musicURL = musicURL;
	}

	public byte[] getMusicLocalImg() {
		return musicLocalImg;
	}

	public void setMusicLocalImg(byte[] musicLocalImg2) {
		this.musicLocalImg = musicLocalImg2;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	@Override
	public String toString() {
		return "LikeMusicModel [musicName=" + musicName + ", musicArtist="
				+ musicArtist + ", musicTime=" + musicTime + ", musicURL="
				+ musicURL + "]";
	}
}
