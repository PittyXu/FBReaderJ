package org.geometerplus.android.fbreader.config;

import android.content.Context;

import org.geometerplus.android.util.DefaultPreferences;
import org.geometerplus.zlibrary.text.view.ZLTextViewBase.ImageFitting;

public class ImagePreferences {
  public enum TapActionEnum {
    doNothing, selectImage, openImageView
  }

  private static final String BACKGROUND_COLOR = "image_view_background_color";
  private static final String FIT_IMAGES_TO_SCREEN = "image_fit_to_screen";
  private static final String TAPPING_ACTION = "image_tapping_action";
  private static final String MATCH_BACKGROUND = "image_match_background";

  public static void setImageViewBackgroundColor(Context pContext, int color) {
    DefaultPreferences.getInstance(pContext).setInt(BACKGROUND_COLOR, color);
  }

  public static int getImageViewBackgroundColor(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(BACKGROUND_COLOR);
  }

  public static void setFitToScreen(Context pContext, int fit) {
    DefaultPreferences.getInstance(pContext).setInt(FIT_IMAGES_TO_SCREEN, fit);
  }

  /**
   * 图片缩放模式
   * {@link ImageFitting#none} 原始图片大小
   * {@link ImageFitting#covers} cover适配屏幕宽度, 其他图片原始大小
   * {@link ImageFitting#all} 所有图片适配屏幕宽度
   */
  public static int getFitToScreen(Context pContext) {
    return DefaultPreferences.getInstance(pContext)
        .getInt(FIT_IMAGES_TO_SCREEN, ImageFitting.all.ordinal());
  }

  public static void setTappingAction(Context pContext, int action) {
    DefaultPreferences.getInstance(pContext).setInt(TAPPING_ACTION, action);
  }

  public static int getTappingAction(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(TAPPING_ACTION, TapActionEnum.openImageView.ordinal());
  }

  public static void setMatchBackground(Context pContext, boolean match) {
    DefaultPreferences.getInstance(pContext).setBoolean(MATCH_BACKGROUND, match);
  }

  public static boolean getMatchBackground(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(MATCH_BACKGROUND, true);
  }
}
