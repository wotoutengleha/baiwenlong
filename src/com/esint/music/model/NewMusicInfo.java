package com.esint.music.model;

public class NewMusicInfo {

	private String updateTime;// �赥����ʱ��
	private String description;// "�������¸���������û�һ�������������¸裨һ�������·��У� �ٷ�TOP���а�ÿ����¡�",
	private String mp3Url;// �������ӵ�ַ
	private String name;// ��������
	private String duration;// ����ʱ��
	private String alias;// ԭ��
	private String artistsName;// ��������
	private String albumName;// ר������ ���Ǹ��ֵ��ļ� ������
	private String trackCount;// ��������
	private String picUrl;// ͼƬ�ĵ�ַ

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
