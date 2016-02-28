package com.esint.music.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.esint.music.model.Mp3Info;
import com.esint.music.sortlistview.CharacterParser;
import com.esint.music.sortlistview.PinyinComparator;

/**     
* 类名称：SortListUtil   
* 类描述： 把音乐列表的数据惊醒排序  
* 创建人：bai   
* 创建时间：2016-2-26 下午4:47:56         
*/
public class SortListUtil {
	// 汉字转换成拼音的类
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;

	public ArrayList<Mp3Info> initMyLocalMusic(ArrayList<Mp3Info> mp3Infos) {
		ArrayList<Mp3Info> arrayList = new ArrayList<Mp3Info>();
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		arrayList = filledData(mp3Infos);
		// 根据a-z进行排序源数据 是为了将listView的位置进行排序 防止播放顺序错乱
		Collections.sort(arrayList, pinyinComparator);
		return arrayList;
	}

	/**
	 * @Description:过滤的音乐列表数据
	 * @param date
	 * @return
	 * @return ArrayList<Mp3Info>
	 * @author bai
	 */
	public ArrayList<Mp3Info> filledData(List<Mp3Info> date) {
		ArrayList<Mp3Info> mSortList = new ArrayList<Mp3Info>();
		for (int i = 0; i < date.size(); i++) {
			Mp3Info mp3Info = new Mp3Info();
			mp3Info.setArtist(date.get(i).getArtist());
			mp3Info.setTitle(date.get(i).getTitle());
			mp3Info.setId(date.get(i).getId());
			mp3Info.setAlbumId(date.get(i).getAlbumId());
			mp3Info.setDuration(date.get(i).getDuration());
			mp3Info.setSize(date.get(i).getSize());
			mp3Info.setUrl(date.get(i).getUrl());
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date.get(i).getArtist());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				mp3Info.setSortLetters(sortString.toUpperCase());
			} else {
				mp3Info.setSortLetters("#");
			}

			mSortList.add(mp3Info);
		}
		return mSortList;

	}
}
