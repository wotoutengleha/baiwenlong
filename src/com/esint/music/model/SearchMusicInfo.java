package com.esint.music.model;

/**
 * 类名称：SearchMusicInfo 类描述：网易云音乐搜索结果的bean 创建人：bai 创建时间：2016-2-28 下午3:10:45
 */
public class SearchMusicInfo {

	private String musicID;// 音乐的ID
	private String musicName;// 音乐的名字
	private String musicArtist;// 音乐的歌唱家

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
