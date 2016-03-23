package com.esint.music.model;

/**   
* 类名称：OriginalMusicInfo   
* 类描述： 飙升歌曲的bean  
* 创建人：bai   
* 创建时间：2016-3-23 上午10:43:37         
*/
public class RiseMusicInfo {

	private String updateTime;// 歌单更新时间
	private String description;// 以推荐优秀独立音乐人作品为目的。每周四网易云音乐首发。联合青年时报，京华时报等15家知名平面媒体和网媒同步发布。申请网易音乐人：
								// http://music.163.com/recruit",
	private String mp3Url;// 歌曲链接地址
	private String name;// 歌曲名字
	private String duration;// 歌曲时间
	private String alias;// 原唱
	private String artistsName;// 歌手名字
	private String albumName;// 专辑名字 我是歌手第四季 第四期
	private String trackCount;// 歌曲总数
	private String picUrl;// 图片的地址

	public RiseMusicInfo() {
		super();
	}


	public RiseMusicInfo(String mp3Url, String name, String duration,
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

	public String getTrackCount() {
		return trackCount;
	}

	public void setTrackCount(String trackCount) {
		this.trackCount = trackCount;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

}
