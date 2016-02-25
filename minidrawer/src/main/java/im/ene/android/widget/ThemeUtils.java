package im.ene.android.widget;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Created by eneim on 2/23/16.
 */
class ThemeUtils {

  private static final int[] APPCOMPAT_CHECK_ATTRS = { android.support.design.R.attr.colorPrimary };

  static void checkAppCompatTheme(Context context) {
    TypedArray a = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS);
    final boolean failed = a == null || !a.hasValue(0);
    if (a != null) {
      a.recycle();
    }
    if (failed) {
      throw new IllegalArgumentException(
          "You need to use a Theme.AppCompat theme " + "(or descendant) with the design library.");
    }
  }
}
