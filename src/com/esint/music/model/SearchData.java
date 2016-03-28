package com.esint.music.model;

/**   
* 类名称：SearchData   
* 类描述：搜索的bean   
* 创建人：bai   
* 创建时间：2016-3-28 上午9:44:58         
*/
public class SearchData {

	private String id;
	private String content;

	public String getId() {
		return id;
	}

	public SearchData setId(String id) {
		this.id = id;
		return this;
	}

	public String getContent() {
		return content;
	}

	public SearchData setContent(String content) {
		this.content = content;
		return this;
	}
}
