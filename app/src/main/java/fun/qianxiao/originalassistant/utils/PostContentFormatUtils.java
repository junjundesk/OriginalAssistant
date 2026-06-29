package fun.qianxiao.originalassistant.utils;

import android.os.Build;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;

import fun.qianxiao.originalassistant.R;
import fun.qianxiao.originalassistant.bean.PostInfo;
import fun.qianxiao.originalassistant.config.Constants;
import fun.qianxiao.originalassistant.config.SPConstants;

/**
 * PostContentFormatUtils
 *
 * @Author QianXiao
 * @Date 2023/3/12
 */
public class PostContentFormatUtils {
    public static final String KEY_FIELD_NAME_APP_NAME = "KEY_FIELD_NAME_APP_NAME";
    public static final String KEY_FIELD_NAME_APP_CHINESE_NAME = "KEY_FIELD_NAME_APP_CHINESE_NAME";
    public static final String KEY_FIELD_NAME_APP_LANGUAGE = "KEY_FIELD_NAME_APP_LANGUAGE";
    public static final String KEY_FIELD_NAME_APP_SIZE = "KEY_FIELD_NAME_APP_SIZE";
    public static final String KEY_FIELD_NAME_APP_VERSION = "KEY_FIELD_NAME_APP_VERSION";
    public static final String KEY_FIELD_NAME_APP_PACKAGE_NAME = "KEY_FIELD_NAME_APP_PACKAGE_NAME";
    public static final String KEY_FIELD_NAME_APP_VERSION_CODE = "KEY_FIELD_NAME_APP_VERSION_CODE";
    public static final String KEY_FIELD_NAME_APP_SYSTEM_VERSION = "KEY_FIELD_NAME_APP_SYSTEM_VERSION";
    public static final String KEY_FIELD_NAME_APP_SPECIAL_INSTRUCTIONS = "KEY_FIELD_NAME_APP_SPECIAL_INSTRUCTIONS";
    public static final String KEY_FIELD_NAME_APP_INTRODUCTION = "KEY_FIELD_NAME_APP_INTRODUCTION";
    public static final String KEY_FIELD_NAME_APP_DOWNLOAD_URL = "KEY_FIELD_NAME_APP_DOWNLOAD_URL";

    public static final String FIELD_NAME_APP_NAME = "【游戏名称】";
    public static final String FIELD_NAME_APP_CHINESE_NAME = "【中文名称】";
    public static final String FIELD_NAME_APP_LANGUAGE = "【游戏语言】";
    public static final String FIELD_NAME_APP_SIZE = "【游戏大小】";
    public static final String FIELD_NAME_APP_VERSION = "【游戏版本】";
    public static final String FIELD_NAME_APP_PACKAGE_NAME = "【游戏包名】";
    public static final String FIELD_NAME_APP_VERSION_CODE = "【开发代号】";
    public static final String FIELD_NAME_APP_SYSTEM_VERSION = "【系统版本】";
    public static final String FIELD_NAME_APP_SPECIAL_INSTRUCTIONS = "【特殊说明】";
    public static final String FIELD_NAME_APP_INTRODUCTION = "【游戏简介】";
    public static final String FIELD_NAME_APP_DOWNLOAD_URL = "【下载地址】";
    public static final String FIELD_NAME_GAME_REGION = "【游戏地区】";
    public static final String FIELD_VALUE_GAME_REGION_NON_MAINLAND_CHINA = "非中国大陆";

    public static final String FIELD_SEPARATOR = "\n";
    public static final String FIELD_SEPARATOR_DOUBLE = "\n\n";

    public static final String TITLE_FORMAT_DEFAULT = Utils.getApp().getString(R.string.post_title_format_default);
    private static final String TITLE_RANK_PREFIX = "【榜】";
    private static final String TITLE_ORIGINAL_TAG_REGEX = "【[^】]*原创】";
    private static final String TITLE_GAME_RECOMMEND_TAG = "【手游推荐】";
    private static final String UC_DRIVE_DOMAIN = "drive.uc.cn";

