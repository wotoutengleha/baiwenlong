package com.esint.music.model;

import android.graphics.Bitmap;

/*   
 *    
 * �����ƣ�BackImg   
 * ������������ͼƬ��bean�ļ�   
 * �����ˣ�bai 
 * ����ʱ�䣺2016-1-15 ����9:06:15   
 *        
 */
public class BackImg {
	public String path;
	public Bitmap bitmap;

	public BackImg() {
		super();
	}

	public BackImg(String path, Bitmap bitmap) {
		super();
		this.path = path;
		this.bitmap = bitmap;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

}
