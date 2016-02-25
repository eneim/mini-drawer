package im.ene.android.widget;

import android.support.annotation.FloatRange;
import android.view.View;

/**
 * Created by eneim on 2/23/16.
 */
public class HeaderNavViewHelper extends NavItemViewHelper {

  // NavigationMenuPresenter$SeparatorViewHolder
  // in this case, just use itemView

  public HeaderNavViewHelper(View itemView) {
    super(itemView);
  }

  // Since header view is highly customizable, it is recommended that client of this View extends
  // this class and override this method for custom behavior
  @Override protected void onDrawerOffset(@FloatRange(from = 0.f, to = 1.f) float offset) {
    if (itemView != null) {
      // do something
    }
  }
}