    public static String getFormatTitle(PostInfo postInfo) {
        StringBuilder stringBuilder = getBaseFormatTitle(postInfo);
        if (SettingPreferences.getBoolean(R.string.p_key_switch_title_rank_prefix)
                && stringBuilder.length() > 0
                && stringBuilder.indexOf(TITLE_RANK_PREFIX) != 0) {
            stringBuilder.insert(0, TITLE_RANK_PREFIX);
        }
        return stringBuilder.toString();
    }

    public static String getGameFormatTitle(PostInfo postInfo) {
        return getBaseFormatTitle(postInfo).toString()
                .replaceAll(TITLE_ORIGINAL_TAG_REGEX, TITLE_GAME_RECOMMEND_TAG);
    }

    private static StringBuilder getBaseFormatTitle(PostInfo postInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        if (SettingPreferences.getBoolean(R.string.p_key_switch_title)) {
            String teamName = SettingPreferences.getString(R.string.p_key_team_name, "");
            String titleFormat = SettingPreferences.getString(R.string.p_key_title_format, TITLE_FORMAT_DEFAULT);
            stringBuilder.append(String.format(titleFormat, teamName, getTitleAppName(postInfo), postInfo.getAppVersionName()));
        }
        return stringBuilder;
    }

    private static CharSequence getTitleAppName(PostInfo postInfo) {
        if (!TextUtils.isEmpty(postInfo.getAppChineseName())) {
            return postInfo.getAppChineseName();
        }
        return postInfo.getAppName();
    }

