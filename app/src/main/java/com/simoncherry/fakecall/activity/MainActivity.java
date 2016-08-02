package com.simoncherry.fakecall.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;
import com.shizhefei.view.viewpager.SViewPager;
import com.simoncherry.fakecall.R;
import com.simoncherry.fakecall.fragment.CallFragment;
import com.simoncherry.fakecall.fragment.ChatFragment;
import com.simoncherry.fakecall.fragment.ContactFragment;
import com.simoncherry.fakecall.fragment.FirstLayerFragment;


public class MainActivity extends FragmentActivity {

    private IndicatorViewPager indicatorViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SViewPager viewPager = (SViewPager) findViewById(R.id.tabmain_viewPager);
        Indicator indicator = (Indicator) findViewById(R.id.tabmain_indicator);
        indicator.setOnTransitionListener(new OnTransitionTextListener().setColor(Color.RED, Color.GRAY));
        indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
        indicatorViewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        // 禁止viewpager的滑动事件
        viewPager.setCanScroll(false);
        // 设置viewpager保留界面不重新加载的页面数量
        viewPager.setOffscreenPageLimit(4);
    }

    private class MyAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {
        private String[] tabNames = { "拨号", "联系人", "信息", "设置" };
        private int[] tabIcons = { R.drawable.maintab_1_selector, R.drawable.maintab_2_selector, R.drawable.maintab_3_selector,
                R.drawable.maintab_4_selector };
        private LayoutInflater inflater;

        public MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            inflater = LayoutInflater.from(getApplicationContext());
        }

        @Override
        public int getCount() {
            return tabNames.length;
        }

        @Override
        public View getViewForTab(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.tab_main, container, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(tabNames[position]);
            textView.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[position], 0, 0);
            return textView;
        }

        @Override
        public Fragment getFragmentForPage(int position) {
//            FirstLayerFragment mainFragment = new FirstLayerFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString(FirstLayerFragment.INTENT_STRING_TABNAME, tabNames[position]);
//            bundle.putInt(FirstLayerFragment.INTENT_INT_INDEX, position);
//            mainFragment.setArguments(bundle);
//            return mainFragment;

            switch (position) {
                case 0 :
                    CallFragment callFragment = new CallFragment();
                    return callFragment;
                case 1:
                    ContactFragment contactFragment = new ContactFragment();
                    return contactFragment;
                case 2:
                    ChatFragment chatFragment = new ChatFragment();
                    return  chatFragment;
                default:
                    FirstLayerFragment mainFragment = new FirstLayerFragment();
                    return mainFragment;
            }
        }
    }
}
