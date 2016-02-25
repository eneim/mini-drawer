/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
