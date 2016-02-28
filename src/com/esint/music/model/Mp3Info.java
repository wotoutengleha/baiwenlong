package com.esint.music.model;

/*   
 *    
 * �����ƣ�Mp3Info   
 * ��������������Ϣ��bean�ļ�   
 * �����ˣ�bai 
 * ����ʱ�䣺2016-1-10 ����1:33:37   
 *        
 */
public class Mp3Info {

	private long id;// ���ֵ�id
	private long mp3InfoId;//���ղ�����ʱ���ڱ���ԭʼID
	private String title;// ����
	private String artist;// ������
	private String album;// ר��
	private long albumId;// ר����id
	private long duration;// ʱ��
	private long size;// ��С
	private String url;// ·��
	private int isMusic;// �Ƿ�Ϊ����
	private String sortLetters; // ��ʾ����ƴ��������ĸ
	private boolean isInflate;// �Ƿ�����

	public long getId() {
		return id;
	}

	public long getAlbumId() {
		return albumId;
	}

	
	public long getMp3InfoId() {
		return mp3InfoId;
	}

	public void setMp3InfoId(long mp3InfoId) {
		this.mp3InfoId = mp3InfoId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getIsMusic() {
		return isMusic;
	}

	public void setIsMusic(int isMusic) {
		this.isMusic = isMusic;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

	public boolean isInflate() {
		return isInflate;
	}

	public void setInflate(boolean isInflate) {
		this.isInflate = isInflate;
	}

	@Override
	public String toString() {
		return "Mp3Info [id=" + id + ", title=" + title + ", artist=" + artist
				+ ", album=" + album + ", albumId=" + albumId + ", duration="
				+ duration + ", size=" + size + ", url=" + url + ", isMusic="
				+ isMusic + "]";
	}

}
