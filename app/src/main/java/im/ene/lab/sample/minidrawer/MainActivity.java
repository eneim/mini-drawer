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

package im.ene.lab.sample.minidrawer;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import im.ene.android.widget.ActionBarDrawerToggle;
import im.ene.android.widget.MiniDrawerLayout;
import im.ene.android.widget.NavigationView;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  NavigationView mNavView;
  MiniDrawerLayout mDrawer;
  RecyclerView mRecyclerView;

  // View mCollapsedMenu;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mNavView = (NavigationView) findViewById(R.id.nav_view);
    mNavView.setNavigationItemSelectedListener(this);

    // mCollapsedMenu = findViewById(R.id.collapsed_menu);

    mDrawer = (MiniDrawerLayout) findViewById(R.id.drawer_layout);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    final ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
    mDrawer.setDrawerListener(new DrawerLayout.DrawerListener() {
      @Override public void onDrawerSlide(View drawerView, float slideOffset) {
        toggle.onDrawerSlide(drawerView, slideOffset);
        if (mNavView != null) {
          mNavView.onDrawerOffset(slideOffset);
        }

        //if (mCollapsedMenu != null) {
        //  mCollapsedMenu.setAlpha(
        //      1 - AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR.getInterpolation(slideOffset));
        //}
      }

      @Override public void onDrawerOpened(View drawerView) {
        toggle.onDrawerOpened(drawerView);
      }

      @Override public void onDrawerClosed(View drawerView) {
        toggle.onDrawerClosed(drawerView);
      }

      @Override public void onDrawerStateChanged(int newState) {
        toggle.onDrawerStateChanged(newState);
      }
    });

    toggle.syncState();

    // RecyclerView
    mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    mRecyclerView.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    mRecyclerView.setAdapter(new Adapter());
  }

  @Override public boolean onNavigationItemSelected(MenuItem item) {
    mDrawer.closeDrawer();
    return true;
  }

  private static class ViewHolder extends RecyclerView.ViewHolder {

    public ViewHolder(View itemView) {
      super(itemView);
    }

    public void bind() {
      ((TextView) itemView).setText(R.string.app_name);
    }
  }

  private static class Adapter extends RecyclerView.Adapter<ViewHolder> {

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new ViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(android.R.layout.simple_list_item_1, parent, false));
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      holder.bind();
    }

    @Override public int getItemCount() {
      return 200;
    }
  }
}
