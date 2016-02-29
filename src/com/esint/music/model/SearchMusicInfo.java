package com.esint.music.model;

/**
 * �����ƣ�SearchMusicInfo ���������������������������bean �����ˣ�bai ����ʱ�䣺2016-2-28 ����3:10:45
 */
public class SearchMusicInfo {

	private String musicID;// ���ֵ�ID
	private String musicName;// ���ֵ�����
	private String musicArtist;// ���ֵĸ質��

	public SearchMusicInfo() {
		super();
	}

	public SearchMusicInfo(String musicID, String musicName, String musicArtist) {
		super();
		this.musicID = musicID;
		this.musicName = musicName;
		this.musicArtist = musicArtist;
	}

	public String getMusicID() {
		return musicID;
	}

	public void setMusicID(String musicID) {
		this.musicID = musicID;
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

	@Override
	public String toString() {
		return "SearchMusicInfo [musicID=" + musicID + ", musicName="
				+ musicName + ", musicArtist=" + musicArtist + "]";
	}

}
