package com.esint.music.dialog;



/**
 * Created by lee on 2014/7/30.
 */
public enum Effectstype {

	Fall(Fall.class),Slidetop(SlideTop.class),Shake(
			Shake.class);
	private Class<? extends BaseEffects> effectsClazz;

	private Effectstype(Class<? extends BaseEffects> mclass) {
		effectsClazz = mclass;
	}

	public BaseEffects getAnimator() {
		BaseEffects bEffects = null;
		try {
			bEffects = effectsClazz.newInstance();
		} catch (ClassCastException e) {
			throw new Error("Can not init animatorClazz instance");
		} catch (InstantiationException e) {
			throw new Error("Can not init animatorClazz instance");
		} catch (IllegalAccessException e) {
			throw new Error("Can not init animatorClazz instance");
		}
		return bEffects;
	}
}
