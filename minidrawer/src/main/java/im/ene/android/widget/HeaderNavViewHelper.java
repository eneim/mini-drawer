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

import android.support.annotation.FloatRange;
import android.view.View;

/**
 * Created by eneim on 2/23/16.
 */
public class HeaderNavViewHelper extends MenuItemHelper {

  // NavigationMenuPresenter$SeparatorViewHolder
  // in this case, just use itemView

  public HeaderNavViewHelper(View itemView) {
    super(itemView);
  }

  // Since header view is highly customizable, it is recommended that client of this View extends
  // this class and override this method for custom behavior
  @Override protected void onDrawerOffset(int groupId, int itemId,
      @FloatRange(from = 0.f, to = 1.f) float offset) {
    if (itemView != null) {
      // do something
    }
  }
}
