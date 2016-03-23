package com.esint.music.model;

/**   
* �����ƣ�OriginalMusicInfo   
* �������� ���������bean  
* �����ˣ�bai   
* ����ʱ�䣺2016-3-23 ����10:43:37         
*/
public class RiseMusicInfo {

	private String updateTime;// �赥����ʱ��
	private String description;// ���Ƽ����������������ƷΪĿ�ġ�ÿ���������������׷�����������ʱ��������ʱ����15��֪��ƽ��ý�����ýͬ���������������������ˣ�
								// http://music.163.com/recruit",
	private String mp3Url;// �������ӵ�ַ
	private String name;// ��������
	private String duration;// ����ʱ��
	private String alias;// ԭ��
	private String artistsName;// ��������
	private String albumName;// ר������ ���Ǹ��ֵ��ļ� ������
	private String trackCount;// ��������
	private String picUrl;// ͼƬ�ĵ�ַ

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
