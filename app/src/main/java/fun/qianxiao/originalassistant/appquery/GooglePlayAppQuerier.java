package fun.qianxiao.originalassistant.appquery;

import android.text.TextUtils;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fun.qianxiao.originalassistant.api.appquery.GooglePlayAppQueryApi;
import fun.qianxiao.originalassistant.bean.AnalysisResult;
import fun.qianxiao.originalassistant.manager.AppQueryManager;
import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;

/**
 * Google Play app query.
 *
 * Keep the original class name to reduce file churn. The implementation now
 * fetches Google Play details by package name.
 *
 * @Author QianXiao
 * @Date 2023/4/19
 */
public class GooglePlayAppQuerier extends AbstractAppQuerier<GooglePlayAppQueryApi, ResponseBody> {
    private static final String GOOGLE_PLAY_LANG = "zh_CN";
    private static final String GOOGLE_PLAY_COUNTRY = "US";
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile(
            "<div[^>]*data-g-id=[\"']description[\"'][^>]*>(.*?)</div>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern META_DESCRIPTION_PATTERN = Pattern.compile(
            "<meta[^>]*(?:name|property)=[\"'](?:description|og:description)[\"'][^>]*content=[\"']([^\"']*)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern SCREENSHOT_IMG_PATTERN = Pattern.compile(
            "<img\\b[^>]*data-screenshot-index=[\"']\\d+[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern OG_IMAGE_PATTERN = Pattern.compile(
            "<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']*)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?i).*(https?://|www\\.|\\b[a-z0-9][a-z0-9.-]*\\.(com|net|org|cn|io|co|me|app|dev|gg|tv|xyz|site|top|vip|club|info|biz|us|uk|jp|kr|de|fr|ru|br|in)(/|\\b)).*"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?i).*\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b.*"
    );
    private static final Pattern CONTACT_LINE_PATTERN = Pattern.compile(
            "(?i).*(联系方式|联系我|联系我们|联系作者|联系开发者|客服|邮箱|邮件|电子邮件|官网|官方网站|网站|网址|"
                    + "QQ\\s*群|QQ群|QQ群号|QQ\\s*[:：]?\\s*\\d{5,}|微信|公众号|WeChat|Telegram|Discord|WhatsApp|"
                    + "Facebook|Twitter|Instagram|TikTok|YouTube|Line\\s*[:：]|电话|手机号|Tel\\b|Phone\\b|Email\\b|E-mail\\b|"
                    + "Contact\\b|Support\\b).*"
    );

    @Override
    protected AppQueryManager.AppQueryChannel getFromChannel() {
        return AppQueryManager.AppQueryChannel.GOOGLE_PLAY;
    }

    @Override
    protected Observable<ResponseBody> search(String appName, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return Observable.error(new Exception(getApiName() + ": packageName is empty"));
        }
        return getApi().detail(packageName, GOOGLE_PLAY_LANG, GOOGLE_PLAY_COUNTRY);
    }

    @Override
    protected Observable<ResponseBody> searchResponseAnalysisAndDetail(ResponseBody searchResponse, AnalysisResult analysisResult) {
        return Observable.just(searchResponse);
    }

    @Override
    protected void detailResponseAnalysis(ResponseBody detailResponse, AnalysisResult analysisResult) {
        try {
            String html = detailResponse.string();
            String description = extractDescription(html);
            List<String> screenshots = extractScreenshots(html);

            if (TextUtils.isEmpty(description)) {
                analysisResult.setErrorMsg(analysisResult.getApi() + ": description not found");
                return;
            }

            analysisResult.getAppQueryResult().setAppIntroduction(description);
            analysisResult.getAppQueryResult().setAppPictures(screenshots);
            analysisResult.setSuccess(true);
        } catch (IOException e) {
            analysisResult.setErrorMsg(analysisResult.getApi() + ": " + e.getMessage());
        }
    }

    private String extractDescription(String html) {
        Matcher matcher = DESCRIPTION_PATTERN.matcher(html);
        if (matcher.find()) {
            return filterContactLines(htmlToPlainText(matcher.group(1)));
        }

        matcher = META_DESCRIPTION_PATTERN.matcher(html);
        if (matcher.find()) {
            return filterContactLines(StringEscapeUtils.unescapeHtml4(matcher.group(1)).trim());
        }
        return "";
    }

    private List<String> extractScreenshots(String html) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = SCREENSHOT_IMG_PATTERN.matcher(html);
        while (matcher.find()) {
            String imgTag = matcher.group();
            String src = extractAttribute(imgTag, "src");
            if (!TextUtils.isEmpty(src)) {
                result.add(StringEscapeUtils.unescapeHtml4(src));
            }
        }

        if (result.isEmpty()) {
            matcher = OG_IMAGE_PATTERN.matcher(html);
            if (matcher.find()) {
                result.add(StringEscapeUtils.unescapeHtml4(matcher.group(1)));
            }
        }
        return new ArrayList<>(result);
    }

    private String extractAttribute(String htmlTag, String attrName) {
        Pattern attrPattern = Pattern.compile(
                "\\b" + Pattern.quote(attrName) + "\\s*=\\s*([\"'])(.*?)\\1",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = attrPattern.matcher(htmlTag);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }

    private String htmlToPlainText(String html) {
        String text = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p\\s*>", "\n")
                .replaceAll("<[^>]+>", "");
        text = StringEscapeUtils.unescapeHtml4(text);
        text = text.replace('\u00A0', ' ');
        text = text.replaceAll("[ \\t\\x0B\\f\\r]+", " ");
        text = text.replaceAll("\\n[ \\t]+", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }

    private String filterContactLines(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimLine = line.trim();
            if (shouldDropIntroductionLine(trimLine)) {
                continue;
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.append('\n');
            }
            stringBuilder.append(trimLine);
        }
        return stringBuilder.toString().replaceAll("\\n{3,}", "\n\n").trim();
    }

    private boolean shouldDropIntroductionLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return false;
        }
        return line.contains("@")
                || URL_PATTERN.matcher(line).matches()
                || EMAIL_PATTERN.matcher(line).matches()
                || CONTACT_LINE_PATTERN.matcher(line).matches();
    }
}
