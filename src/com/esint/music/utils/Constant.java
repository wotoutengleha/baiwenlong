package com.esint.music.utils;

/*   
 * �����ƣ�Constant   
 * ������������   
 * �����ˣ�bai 
 * ����ʱ�䣺2016-1-11 ����8:09:22   
 */
public class Constant {

	public static final int START_ACTIVITY = 0x1;// ����activity����Ϣ
	public static final String ALARM_CLOCK_BROADCAST = "alarm_clock_broadcast";// �������ӵĹ㲥
	public static final String BROADCAST_SHAKE = "com.esint.music.shake";// ����ҡһҡ�л������Ĺ㲥
	public static final String SP_SHAKE_CHANGE_SONG = "shake_change_song";// ҡһҡ�л�����
	public static final String BROADCAST_NAME = "com.esint.music.broadcast";
	public static final String CLICKED_MUNSIC_NAME = "ClickPosition";// ������ظ�����ʱ������λ��
	public static final String CLICKED_MUNSIC_NAME_DOWN = "ClickPositionDown";// ������ظ�����ʱ������λ��
	public static final String CLICKED_MUNSIC_NAME_LIKE = "ClickPositionlike";// ������ظ�����ʱ������λ��
	public static final String COLOR_INDEX = "color_index";// �����ý����е���л�actionBar����ɫ
	public static final String COLOR_INDEX_SELECT = "color_index_select";// ��ѡ���
	public static final String MUSIC_FLAG = "music_flag";
	public static final String MY_LOCAL_MUSIC = "local_music";// �������ֱ��
	public static final String MY_LIKE_MUSIC = "like_music";// ϲ�������ֱ��
	public static final String MY_DOWN_MUSIC = "down_music";// ���ص����ֱ��
	public static final String DB_NAME = "music_db";
	public static final String BAIDU_URL = "http://music.baidu.com/";// �ٶ����ֵĵ�ַ
	public static final String BAIDU_SEARCH = "/search/song";
	public static final String BAIDU_DAYHOT = "top/dayhot/?pst=shouyeTop";// �ٶ��ȸ��
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.97 Safari/537.36";
	public static final String DIR_MUSIC = "/suixin_music/music";
	public static final String LRC_MUSIC = "suixin_music/lrc";
	public static final int UPTATE_LRC = 0x007;
	public static final int UPTATE_LRC_LOCK = 0x008;
	public static final int SUCCESS = 0x005;
	public static final int FAILED = 0x006;
	public static final int UPDATE_LOCKTIME = 0x009;// ��������ҳ��Ľ�����
	public static final int page = 1;// ����������ʱ��������ҳ��
	public static final int NEXT_LEKE_MUSIC = 0x010;// ȡ��ϲ�����ֵ�ʱ�򲥷���һ�׵�what
	public static final int SHAKE_MUSIC = 0x011;// ҡһҡ��ʱ�򲥷���һ�׵�what
	public static final String PLAY_MODE = "play_mode";// ����ģʽ Ĭ����1 ˳�򲥷�
	public static boolean isFirst = true;
	public static boolean ISFirst_PLAY = true;
	public static long exitTime = 0;
	public static int playModel = 1; // 1�� ˳�򲥷� 2��������� 3�ǵ�������
	public static final String ND = "http://www.5nd.com/";
	public static final String ND_HOT = "paihang/liuxinggequ.htm";
	public static final String SHAKE_FLAG = "shake_flag";
	public static final int WHAT_SHAKE = 0x011;// ����ҡһҡ��what
	public static final int DOWN_LRC = 0x012;// �ڲ��Ž����ʱ�����ظ��
	public static boolean isDownStop = true;// �����������ֵĽ�����
	public static boolean isLocalStop = true;// ���Ʊ������ֵĽ�����
	public static boolean isLikeStop = true;// ����ϲ�����ֵĽ�����
	public static boolean isInsert = false;// �Ƿ����������

	// �Ƿ�������ģʽ
	public static final String SHAKE_ON_OFF = "SHAKE_ON_OFF";
	// wifi����
	public static final String IS_WIFI = "SHAKE_ON_OFF";
	// ������
	public static final String DESK_LRC = "desk_lrc";
	// �������
	public static final String LOCK_LRC = "lock_lrc";

	// sharedPreference����
	public static final String SP_NAME = "MUSIC";
	public static final String BACK_IMG = "backImg";
	// handler��������Ϣ
	public static final int UPDATA_TIME = 0x008;
	public static final String PLAYBUTTON_BROAD = "com.esint.music.broadcastreceiver.updateplaybutton";
	public static final String PAUSEBUTTON_BROAD = "com.esint.music.broadcastreceiver.updatepausebutton";
	public static String[] colorBGColorStr = { "#ff33b5e5", "#667e83",
			"#f76f60", "#f57bb8", "#e7923d", "#b38684" };
	public static int colorIndex = 0;
	public static int soundIndex = 0;

	public static final String API_NET_NEWMUSIC_LIST = "http://music.163.com/api/playlist/detail?id=3779629";
	public static final String API_NET_HOTMUSIC_LIST = "http://music.163.com/api/playlist/detail?id=3778678";
	public static final String API_NET_ORIGINALMUSIC_LIST = "http://music.163.com/api/playlist/detail?id=2884035";
	public static final String API_NET_RISEMUSIC_LIST = "http://music.163.com/api/playlist/detail?id=19723756";
	public static final String API_NET_SEARCH_MUSIC = "http://music.163.com/api/search/get";// �������ֵĽӿ�
	public static final int WHAT_NET_NEWMUSIC_LIST = 0x111;
	public static final int WHAT_NEW_SONGINFO = 0x112;
	public static final int WHAT_HOT_SONGINFO = 0x114;
	public static final int WHAT_ORIGINAL_SONGINFO = 0x117;
	public static final int WHAT_RISE_SONGINFO = 0x119;
	public static final int WHAT_NET_HOTMUSIC_LIST = 0x113;
	public static final int WHAT_NET_ORIGINALMUSIC_LIST = 0x116;
	public static final int WHAT_NET_RISEMUSIC_LIST = 0x118;
	public static final int WHAT_EXECEPTION = 0X114;
	public static final int WHAT_UPDATE_BTN = 0x115;// ��������������ֵ�ʱ����°�ť

	public static final String YR_URL = "http://music.163.com/api/song/media?id=";
	public static final String DIR_LRC = "/youran_music/lrc";
}
