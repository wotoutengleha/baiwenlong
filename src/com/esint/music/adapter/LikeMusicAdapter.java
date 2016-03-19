package com.esint.music.adapter;

import java.text.DecimalFormat;
import java.util.List;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esint.music.R;
import com.esint.music.activity.MainFragmentActivity;
import com.esint.music.dialog.Effectstype;
import com.esint.music.dialog.NiftyDialogBuilder;
import com.esint.music.fragment.MyTabMusic;
import com.esint.music.model.LikeMusicModel;
import com.esint.music.model.LikeMusicModel;
import com.esint.music.utils.Constant;
import com.esint.music.utils.MediaUtils;
import com.esint.music.utils.SharedPrefUtil;

/**   
* �����ƣ�LikeMusicAdapter   
* ��������  ϲ�������ֵ������� 
* �����ˣ�bai   
* ����ʱ�䣺2016-3-18 ����2:22:38         
*/
public class LikeMusicAdapter extends BaseAdapter {
	private List<LikeMusicModel> list;
	private Context mContext;
	private SharedPreferences sp;
	private Effectstype effect;
	private NiftyDialogBuilder dialogBuilder;

	public LikeMusicAdapter(Context mContext, List<LikeMusicModel> list) {
		this.mContext = mContext;
		this.list = list;
		sp = mContext.getSharedPreferences("ARROW", Context.MODE_PRIVATE);
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	private ViewHolder viewHolder;
	private SharedPreferences spf;

	public View getView(final int position, View view, ViewGroup arg2) {
		final LikeMusicModel likeMusicModel = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(
					R.layout.ilv_likemusic, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.tvSongName = (TextView) view.findViewById(R.id.songname);
			viewHolder.ivRecord = (ImageView) view.findViewById(R.id.record);
			viewHolder.listBackGround = (LinearLayout) view
					.findViewById(R.id.backgroundRecord);
			viewHolder.lvArrowDown = (ImageView) view
					.findViewById(R.id.arrowDown);
			viewHolder.lvArrowUp = (ImageView) view.findViewById(R.id.arrowUp);
			viewHolder.menuLayout = (LinearLayout) view
					.findViewById(R.id.arrow_like_ll);
			viewHolder.ivLikeNormal = (ImageView) view
					.findViewById(R.id.ivLikeNormal);
			viewHolder.ivLikePress = (ImageView) view
					.findViewById(R.id.ivLikePress);
			viewHolder.ivDelete = (LinearLayout) view
					.findViewById(R.id.ll_arrow_delete);
			viewHolder.ivInfo = (LinearLayout) view
					.findViewById(R.id.ll_arrow_info);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		int recordPosition = SharedPrefUtil.getInt(mContext,
				Constant.CLICKED_MUNSIC_NAME_LIKE, -1);
		if (recordPosition == position) {
			viewHolder.ivRecord.setVisibility(View.VISIBLE);
			viewHolder.listBackGround
					.setBackgroundResource(R.drawable.play_bar_bottom1_p);
		} else {
			viewHolder.ivRecord.setVisibility(View.GONE);
			viewHolder.listBackGround.setBackgroundColor(color.white);
		}

		viewHolder.lvArrowDown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sp.edit().putInt("ARROW", position).commit();
				notifyDataSetChanged();
			}
		});
		viewHolder.lvArrowUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sp.edit().clear().commit();
				notifyDataSetChanged();
			}
		});
//		viewHolder.ivLikeNormal.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				spf = mContext.getSharedPreferences("LIKE",
//						Context.MODE_PRIVATE);
//				spf.edit().putInt("LIKE", position).commit();
//				notifyDataSetChanged();
//			}
//		});
		viewHolder.ivLikePress.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialogLike(position);
			}
		});
		viewHolder.ivDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				chooseDialog(position);
			}
		});
		viewHolder.ivInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				musicInfo(position);
			}
		});

		int inflatPosition = sp.getInt("ARROW", -1);
		if (inflatPosition == position) {
			viewHolder.menuLayout.setVisibility(View.VISIBLE);
			viewHolder.lvArrowUp.setVisibility(View.VISIBLE);
			viewHolder.lvArrowDown.setVisibility(View.GONE);

		} else {
			viewHolder.menuLayout.setVisibility(View.GONE);
			viewHolder.lvArrowDown.setVisibility(View.VISIBLE);
			viewHolder.lvArrowUp.setVisibility(View.GONE);

		}