    public static String getFormatDetail(PostInfo postInfo) {
        String fieldNameAppName = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_NAME, FIELD_NAME_APP_NAME);
        String fieldNameAppChineseName = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_CHINESE_NAME, FIELD_NAME_APP_CHINESE_NAME);
        String fieldNameAppLanguage = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_LANGUAGE, FIELD_NAME_APP_LANGUAGE);
        String fieldNameAppSize = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_SIZE, FIELD_NAME_APP_SIZE);
        String fieldNameAppVersion = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_VERSION, FIELD_NAME_APP_VERSION);
        String fieldNameAppPackageName = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_PACKAGE_NAME, FIELD_NAME_APP_PACKAGE_NAME);
        String fieldNameAppVersionCode = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_VERSION_CODE, FIELD_NAME_APP_VERSION_CODE);
        String fieldNameAppSystemVersion = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_SYSTEM_VERSION, FIELD_NAME_APP_SYSTEM_VERSION);
        String fieldNameAppSpecialInstructions = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_SPECIAL_INSTRUCTIONS, FIELD_NAME_APP_SPECIAL_INSTRUCTIONS);
        String fieldNameAppIntroduction = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_INTRODUCTION, FIELD_NAME_APP_INTRODUCTION);
        String fieldNameAppDownloadUrl = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_DOWNLOAD_URL, FIELD_NAME_APP_DOWNLOAD_URL);

        if (SPUtils.getInstance().getInt(SPConstants.KEY_APP_MODE, Constants.APP_MODE_GAME) == Constants.APP_MODE_SOFTWARE) {
            fieldNameAppName = fieldNameAppName.replace("游戏", "软件");
            fieldNameAppLanguage = fieldNameAppLanguage.replace("游戏", "软件");
            fieldNameAppSize = fieldNameAppSize.replace("游戏", "软件");
            fieldNameAppVersion = fieldNameAppVersion.replace("游戏", "软件");
            fieldNameAppPackageName = fieldNameAppPackageName.replace("游戏", "软件");
            fieldNameAppVersionCode = fieldNameAppVersionCode.replace("游戏", "软件");
            fieldNameAppSystemVersion = fieldNameAppSystemVersion.replace("游戏", "软件");
            fieldNameAppSpecialInstructions = fieldNameAppSpecialInstructions.replace("游戏", "软件");
            fieldNameAppIntroduction = fieldNameAppIntroduction.replace("游戏", "软件");
            fieldNameAppDownloadUrl = fieldNameAppDownloadUrl.replace("游戏", "软件");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fieldNameAppName).append(postInfo.getAppName()).append(FIELD_SEPARATOR_DOUBLE);
        appendChineseNameFieldIfNotEmpty(stringBuilder, fieldNameAppChineseName, postInfo);
        stringBuilder.append(fieldNameAppLanguage).append(postInfo.getAppLanguage()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppSize).append(postInfo.getAppSize()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppVersion).append(postInfo.getAppVersionName()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppPackageName).append(postInfo.getAppPackageName()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppVersionCode).append(postInfo.getAppVersionCode()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppSystemVersion).append(Build.VERSION.RELEASE).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppSpecialInstructions).append(FIELD_SEPARATOR).append(postInfo.getAppSpecialInstructions()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppIntroduction).append(postInfo.getAppIntroduction()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppDownloadUrl).append(FIELD_SEPARATOR).append(postInfo.getAppDownloadUrl());

        return stringBuilder.toString();
    }

    public static String getGameFormatDetail(PostInfo postInfo) {
        String fieldNameAppName = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_NAME, FIELD_NAME_APP_NAME);
        String fieldNameAppChineseName = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_CHINESE_NAME, FIELD_NAME_APP_CHINESE_NAME);
        String fieldNameAppLanguage = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_LANGUAGE, FIELD_NAME_APP_LANGUAGE);
        String fieldNameAppSize = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_SIZE, FIELD_NAME_APP_SIZE);
        String fieldNameAppVersion = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_VERSION, FIELD_NAME_APP_VERSION);
        String fieldNameAppPackageName = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_PACKAGE_NAME, FIELD_NAME_APP_PACKAGE_NAME);
        String fieldNameAppVersionCode = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_VERSION_CODE, FIELD_NAME_APP_VERSION_CODE);
        String fieldNameAppSystemVersion = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_SYSTEM_VERSION, FIELD_NAME_APP_SYSTEM_VERSION);
        String fieldNameAppSpecialInstructions = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_SPECIAL_INSTRUCTIONS, FIELD_NAME_APP_SPECIAL_INSTRUCTIONS);
        String fieldNameAppIntroduction = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_INTRODUCTION, FIELD_NAME_APP_INTRODUCTION);
        String fieldNameAppDownloadUrl = SPUtils.getInstance().getString(KEY_FIELD_NAME_APP_DOWNLOAD_URL, FIELD_NAME_APP_DOWNLOAD_URL);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fieldNameAppName).append(postInfo.getAppName()).append(FIELD_SEPARATOR_DOUBLE);
        appendChineseNameFieldIfNotEmpty(stringBuilder, fieldNameAppChineseName, postInfo);
        stringBuilder.append(fieldNameAppLanguage).append(postInfo.getAppLanguage()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppPackageName).append(postInfo.getAppPackageName()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(FIELD_NAME_GAME_REGION).append(FIELD_VALUE_GAME_REGION_NON_MAINLAND_CHINA).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppSize).append(postInfo.getAppSize()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppVersion).append(postInfo.getAppVersionName()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppVersionCode).append(postInfo.getAppVersionCode()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppSystemVersion).append(Build.VERSION.RELEASE).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppSpecialInstructions).append(FIELD_SEPARATOR).append(postInfo.getAppSpecialInstructions()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppIntroduction).append(postInfo.getAppIntroduction()).append(FIELD_SEPARATOR_DOUBLE);
        stringBuilder.append(fieldNameAppDownloadUrl).append(FIELD_SEPARATOR).append(filterUcDriveDownloadUrl(postInfo.getAppDownloadUrl()));

        return stringBuilder.toString();
    }

    private static String filterUcDriveDownloadUrl(CharSequence downloadUrl) {
        if (downloadUrl == null) {
            return "";
        }
        String[] lines = downloadUrl.toString().split("\\r?\\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            if (line.toLowerCase(java.util.Locale.ROOT).contains(UC_DRIVE_DOMAIN)) {
                continue;
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.append(FIELD_SEPARATOR);
            }
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    private static void appendChineseNameFieldIfNotEmpty(StringBuilder stringBuilder, String fieldNameAppChineseName, PostInfo postInfo) {
        if (!TextUtils.isEmpty(postInfo.getAppChineseName())) {
            stringBuilder.append(fieldNameAppChineseName).append(postInfo.getAppChineseName()).append(FIELD_SEPARATOR_DOUBLE);
        }
    }
}
