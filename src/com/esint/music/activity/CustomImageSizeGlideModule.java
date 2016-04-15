package com.esint.music.activity;

import java.io.InputStream;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.bumptech.glide.module.GlideModule;

public class CustomImageSizeGlideModule implements GlideModule {

	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
	}

	@Override
	public void registerComponents(Context context, Glide glide) {
		glide.register(CustomImageSizeModel.class, InputStream.class,
				new CustomImageSizeModelFactory());
	}

	public interface CustomImageSizeModel {
		String requestCustomSizeUrl(int width, int height);
	}

	private class CustomImageSizeModelFactory implements
			ModelLoaderFactory<CustomImageSizeModel, InputStream> {
		@Override
		public ModelLoader<CustomImageSizeModel, InputStream> build(
				Context context, GenericLoaderFactory factories) {
			return new CustomImageSizeUrlLoader(context);
		}

		@Override
		public void teardown() {

		}
	}

	public class CustomImageSizeUrlLoader extends
			BaseGlideUrlLoader<CustomImageSizeModel> {
		public CustomImageSizeUrlLoader(Context context) {
			super(context);
		}

		@Override
		protected String getUrl(CustomImageSizeModel model, int width,
				int height) {
			return model.requestCustomSizeUrl(width, height);
		}
	}

}
