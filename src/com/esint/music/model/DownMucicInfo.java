package com.esint.music.model;

public class DownMucicInfo {

	private String downMusicName;
	private String downMusicArtist;
	private String downMusicUrl;
	private String downMusicSize;
	private String downMusicPicUrl;
	private String downMusicDuration;
	private int downMusicId;
	private String myLikeDownMusicName;

	public DownMucicInfo() {
		super();
	}

	public DownMucicInfo(String downMusicPicUrl) {
		super();
		this.downMusicPicUrl = downMusicPicUrl;
	}

	public String getDownMusicName() {
		return downMusicName;
	}

	public void setDownMusicName(String downMusicName) {
		this.downMusicName = downMusicName;
	}

	public String getDownMusicArtist() {
		return downMusicArtist;
	}

	public void setDownMusicArtist(String downMusicArtist) {
		this.downMusicArtist = downMusicArtist;
	}

	public String getDownMusicUrl() {
		return downMusicUrl;
	}

	public void setDownMusicUrl(String downMusicUrl) {
		this.downMusicUrl = downMusicUrl;
	}

	public String getDownMusicSize() {
		return downMusicSize;
	}

	public void setDownMusicSize(String downMusicSize) {
		this.downMusicSize = downMusicSize;
	}

	public String getDownMusicPicUrl() {
		return downMusicPicUrl;
	}

	public void setDownMusicPicUrl(String downMusicPicUrl) {
		this.downMusicPicUrl = downMusicPicUrl;
	}

	public String getDownMusicDuration() {
		return downMusicDuration;
	}

	public void setDownMusicDuration(String downMusicDuration) {
		this.downMusicDuration = downMusicDuration;
	}

	public int getDownMusicId() {
		return downMusicId;
	}

	public void setDownMusicId(int downMusicId) {
		this.downMusicId = downMusicId;
	}

	public String getMyLikeDownMusicName() {
		return myLikeDownMusicName;
	}

	public void setMyLikeDownMusicName(String myLikeDownMusicName) {
		this.myLikeDownMusicName = myLikeDownMusicName;
	}

}
