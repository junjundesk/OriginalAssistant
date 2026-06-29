package fun.qianxiao.originalassistant.api.appquery;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Google Play app query api.
 *
 * @Author QianXiao
 * @Date 2023/4/19
 */
public interface GooglePlayAppQueryApi extends AppQueryaApi {
    String API_NAME = "GooglePlay";

    /**
     * Google Play app detail page.
     *
     * @param packageName package name
     * @param lang        language, such as zh_CN
     * @param country     country, such as US
     * @return {@link Observable<ResponseBody>}
     */
    @Headers("App-Query: true")
    @GET("https://play.google.com/store/apps/details")
    Observable<ResponseBody> detail(@Query("id") String packageName, @Query("hl") String lang, @Query("gl") String country);
}
