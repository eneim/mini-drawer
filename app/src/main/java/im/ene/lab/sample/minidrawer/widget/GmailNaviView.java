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

package im.ene.lab.sample.minidrawer.widget;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import im.ene.android.widget.HeaderNavViewHelper;
import im.ene.android.widget.MenuItemHelper;
import im.ene.android.widget.NavigationView;
import im.ene.lab.sample.minidrawer.R;

/**
 * Created by eneim on 2/24/16.
 */
public class GmailNaviView extends NavigationView {
  public GmailNaviView(Context context) {
    super(context);
  }

  public GmailNaviView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public GmailNaviView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected MenuItemHelper getItemViewHelper(int viewType, View holder) {
    if (viewType == VIEW_TYPE_HEADER) {
      return new HeaderViewHelper(holder);
    }

    return super.getItemViewHelper(viewType, holder);
  }

  static class HeaderViewHelper extends HeaderNavViewHelper {

    @Nullable final ImageView mIcon;
    @Nullable final View mTitleContainer;

    private final Interpolator sAccelerate = new AccelerateInterpolator();

    private final int itemHeight;
    private final int contentHeight;
    private final int iconMaxSize;
    private final int iconMinSize;

    public HeaderViewHelper(View itemView) {
      super(itemView);
      mIcon = (ImageView) itemView.findViewById(R.id.icon);
      mTitleContainer = itemView.findViewById(R.id.title_container);

      itemHeight = itemView.getHeight();
      contentHeight = mTitleContainer.getHeight();

      ViewCompat.setPivotX(mIcon, 0);
      ViewCompat.setPivotY(mIcon, mIcon.getHeight());
      iconMaxSize = mIcon.getWidth();
      iconMinSize = itemView.getResources().getDimensionPixelSize(R.dimen.header_icon_size_min);
    }

    @Override protected void onDrawerOffset(int groupId, int itemId,
        @FloatRange(from = 0.f, to = 1.f) float offset) {
      super.onDrawerOffset(groupId, itemId, offset);
      float value = sAccelerate.getInterpolation(offset);
      // offset = 0 --> width = minSize;
      // offset = 1 --> width = maxSize;
      float scale = (iconMaxSize * value + iconMinSize * (1.f - value)) / iconMaxSize;
      if (mIcon != null) {
        ViewCompat.setScaleX(mIcon, scale);
        ViewCompat.setScaleY(mIcon, scale);
      }

      if (mTitleContainer != null) {
        ViewCompat.setAlpha(mTitleContainer, value);
      }

      ViewGroup.LayoutParams params = itemView.getLayoutParams();
      params.height = itemHeight - (int) (contentHeight * (1.f - value));
      itemView.setLayoutParams(params);
    }
  }
}
