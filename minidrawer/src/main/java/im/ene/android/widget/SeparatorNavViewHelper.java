package im.ene.android.widget;

import android.support.annotation.FloatRange;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by eneim on 2/23/16.
 */
public class SeparatorNavViewHelper extends NavItemViewHelper {

  // NavigationMenuPresenter$SeparatorViewHolder
  // in this case, just use itemView

  public SeparatorNavViewHelper(View itemView) {
    super(itemView);
  }

  @Override protected void onDrawerOffset(@FloatRange(from = 0.f, to = 1.f) float offset) {
    float newValue = AnimationUtils.ACCELERATE_INTERPOLATOR.getInterpolation(offset);
    if (itemView != null) {
      ViewCompat.setAlpha(itemView, newValue);
    }
  }
}
