package im.ene.android.widget;

import android.support.annotation.FloatRange;
import android.view.View;

/**
 * Created by eneim on 2/23/16.
 */
public abstract class NavItemViewHelper {

  protected final View itemView;

  public NavItemViewHelper(View itemView) {
    this.itemView = itemView;
  }

  protected abstract void onDrawerOffset(@FloatRange(from = 0.f, to = 1.f) float offset);
}
