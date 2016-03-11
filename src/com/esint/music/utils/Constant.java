package com.esint.music.utils;

/*   
 * 类名称：Constant   
 * 类描述：常量   
 * 创建人：bai 
 * 创建时间：2016-1-11 上午8:09:22   
 */
public class Constant {

	public static final int START_ACTIVITY = 0x1;// 启动activity的消息
	public static final String ALARM_CLOCK_BROADCAST = "alarm_clock_broadcast";// 设置闹钟的广播
	public static final String BROADCAST_SHAKE = "com.ldw.music.shake";// 设置摇一摇切换歌曲的广播
	public static final String SP_SHAKE_CHANGE_SONG = "shake_change_song";// 摇一摇切换歌曲
	public static final String CLICKED_MUNSIC_NAME = "ClickPosition";// 点击歌曲的时候存入的位置
	public static final String CLICKED_MUNSIC_NAME_DOWN = "ClickPositionDown";// 点击下载歌曲的时候存入的位置
	public static final String COLOR_INDEX = "color_index";// 在设置界面中点击切换actionBar的颜色
	public static final String COLOR_INDEX_SELECT = "color_index_select";// 被选择的
	public static final String MUSIC_FLAG = "music_flag";
	public static final String MY_LOCAL_MUSIC = "local_music";// 本地音乐标记
	public static final String MY_LIKE_MUSIC = "like_music";// 喜欢的音乐标记
	public static final String MY_DOWN_MUSIC = "down_music";// 下载的音乐标记
	public static final String DB_NAME = "music_db";
	public static final String BAIDU_URL = "http://music.baidu.com/";// 百度音乐的地址
	public static final String BAIDU_SEARCH = "/search/song";
	public static final String BAIDU_DAYHOT = "top/dayhot/?pst=shouyeTop";// 百度热歌榜
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.97 Safari/537.36";
	public static final String DIR_MUSIC = "/suixin_music/music";
	public static final String LRC_MUSIC = "suixin_music/lrc";
	public static final int UPTATE_LRC = 0x007;
	public static final int UPTATE_LRC_LOCK = 0x008;
	public static final int SUCCESS = 0x005;
	public static final int FAILED = 0x006;
	public static final int UPDATE_LOCKTIME  = 0x009;//更新锁屏页面的进度条
	public static final int page = 1;// 搜索歌曲的时候搜索的页数
	public static final String  PLAY_MODE = "play_mode";//播放模式 默认是1  顺序播放
	public static boolean isFirst = true;
	public static boolean ISFirst_PLAY = true;
	public static long exitTime = 0;

	public static final String ND = "http://www.5nd.com/";
	public static final String ND_HOT = "paihang/liuxinggequ.htm";

	// 是否开启了振动模式
	public static final String SHAKE_ON_OFF = "SHAKE_ON_OFF";

	// sharedPreference名字
	public static final String SP_NAME = "MUSIC";
	public static final String BACK_IMG = "backImg";
	// handler发出的消息
	public static final int UPDATA_TIME = 0x008;
	public static final String PLAYBUTTON_BROAD = "com.esint.music.broadcastreceiver.updateplaybutton";
	public static final String PAUSEBUTTON_BROAD = "com.esint.music.broadcastreceiver.updatepausebutton";

	public static String[] colorBGColorStr = { "#ff33b5e5", "#667e83",
			"#f76f60", "#f57bb8", "#e7923d", "#b38684" };
	public static int colorIndex = 0;
	public static int soundIndex = 0;

	public static final String API_NET_NEWMUSIC_LIST = "http://music.163.com/api/playlist/detail?id=3779629";
	public static final String API_NET_HOTMUSIC_LIST = "http://music.163.com/api/playlist/detail?id=3778678";
	public static final String API_NET_SEARCH_MUSIC = "http://music.163.com/api/search/get";// 搜索音乐的接口
	public static final int WHAT_NET_NEWMUSIC_LIST = 0x111;
	public static final int WHAT_NEW_SONGINFO = 0x112;
	public static final int WHAT_HOT_SONGINFO = 0x114;
	public static final int WHAT_NET_HOTMUSIC_LIST = 0x113;
	public static final int WHAT_EXECEPTION = 0X114;
}
