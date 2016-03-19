package com.esint.music.utils;

import android.view.View;

public interface PageAction {
	public void addPage(View view);

	public View getPage();

	public void finish();
}