//		SharedPreferences spf = mContext.getSharedPreferences("LIKE",
//				Context.MODE_PRIVATE);
//		int like = spf.getInt("LIKE", -1);
//		if (like == position) {
//			viewHolder.ivLikePress.setVisibility(View.VISIBLE);
//			viewHolder.ivLikeNormal.setVisibility(View.GONE);
//		} else {
//			viewHolder.ivLikePress.setVisibility(View.GONE);
//			viewHolder.ivLikeNormal.setVisibility(View.VISIBLE);
//		}
		viewHolder.tvTitle.setText(likeMusicModel.getMusicArtist());
		viewHolder.tvSongName.setText(likeMusicModel.getMusicName());
		return view;
	}

	final static class ViewHolder {
		TextView tvLetter;
		TextView tvTitle;
		TextView tvSongName;
		ImageView ivRecord;// ���������
		ImageView lvArrowDown;// ����������ֵļ�ͷ
		ImageView lvArrowUp;// ���ϵļ�ͷ
		LinearLayout listBackGround;// ��ǰѡ�еĸ�������
		LinearLayout menuLayout;// �����Ĳ���
		ImageView ivLikeNormal;// ϲ����ť
		ImageView ivLikePress;// ϲ����ɫ
		LinearLayout ivDelete;// ɾ����ť
		LinearLayout ivInfo;// ��Ϣ��ť
	}

	// ɾ��������dialog
	private void chooseDialog(int position) {
		effect = Effectstype.Shake;
		dialogBuilder = new NiftyDialogBuilder(mContext, R.style.dialog_untran);
		dialogBuilder
				.withTitle("ȷ��Ҫɾ�����׸���")
				.withTitleColor("#FFFFFF")
				.withDividerColor("#97E8F9")
				.withMessage(
						list.get(position).getMusicArtist() + " - "
								+ list.get(position).getMusicName())
				.withMessageColor("#FFFFFF").isCancelableOnTouchOutside(true)
				.withEffect(effect).withButton1Text("ȷ��").withButton2Text("ȡ��")
				.setCustomView(R.layout.custom_view, mContext)
				.setButton1Click(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogBuilder.dismiss();
					}
				}).setButton2Click(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialogBuilder.dismiss();
					}
				}).show();
	}

	// ������Ϣ��dialog
	private void musicInfo(int position) {
		View dialogView = View.inflate(mContext, R.layout.dialog_musicinfo,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		final AlertDialog dialog = builder.create();
		// һ����show֮ǰ����
		dialog.setView(dialogView, 0, 0, 0, 0);
		dialog.show();
		// ����ʹ��setContentView���� ��Ȼû�취��Ӧ����¼�
		// dialog.setContentView(R.layout.musicinfo_dialog);
		LinearLayout dismissBtn = (LinearLayout) dialogView
				.findViewById(R.id.ll_dismiss);
		TextView musinName = (TextView) dialog.findViewById(R.id.musicNameInfo);
		TextView artistInfo = (TextView) dialog.findViewById(R.id.artistInfo);
		TextView musicTimeInfo = (TextView) dialog
				.findViewById(R.id.musicTimeInfo);
		TextView musicSizeInfo = (TextView) dialog
				.findViewById(R.id.musicSizeInfo);
		TextView musicPathInfo = (TextView) dialog
				.findViewById(R.id.musicPathInfo);
		musinName.setText(list.get(position).getMusicName());
		artistInfo.setText(list.get(position).getMusicArtist());
		musicTimeInfo.setText(MediaUtils.formatTime((list.get(position)
				.getMusicTime())));
		// ת����M ������λС��
		DecimalFormat df2 = new DecimalFormat("##.00");
		// String formatSize = df2.format(list.get(position).getSize()
		// / (1024.00 * 1024.00));
		// musicSizeInfo.setText(formatSize + "M");
		musicPathInfo.setText(list.get(position).getMusicURL());
		dismissBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	// ��ʾȡ��ϲ���ĶԻ���
	private void showDialogLike(final int position) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		final AlertDialog dialog = builder.create();
		View cancleLike = View.inflate(mContext, R.layout.dialog_cancellike, null);
		TextView cancelTv = (TextView) cancleLike.findViewById(R.id.tv_dismiss);
		TextView okTv = (TextView) cancleLike.findViewById(R.id.tv_ok);
		dialog.show();
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		WindowManager m = ((Activity)mContext).getWindowManager();
		Display d = m.getDefaultDisplay();
		params.height = (int) (d.getHeight() * 0.286);
		params.width = (int) (d.getWidth() * 0.8);
		dialog.getWindow().setAttributes(params);
		dialog.setContentView(cancleLike);
		okTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//ȡ��ϲ�����׸��� �����ݿ���ɾ��
				String musicTitle = list.get(position).getMusicName();
				String whereClause = "MusicTitle=?";
				String[] whereArgs = {musicTitle};
				MainFragmentActivity.db.delete("Music", whereClause, whereArgs);
				list.remove(position);
				//ɾ�����������Ժ�  ʹ��handler������Ϣ������һ�׸���
				Log.e("�����ĸ�������", musicTitle);
				Message message = MyTabMusic.mHandler.obtainMessage(Constant.NEXT_LEKE_MUSIC,musicTitle);
				message.sendToTarget();
				sp.edit().clear().commit();
				notifyDataSetChanged();
				dialog.dismiss();
			}
		});
		cancelTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

}