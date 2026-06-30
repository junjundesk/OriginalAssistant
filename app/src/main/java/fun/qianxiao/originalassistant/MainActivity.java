package fun.qianxiao.originalassistant;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

import fun.qianxiao.originalassistant.adapter.MyPageAdapter;
import fun.qianxiao.originalassistant.base.BaseActivity;
import fun.qianxiao.originalassistant.base.BaseFragment;
import fun.qianxiao.originalassistant.bean.AppInfo;
import fun.qianxiao.originalassistant.config.AppConfig;
import fun.qianxiao.originalassistant.databinding.ActivityMainBinding;
import fun.qianxiao.originalassistant.fragment.find.FindFragment;
import fun.qianxiao.originalassistant.fragment.me.MeFragment;
import fun.qianxiao.originalassistant.fragment.original.OriginalFragment;
import fun.qianxiao.originalassistant.fragment.test.TestFragment;
import fun.qianxiao.originalassistant.manager.CheckUpdateManager;
import fun.qianxiao.originalassistant.manager.PermissionManager;
import fun.qianxiao.originalassistant.manager.PrivacyPolicyManager;
import fun.qianxiao.originalassistant.utils.AppListTool;
import fun.qianxiao.originalassistant.utils.SettingPreferences;
import fun.qianxiao.originalassistant.view.loading.ILoadingView;
import fun.qianxiao.originalassistant.view.loading.MyLoadingDialog;

/**
 * MainActivity
 *
 * @author QianXiao
 * @since 2023/3/10
 */
public class MainActivity extends BaseActivity<ActivityMainBinding>
        implements ILoadingView, NetworkUtils.OnNetworkStatusChangedListener {
    private final String[] PAGES_TITLES = new String[]{"原创助手", "测试助手", "发现", "我的"};
    private final String[] PAGES_TAB_TEXTS = new String[]{"原创", "测试", "发现", "我的"};
    private final int[] NAV_ITEM_IDS = new int[]{R.id.nav_original, R.id.nav_test, R.id.nav_find, R.id.nav_me};
    private List<BaseFragment<?, MainActivity>> fragments = new ArrayList<>();
    private int currentPosition;
    private MyLoadingDialog loadingDialog;

    @Override
    protected void initListener() {
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                KeyboardUtils.hideSoftInput(getWindow());
            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                if (position >= 0 && position < NAV_ITEM_IDS.length
                        && binding.bottomNavigation.getSelectedItemId() != NAV_ITEM_IDS[position]) {
                    binding.bottomNavigation.setSelectedItemId(NAV_ITEM_IDS[position]);
                }
                if (position >= 0 && position < PAGES_TITLES.length) {
                    setTitle(PAGES_TITLES[position]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int position = getPositionByNavItemId(item.getItemId());
            if (position < 0) {
                return false;
            }
            if (binding.viewPager.getCurrentItem() != position) {
                KeyboardUtils.hideSoftInput(getWindow());
                binding.viewPager.setCurrentItem(position);
            }
            return true;
        });
        NetworkUtils.registerNetworkStatusChangedListener(this);
    }

    @Override
    protected void initData() {
        setSupportActionBar(binding.mainToolbar);
        setTitle(PAGES_TITLES[currentPosition]);
        preLoadAppList();
        initNetWorkState();
        PrivacyPolicyManager privacyPolicyManager = PrivacyPolicyManager.getInstance();
        if (!privacyPolicyManager.isAgreePrivacyPolicy()) {
            privacyPolicyManager.confrim(context, new PrivacyPolicyManager.OnPrivacyPolicyListener() {
                @Override
                public void onAgree() {
                    // requestPermission();
                    initAfterPolicy();
                }

                @Override
                public void onRefuse() {
                    AppUtils.exitApp();
                }

                private void requestPermission() {
                    if (!PermissionManager.getInstance().hasAllPermission()) {
                        PermissionManager.getInstance().requestNeeded();
                    }
                }
            });
        } else {
            initAfterPolicy();
        }
        initFragmentsAndTabData();
    }

    private void initAfterPolicy() {
        checkUpdateSilent();
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private void initNetWorkState() {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<Boolean>() {
            @Override
            public Boolean doInBackground() throws Throwable {
                return NetworkUtils.isAvailable();
            }

            @Override
            public void onSuccess(Boolean result) {
                AppConfig.isNetAvailable = result;
            }
        });
    }

    private void checkUpdateSilent() {
        CheckUpdateManager.getInstance().check(context, true);
    }

    private void initFragmentsAndTabData() {
        fragments.clear();
        fragments.add(new OriginalFragment());
        fragments.add(new TestFragment());
        fragments.add(new FindFragment());
        fragments.add(new MeFragment());
        MyPageAdapter adapter = new MyPageAdapter(getSupportFragmentManager(), fragments, PAGES_TAB_TEXTS);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(4);
        if (SettingPreferences.getBoolean(R.string.p_key_switch_auto_test_tab_when_enter_app)) {
            binding.viewPager.setCurrentItem(1, false);
        }
    }


    private int getPositionByNavItemId(int itemId) {
        for (int i = 0; i < NAV_ITEM_IDS.length; i++) {
            if (NAV_ITEM_IDS[i] == itemId) {
                return i;
            }
        }
        return -1;
    }

    private void preLoadAppList() {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<List<AppInfo>>() {
            @Override
            public List<AppInfo> doInBackground() throws Throwable {
                return AppListTool.getAppList();
            }

            @Override
            public void onSuccess(List<AppInfo> result) {

            }
        });
    }

    public void setTabNavigationHide(boolean hide) {
        if (hide) {
            binding.bottomNavigation.setVisibility(View.INVISIBLE);
        } else {
            binding.bottomNavigation.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void openLoadingDialog(String msg) {
        if (loadingDialog == null) {
            loadingDialog = new MyLoadingDialog(context);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.setMessage(msg);
            loadingDialog.show();
        } else {
            loadingDialog.updateMessage(msg);
        }
    }

    @Override
    public void closeLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * {@link NetworkUtils.OnNetworkStatusChangedListener}
     */
    @Override
    public void onDisconnected() {
        AppConfig.isNetAvailable = false;
    }

    /**
     * {@link NetworkUtils.OnNetworkStatusChangedListener}
     */
    @Override
    public void onConnected(NetworkUtils.NetworkType networkType) {
        AppConfig.isNetAvailable = true;
    }

    @Override
    public void onBackPressed() {
        if (fragments.get(currentPosition).onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtils.unregisterNetworkStatusChangedListener(this);
        KeyboardUtils.unregisterSoftInputChangedListener(getWindow());
    }
}
