package com.esint.music.model;

import android.graphics.Bitmap;

/*   
 *    
 * 类名称：BackImg   
 * 类描述：背景图片的bean文件   
 * 创建人：bai 
 * 创建时间：2016-1-15 上午9:06:15   
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
