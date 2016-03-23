package com.esint.music.service;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class NetMusicService extends Service {

	private MediaPlayer player;//����һ��MediaPlayer����

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO �Զ����ɵķ������
		return null;
	}

	//��������
	@Override
	public void onCreate() {
		// ��player����Ϊ��ʱ
		if (player == null) {
			player = MediaPlayer.create(NetMusicService.this, Uri
					.parse("http://m2.music.126.net/pWIM7L_SJC6qawvggvkJuw==/1418370002099651.mp3"));//ʵ��������ͨ�����ű����������ϵ�һ������
			player.setLooping(false);//���ò�ѭ������
		}
		super.onCreate();
	}

	//���ٷ���
	@Override
	public void onDestroy() {
		//������Ϊ��ʱ
		if (player != null) {
			player.stop();//ֹͣ����
			player.release();//�ͷ���Դ
			player = null;//��player��������Ϊnull
		}
		super.onDestroy();
	}

	//��ʼ����
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO �Զ����ɵķ������
		Bundle b = intent.getExtras();//��ȡ����MainActivity���д��ݹ�����Bundle����
		int op = b.getInt("msg");//�ٻ�ȡ��MainActivity����op��ֵ
		switch (op) {
		case 1://��opΪ1ʱ����������Ű�ťʱ
			play();//����play()����
			break;
		case 2://��opΪ2ʱ���������ͣ��ťʱ
			pause();//����pause()����
			break;
		case 3://��opΪ3ʱ�������ֹͣ��ťʱ
			stop();//����stop()����
			break;
		default:
			break;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	//ֹͣ�������ַ���
	private void stop() {
		// ��player����Ϊ��ʱ
		if (player != null) {
			player.seekTo(0);//���ô�ͷ��ʼ
			player.stop();//ֹͣ����
			try {
				player.prepare();//Ԥ��������
			} catch (IllegalStateException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			} catch (IOException e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
	}

	//��ͣ�������ַ���
	private void pause() {
		// ��player�������ڲ���ʱ����player����Ϊ��ʱ
		if (player.isPlaying() && player != null) {
			player.pause();//��ͣ��������
		}
	}

	//�������ַ���
	private void play() {
		// ��player����Ϊ�ղ���player�������ڲ���ʱ
		if (player != null && !player.isPlaying()) {
			Log.e("��ʼ��", "��ʼ��");
			player.start();//��ʼ��������
		}
	}

}
