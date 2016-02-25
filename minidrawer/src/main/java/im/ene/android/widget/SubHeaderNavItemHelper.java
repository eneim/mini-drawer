package im.ene.android.widget;

import android.support.annotation.FloatRange;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by eneim on 2/23/16.
 */
public class SubHeaderNavItemHelper extends NavItemViewHelper {

  // in this case, itemView itself it a TextView
  private final TextView textView;

  private final int viewHeight;

  // NavigationMenuPresenter$SubheaderViewHolder
  public SubHeaderNavItemHelper(View itemView) {
    super(itemView);
    textView = (TextView) itemView;
    viewHeight = itemView.getLayoutParams().height;
  }

  @Override protected void onDrawerOffset(@FloatRange(from = 0.f, to = 1.f) float offset) {
    float newValue = AnimationUtils.ACCELERATE_INTERPOLATOR.getInterpolation(offset);

    if (this.textView != null) {
      this.textView.setTextColor(this.textView.getTextColors().withAlpha((int) (newValue * 255)));
    }

    ViewGroup.LayoutParams params = itemView.getLayoutParams();
    params.height = (int) (viewHeight * newValue);
    itemView.setLayoutParams(params);
  }
}
