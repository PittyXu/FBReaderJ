/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.preferences;

import android.content.Intent;
import android.view.KeyEvent;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.util.DeviceType;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.fbreader.fbreader.options.EInkOptions;
import org.geometerplus.fbreader.fbreader.options.FooterOptions;
import org.geometerplus.fbreader.fbreader.options.ImageOptions;
import org.geometerplus.fbreader.fbreader.options.MiscOptions;
import org.geometerplus.fbreader.fbreader.options.PageTurningOptions;
import org.geometerplus.fbreader.fbreader.options.ViewOptions;
import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.style.ZLTextBaseStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextNGStyleDescription;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PreferenceActivity extends ZLPreferenceActivity {

  public PreferenceActivity() {
    super("Preferences");
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void init(Intent intent) {
    final Config config = Config.Instance();
    config.requestAllValuesForGroup("Style");
    config.requestAllValuesForGroup("Options");
    config.requestAllValuesForGroup("LookNFeel");
    config.requestAllValuesForGroup("Fonts");
    config.requestAllValuesForGroup("Files");
    config.requestAllValuesForGroup("Scrolling");
    config.requestAllValuesForGroup("Colors");
    config.requestAllValuesForGroup("Sync");
    setResult(FBReader.RESULT_REPAINT);

    final ViewOptions viewOptions = new ViewOptions();
    final MiscOptions miscOptions = new MiscOptions();
    final FooterOptions footerOptions = viewOptions.getFooterOptions();
    final PageTurningOptions pageTurningOptions = new PageTurningOptions();
    final ImageOptions imageOptions = new ImageOptions();
    final ColorProfile profile = viewOptions.getColorProfile();
    final ZLTextStyleCollection collection = viewOptions.getTextStyleCollection();
    final ZLKeyBindings keyBindings = new ZLKeyBindings();

    final ZLAndroidLibrary androidLibrary = (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
    // TODO: use user-defined locale, not the default one,
    // or set user-defined locale as default
    final String decimalSeparator =
        String.valueOf(new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator());

    final Screen appearanceScreen = createPreferenceScreen("appearance");
    appearanceScreen.addPreference(new ZLStringChoicePreference(
        this, appearanceScreen.Resource.getResource("screenOrientation"),
        androidLibrary.getOrientationOption(), androidLibrary.allOrientations()
    ));
    appearanceScreen.addPreference(new ZLBooleanPreference(
        this,
        viewOptions.TwoColumnView,
        appearanceScreen.Resource.getResource("twoColumnView")
    ));

    if (DeviceType.Instance().isEInk()) {
      final EInkOptions einkOptions = new EInkOptions();
      final Screen einkScreen = createPreferenceScreen("eink");
      final PreferenceSet einkPreferences = new PreferenceSet.Enabler() {
        @Override
        protected Boolean detectState() {
          return einkOptions.EnableFastRefresh.getValue();
        }
      };

      einkScreen.addPreference(new ZLBooleanPreference(
          this, einkOptions.EnableFastRefresh,
          einkScreen.Resource.getResource("enableFastRefresh")
      ) {
        @Override
        protected void onClick() {
          super.onClick();
          einkPreferences.run();
        }
      });

      final ZLIntegerRangePreference updateIntervalPreference = new ZLIntegerRangePreference(
          this, einkScreen.Resource.getResource("interval"), einkOptions.UpdateInterval
      );
      einkScreen.addPreference(updateIntervalPreference);

      einkPreferences.add(updateIntervalPreference);
      einkPreferences.run();
    }

    final Screen textScreen = createPreferenceScreen("text");

    final Screen fontPropertiesScreen = textScreen.createPreferenceScreen("fontProperties");
    fontPropertiesScreen.addOption(ZLAndroidPaintContext.AntiAliasOption, "antiAlias");
    fontPropertiesScreen.addOption(ZLAndroidPaintContext.DeviceKerningOption, "deviceKerning");
    fontPropertiesScreen.addOption(ZLAndroidPaintContext.DitheringOption, "dithering");
    fontPropertiesScreen.addOption(ZLAndroidPaintContext.SubpixelOption, "subpixel");

    final ZLTextBaseStyle baseStyle = collection.getBaseStyle();

    final PreferenceSet fontReloader = new PreferenceSet.Reloader();

    fontReloader.add(textScreen.addPreference(new FontPreference(
        this, textScreen.Resource.getResource("font"),
        baseStyle.FontFamilyOption, false
    )));
    textScreen.addPreference(new ZLIntegerRangePreference(
        this, textScreen.Resource.getResource("fontSize"),
        baseStyle.FontSizeOption
    ));
    textScreen.addPreference(new FontStylePreference(
        this, textScreen.Resource.getResource("fontStyle"),
        baseStyle.BoldOption, baseStyle.ItalicOption
    ));
    final ZLIntegerRangeOption spaceOption = baseStyle.LineSpaceOption;
    final String[] spacings = new String[spaceOption.MaxValue - spaceOption.MinValue + 1];
    for (int i = 0; i < spacings.length; ++i) {
      final int val = spaceOption.MinValue + i;
      spacings[i] = (char) (val / 10 + '0') + decimalSeparator + (char) (val % 10 + '0');
    }
    textScreen.addPreference(new ZLChoicePreference(
        this, textScreen.Resource.getResource("lineSpacing"),
        spaceOption, spacings
    ));
    final String[] alignments = {"left", "right", "center", "justify"};
    textScreen.addPreference(new ZLChoicePreference(
        this, textScreen.Resource.getResource("alignment"),
        baseStyle.AlignmentOption, alignments
    ));
    textScreen.addOption(baseStyle.AutoHyphenationOption, "autoHyphenations");

    final Screen moreStylesScreen = textScreen.createPreferenceScreen("more");
    for (ZLTextNGStyleDescription description : collection.getDescriptionList()) {
      final Screen ngScreen = moreStylesScreen.createPreferenceScreen(description.Name);
      ngScreen.addPreference(new FontPreference(
          this, textScreen.Resource.getResource("font"),
          description.FontFamilyOption, true
      ));
      ngScreen.addPreference(new StringPreference(
          this, description.FontSizeOption,
          StringPreference.Constraint.POSITIVE_LENGTH,
          textScreen.Resource, "fontSize"
      ));
      ngScreen.addPreference(new ZLStringChoicePreference(
          this, textScreen.Resource.getResource("bold"),
          description.FontWeightOption,
          new String[] {"inherit", "normal", "bold"}
      ));
      ngScreen.addPreference(new ZLStringChoicePreference(
          this, textScreen.Resource.getResource("italic"),
          description.FontStyleOption,
          new String[] {"inherit", "normal", "italic"}
      ));
      ngScreen.addPreference(new ZLStringChoicePreference(
          this, textScreen.Resource.getResource("textDecoration"),
          description.TextDecorationOption,
          new String[] {"inherit", "none", "underline", "line-through"}
      ));
      ngScreen.addPreference(new ZLStringChoicePreference(
          this, textScreen.Resource.getResource("allowHyphenations"),
          description.HyphenationOption,
          new String[] {"inherit", "none", "auto"}
      ));
      ngScreen.addPreference(new ZLStringChoicePreference(
          this, textScreen.Resource.getResource("alignment"),
          description.AlignmentOption,
          new String[] {"inherit", "left", "right", "center", "justify"}
      ));
      ngScreen.addPreference(new StringPreference(
          this, description.LineHeightOption,
          StringPreference.Constraint.PERCENT,
          textScreen.Resource, "lineSpacing"
      ));
      ngScreen.addPreference(new StringPreference(
          this, description.MarginTopOption,
          StringPreference.Constraint.LENGTH,
          textScreen.Resource, "spaceBefore"
      ));
      ngScreen.addPreference(new StringPreference(
          this, description.MarginBottomOption,
          StringPreference.Constraint.LENGTH,
          textScreen.Resource, "spaceAfter"
      ));
      ngScreen.addPreference(new StringPreference(
          this, description.MarginLeftOption,
          StringPreference.Constraint.LENGTH,
          textScreen.Resource, "leftIndent"
      ));
      ngScreen.addPreference(new StringPreference(
          this, description.MarginRightOption,
          StringPreference.Constraint.LENGTH,
          textScreen.Resource, "rightIndent"
      ));
      ngScreen.addPreference(new StringPreference(
          this, description.TextIndentOption,
          StringPreference.Constraint.LENGTH,
          textScreen.Resource, "firstLineIndent"
      ));
      ngScreen.addPreference(new StringPreference(
          this, description.VerticalAlignOption,
          StringPreference.Constraint.LENGTH,
          textScreen.Resource, "verticalAlignment"
      ));
    }

    final Screen toastsScreen = createPreferenceScreen("toast");
    toastsScreen.addOption(miscOptions.ToastFontSizePercent, "fontSizePercent");
    toastsScreen.addOption(miscOptions.ShowFootnoteToast, "showFootnoteToast");
    toastsScreen.addPreference(new ZLEnumPreference(
        this,
        miscOptions.FootnoteToastDuration,
        toastsScreen.Resource.getResource("footnoteToastDuration"),
        ZLResource.resource("duration")
    ));

    final Screen cssScreen = createPreferenceScreen("css");
    cssScreen.addOption(baseStyle.UseCSSFontFamilyOption, "fontFamily");
    cssScreen.addOption(baseStyle.UseCSSFontSizeOption, "fontSize");
    cssScreen.addOption(baseStyle.UseCSSTextAlignmentOption, "textAlignment");
    cssScreen.addOption(baseStyle.UseCSSMarginsOption, "margins");

    final Screen colorsScreen = createPreferenceScreen("colors");

    final PreferenceSet backgroundSet = new PreferenceSet.Enabler() {
      @Override
      protected Boolean detectState() {
        return profile.WallpaperOption.getValue().startsWith("/");
      }
    };
    backgroundSet.add(colorsScreen.addOption(profile.FillModeOption, "fillMode"));
    backgroundSet.run();

    colorsScreen.addOption(profile.RegularTextOption, "text");
    colorsScreen.addOption(profile.HyperlinkTextOption, "hyperlink");
    colorsScreen.addOption(profile.VisitedHyperlinkTextOption, "hyperlinkVisited");
    colorsScreen.addOption(profile.FooterFillOption, "footerOldStyle");
    colorsScreen.addOption(profile.FooterNGBackgroundOption, "footerBackground");
    colorsScreen.addOption(profile.FooterNGForegroundOption, "footerForeground");
    colorsScreen.addOption(profile.FooterNGForegroundUnreadOption, "footerForegroundUnread");
    colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
    colorsScreen.addOption(profile.SelectionForegroundOption, "selectionForeground");
    colorsScreen.addOption(profile.HighlightingForegroundOption, "highlightingForeground");
    colorsScreen.addOption(profile.HighlightingBackgroundOption, "highlightingBackground");

    final Screen marginsScreen = createPreferenceScreen("margins");
    marginsScreen.addOption(viewOptions.LeftMargin, "left");
    marginsScreen.addOption(viewOptions.RightMargin, "right");
    marginsScreen.addOption(viewOptions.TopMargin, "top");
    marginsScreen.addOption(viewOptions.BottomMargin, "bottom");
    marginsScreen.addOption(viewOptions.SpaceBetweenColumns, "spaceBetweenColumns");

    final Screen statusLineScreen = createPreferenceScreen("scrollBar");

    final PreferenceSet footerPreferences = new PreferenceSet.Enabler() {
      @Override
      protected Boolean detectState() {
        switch (viewOptions.ScrollbarType.getValue()) {
          case FBView.SCROLLBAR_SHOW_AS_FOOTER:
          case FBView.SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE:
            return true;
          default:
            return false;
        }
      }
    };
    final PreferenceSet tocPreferences = new PreferenceSet.Enabler() {
      @Override
      protected Boolean detectState() {
        switch (viewOptions.ScrollbarType.getValue()) {
          case FBView.SCROLLBAR_SHOW_AS_FOOTER:
          case FBView.SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE:
            return footerOptions.ShowTOCMarks.getValue();
          default:
            return false;
        }
      }
    };
    final PreferenceSet oldStyleFooterPreferences = new PreferenceSet.Enabler() {
      @Override
      protected Boolean detectState() {
        switch (viewOptions.ScrollbarType.getValue()) {
          case FBView.SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE:
            return true;
          default:
            return false;
        }
      }
    };
    final PreferenceSet newStyleFooterPreferences = new PreferenceSet.Enabler() {
      @Override
      protected Boolean detectState() {
        switch (viewOptions.ScrollbarType.getValue()) {
          case FBView.SCROLLBAR_SHOW_AS_FOOTER:
            return true;
          default:
            return false;
        }
      }
    };

    final String[] scrollBarTypes = {"hide", "show", "showAsProgress", "showAsFooter",
        "showAsFooterOldStyle"};
    statusLineScreen.addPreference(new ZLChoicePreference(
        this, statusLineScreen.Resource.getResource("scrollbarType"),
        viewOptions.ScrollbarType, scrollBarTypes
    ) {
      @Override
      protected void onDialogClosed(boolean result) {
        super.onDialogClosed(result);
        footerPreferences.run();
        tocPreferences.run();
        oldStyleFooterPreferences.run();
        newStyleFooterPreferences.run();
      }
    });

    footerPreferences.add(statusLineScreen.addPreference(new ZLIntegerRangePreference(
        this, statusLineScreen.Resource.getResource("footerHeight"),
        viewOptions.FooterHeight
    )));
    oldStyleFooterPreferences.add(
        statusLineScreen.addOption(profile.FooterFillOption, "footerOldStyleColor"));
    newStyleFooterPreferences.add(
        statusLineScreen.addOption(profile.FooterNGBackgroundOption, "footerBackgroundColor"));
    newStyleFooterPreferences.add(
        statusLineScreen.addOption(profile.FooterNGForegroundOption, "footerForegroundColor"));
    newStyleFooterPreferences.add(statusLineScreen.addOption(profile.FooterNGForegroundUnreadOption,
        "footerForegroundUnreadColor"));
    footerPreferences.add(statusLineScreen.addPreference(new ZLBooleanPreference(
        PreferenceActivity.this,
        footerOptions.ShowTOCMarks,
        statusLineScreen.Resource.getResource("tocMarks")
    ) {
      @Override
      protected void onClick() {
        super.onClick();
        tocPreferences.run();
      }
    }));
    tocPreferences.add(statusLineScreen.addOption(footerOptions.MaxTOCMarks, "tocMarksMaxNumber"));
    footerPreferences.add(statusLineScreen.addOption(footerOptions.ShowProgress, "showProgress"));
    footerPreferences.add(statusLineScreen.addPreference(new FontPreference(
        this, statusLineScreen.Resource.getResource("font"),
        footerOptions.Font, false
    )));
    footerPreferences.run();
    tocPreferences.run();
    oldStyleFooterPreferences.run();
    newStyleFooterPreferences.run();

		/*
		final Screen colorProfileScreen = createPreferenceScreen("colorProfile");
		final ZLResource resource = colorProfileScreen.Resource;
		colorProfileScreen.setSummary(ColorProfilePreference.createTitle(resource, fbreader.getColorProfileName()));
		for (String key : ColorProfile.names()) {
			colorProfileScreen.addPreference(new ColorProfilePreference(
				this, fbreader, colorProfileScreen, key, ColorProfilePreference.createTitle(resource, key)
			));
		}
		 */

    final Screen scrollingScreen = createPreferenceScreen("scrolling");
    scrollingScreen.addOption(pageTurningOptions.FingerScrolling, "fingerScrolling");
    scrollingScreen.addOption(miscOptions.EnableDoubleTap, "enableDoubleTapDetection");

    final PreferenceSet volumeKeysPreferences = new PreferenceSet.Enabler() {
      @Override
      protected Boolean detectState() {
        return keyBindings.hasBinding(KeyEvent.KEYCODE_VOLUME_UP, false);
      }
    };
    scrollingScreen.addPreference(new ZLCheckBoxPreference(
        this, scrollingScreen.Resource.getResource("volumeKeys")
    ) {
      {
        setChecked(keyBindings.hasBinding(KeyEvent.KEYCODE_VOLUME_UP, false));
      }

      @Override
      protected void onClick() {
        super.onClick();
        if (isChecked()) {
          keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false,
              ActionCode.VOLUME_KEY_SCROLL_FORWARD);
          keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
        } else {
          keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, FBReaderApp.NoAction);
          keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, FBReaderApp.NoAction);
        }
        volumeKeysPreferences.run();
      }
    });
    volumeKeysPreferences.add(scrollingScreen.addPreference(new ZLCheckBoxPreference(
        this, scrollingScreen.Resource.getResource("invertVolumeKeys")
    ) {
      {
        setChecked(ActionCode.VOLUME_KEY_SCROLL_FORWARD.equals(
            keyBindings.getBinding(KeyEvent.KEYCODE_VOLUME_UP, false)
        ));
      }

      @Override
      protected void onClick() {
        super.onClick();
        if (isChecked()) {
          keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false,
              ActionCode.VOLUME_KEY_SCROLL_BACK);
          keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false,
              ActionCode.VOLUME_KEY_SCROLL_FORWARD);
        } else {
          keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false,
              ActionCode.VOLUME_KEY_SCROLL_FORWARD);
          keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
        }
      }
    }));
    volumeKeysPreferences.run();

    scrollingScreen.addOption(pageTurningOptions.Animation, "animation");
    scrollingScreen.addPreference(new AnimationSpeedPreference(
        this,
        scrollingScreen.Resource,
        "animationSpeed",
        pageTurningOptions.AnimationSpeed
    ));
    scrollingScreen.addOption(pageTurningOptions.Horizontal, "horizontal");

    final Screen imagesScreen = createPreferenceScreen("images");
    imagesScreen.addOption(imageOptions.TapAction, "longTapAction");
    imagesScreen.addOption(imageOptions.FitToScreen, "fitImagesToScreen");
    imagesScreen.addOption(imageOptions.ImageViewBackground, "backgroundColor");
    imagesScreen.addOption(imageOptions.MatchBackground, "matchBackground");
  }
}
