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

import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by eneim on 11/19/15.
 */
class AnimationUtils {

  static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
  static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
  static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
  static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
  static final Interpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();

  /**
   * Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}.
   */
  public static float lerp(float startValue, float endValue, float fraction) {
    return startValue + (fraction * (endValue - startValue));
  }

  static int lerp(int startValue, int endValue, float fraction) {
    return startValue + Math.round(fraction * (endValue - startValue));
  }

  static class AnimationListenerAdapter implements Animation.AnimationListener {
    @Override public void onAnimationStart(Animation animation) {
    }

    @Override public void onAnimationEnd(Animation animation) {
    }

    @Override public void onAnimationRepeat(Animation animation) {
    }
  }
}
