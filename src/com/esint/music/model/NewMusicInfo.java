package com.esint.music.model;

public class NewMusicInfo {

	private String updateTime;// 歌单更新时间
	private String description;// "云音乐新歌榜：云音乐用户一周内收听所有新歌（一月内最新发行） 官方TOP排行榜，每天更新。",
	private String mp3Url;// 歌曲链接地址
	private String name;// 歌曲名字
	private String duration;// 歌曲时间
	private String alias;// 原唱
	private String artistsName;// 歌手名字
	private String albumName;// 专辑名字 我是歌手第四季 第四期
	private String trackCount;// 歌曲总数
	private String picUrl;// 图片的地址

	public NewMusicInfo(String mp3Url, String name, String duration,
			String alias, String artistsName, String albumName,
			String trackCount, String picUrl) {
		super();
		this.mp3Url = mp3Url;
		this.name = name;
		this.duration = duration;
		this.alias = alias;
		this.artistsName = artistsName;
		this.albumName = albumName;
		this.trackCount = trackCount;
		this.picUrl = picUrl;
	}

	public NewMusicInfo() {
		super();
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getTrackCount() {
		return trackCount;
	}

	public void setTrackCount(String trackCount) {
		this.trackCount = trackCount;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMp3Url() {
		return mp3Url;
	}

	public void setMp3Url(String mp3Url) {
		this.mp3Url = mp3Url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getArtistsName() {
		return artistsName;
	}

	public void setArtistsName(String artistsName) {
		this.artistsName = artistsName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	@Override
	public String toString() {
		return "NetNewMusicInfo [updateTime=" + updateTime + ", description="
				+ description + ", mp3Url=" + mp3Url + ", name=" + name
				+ ", duration=" + duration + ", alias=" + alias
				+ ", artistsName=" + artistsName + ", albumName=" + albumName
				+ "]";
	}

}
