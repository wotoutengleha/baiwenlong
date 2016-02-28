package com.esint.music.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.esint.music.model.Mp3Info;
import com.esint.music.sortlistview.CharacterParser;
import com.esint.music.sortlistview.PinyinComparator;

/**     
* �����ƣ�SortListUtil   
* �������� �������б�����ݾ�������  
* �����ˣ�bai   
* ����ʱ�䣺2016-2-26 ����4:47:56         
*/
public class SortListUtil {
	// ����ת����ƴ������
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;

	public ArrayList<Mp3Info> initMyLocalMusic(ArrayList<Mp3Info> mp3Infos) {
		ArrayList<Mp3Info> arrayList = new ArrayList<Mp3Info>();
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		arrayList = filledData(mp3Infos);
		// ����a-z��������Դ���� ��Ϊ�˽�listView��λ�ý������� ��ֹ����˳�����
		Collections.sort(arrayList, pinyinComparator);
		return arrayList;
	}

	/**
	 * @Description:���˵������б�����
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
			// ����ת����ƴ��
			String pinyin = characterParser.getSelling(date.get(i).getArtist());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
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
