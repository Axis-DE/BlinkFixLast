package moe.ichinomiya.naven.protocols.HuaYuTing.api.netease;

import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive.*;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.objs.WPFVersionInfo;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.receive.*;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.*;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.auth.AuthOtpRequest;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.auth.AuthUpdateRequest;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.netease.send.auth.CreateGameCharacterRequest;
import moe.ichinomiya.naven.protocols.HuaYuTing.api.utils.http.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class NeteaseApi {
    private static final Gson GSON = new GsonBuilder().create();
    private static final String API_LOGIN_OTP = "/login-otp";
    private static final String API_AUTH_OTP = "/authentication-otp";
    private static final String API_AUTH_UPDATE = "/authentication/update";
    private static final String API_AVAILABLE_QUERY = "/item/query/available";
    private static final String API_SEARCH_BY_ITEMS = "/item-mc-version/query/search-by-items";
    private static final String API_SEARCH_ITEM_CHANNEL_LIST_BY_ID = "/item-channel/query/search-item-channel-list-by-id";
    private static final String API_GET_ITEM_ADDRESS = "/item-address/get";
    private static final String API_GET_USER_GAME_CHARS = "/game-character/query/user-game-characters";
    private static final String API_CREATE_USER_GAME_CHARS = "/game-character";
    private static final String API_ITEM_DOWNLOAD_LIST = "/user-item-download-v2/get-list";
    private static final String API_SEARCH_AUTH_ITEM_LIST = "/game-auth-item-list/query/search-by-game";

    private static final String API_WPF_VERSIONS = "/pl/x19_java_patchlist";

    public enum ReqType {

        User("https://x19obtcore.nie.netease.com:8443"),
        Data("https://x19mclobt.nie.netease.com"),
        Update("https://x19.update.netease.com");

        public final String apiUrl;

        ReqType(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String toFullUrl(String path) {
            return this.apiUrl + (path.startsWith("/") ? path : "/" + path);
        }
    }

    private static WPFVersionInfo WPF_VERSION;

    private static NeteaseApi INSTANCE = null;

    public final WPFVersionInfo wpf_ver;

    private NeteaseApi(WPFVersionInfo wpf_ver) {
        this.wpf_ver = wpf_ver;
    }
    public static NeteaseApi getInstance(){
        if (INSTANCE == null){
            INSTANCE = NeteaseApi.create();
        }
        return INSTANCE;
    }
    public static NeteaseApi create() {
        if (WPF_VERSION == null) {
            var current = "1.14.18.24399";
            var currentMD5 = "784594ba3994c1d1390947ad026497cd";
            try {
                var request = Http.to(Http.Method.GET, ReqType.Update.toFullUrl(API_WPF_VERSIONS))
                        .userAgent("WPFLauncher/" + current)
                        .acceptAll();

                var data = request.sendString();
                var json = "{" + data.substring(0, data.length() - 2) + "}";
                var obj = (new JsonParser()).parse(json).getAsJsonObject();
                var keys = new ArrayList<String>();

                for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                    keys.add(e.getKey());
                }

                current = keys.get(keys.size() - 1);
                currentMD5 = obj.get(current).getAsJsonObject().get("md5").getAsString();
            } catch (Exception e) {
            }

            WPF_VERSION = new WPFVersionInfo(current, currentMD5);
        }

        return new NeteaseApi(WPF_VERSION);
    }

    private Http.Request request(Http.Method method, ReqType type, String path) {
        return Http.to(method, type.toFullUrl(path))
                .userAgent("WPFLauncher/" + wpf_ver)
                .acceptAll();
    }

    private Http.Request post(ReqType type, String path) {
        return request(Http.Method.POST, type, path);
    }

    private Http.Request get(ReqType type, String path) {
        return request(Http.Method.GET, type, path);
    }

    public LoginOtpResponse login_otp(CookiesData data) {
        return post(ReqType.User, API_LOGIN_OTP)
                .bodyJson(data)
                .sendJson(LoginOtpResponse.class);
    }

    public AuthOtpResponse auth_otp(AuthOtpRequest data) {
        return post(ReqType.User, API_AUTH_OTP)
                .bodyCrypt(X19Crypt::encrypt, data)
                .sendCryptJson(X19Crypt::decrypt, AuthOtpResponse.class);
    }

    public AuthOtpResponse auth_update(AuthOtpResponse auth) {
        var obj = new AuthUpdateRequest(auth.entity.entity_id);
        return post(ReqType.User, API_AUTH_UPDATE)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_AUTH_UPDATE, obj))
                .bodyCrypt(X19Crypt::encrypt, obj)
                .sendCryptJson(X19Crypt::decrypt, AuthOtpResponse.class);
    }

    public AvailableQueryResponse available_query(AuthOtpResponse auth, int len, int offset) {
        var obj = new ItemQueryAvailableRequest(offset, len);
        return post(ReqType.Data, API_AVAILABLE_QUERY)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_AVAILABLE_QUERY, obj))
                .bodyJson(obj)
                .sendJson(AvailableQueryResponse.class);
    }

    public ServersTitleResponse search_item_channel_list(AuthOtpResponse auth, int channel_id, String... items) {
        var obj = new ItemSearchChannelRequest(channel_id, items);
        return post(ReqType.Data, API_SEARCH_ITEM_CHANNEL_LIST_BY_ID)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_SEARCH_ITEM_CHANNEL_LIST_BY_ID, obj))
                .bodyJson(obj)
                .sendJson(ServersTitleResponse.class);
    }
    
    public ServersTitleResponse search_item_channel_list(AuthOtpResponse auth, int channel_id, List<String> items) {
        var obj = new ItemSearchChannelRequest(channel_id, items);
        return post(ReqType.Data, API_SEARCH_ITEM_CHANNEL_LIST_BY_ID)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_SEARCH_ITEM_CHANNEL_LIST_BY_ID, obj))
                .bodyJson(obj)
                .sendJson(ServersTitleResponse.class);
    }

    public ServersRuntimeResponse search_by_items(AuthOtpResponse auth, String... items) {
        var obj = new SearchByItemsRequest(items);
        return post(ReqType.Data, API_SEARCH_BY_ITEMS)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_SEARCH_BY_ITEMS, obj))
                .bodyJson(obj)
                .sendJson(ServersRuntimeResponse.class);
    }

    public ServersRuntimeResponse search_by_items(AuthOtpResponse auth, List<String> items) {
        var obj = new SearchByItemsRequest(items);
        return post(ReqType.Data, API_SEARCH_BY_ITEMS)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_SEARCH_BY_ITEMS, obj))
                .bodyJson(obj)
                .sendJson(ServersRuntimeResponse.class);
    }

    public ServerAddressResponse get_item_address(AuthOtpResponse auth, String item_id) {
        var obj = new GetItemAddressRequest(item_id);
        return post(ReqType.Data, API_GET_ITEM_ADDRESS)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_GET_ITEM_ADDRESS, obj))
                .bodyJson(obj)
                .sendJson(ServerAddressResponse.class);
    }

    public GameCharactersResponse get_game_user_characters(AuthOtpResponse auth, String game_id, int game_type) {
        var obj = new GetUserCharactersRequest(game_id, String.valueOf(auth.entity.entity_id), game_type);
        return post(ReqType.Data, API_GET_USER_GAME_CHARS)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_GET_USER_GAME_CHARS, obj))
                .bodyJson(obj)
                .sendJson(GameCharactersResponse.class);
    }

    public CreateCharacterResponse create_game_character(AuthOtpResponse auth, String name, String game_id, int game_type) {
        var obj = new CreateGameCharacterRequest(game_id, game_type, name, String.valueOf(auth.entity.entity_id));
        return post(ReqType.Data, API_CREATE_USER_GAME_CHARS)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_CREATE_USER_GAME_CHARS, obj))
                .bodyJson(obj)
                .sendJson(CreateCharacterResponse.class);
    }

    public ServerDownloadListData get_item_download_list(AuthOtpResponse auth, String... ids) {
        var obj = new ItemDownloadListRequest(ids);
        return post(ReqType.Data, API_ITEM_DOWNLOAD_LIST)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_ITEM_DOWNLOAD_LIST, obj))
                .bodyJson(obj)
                .sendJson(ServerDownloadListData.class);
    }

    public ServerDownloadListData get_item_download_list(AuthOtpResponse auth, List<String> ids) {
        var obj = new ItemDownloadListRequest(ids);
        return post(ReqType.Data, API_ITEM_DOWNLOAD_LIST)
                .header("user-id", auth.entity.entity_id)
                .header("user-token", newToken(auth, API_ITEM_DOWNLOAD_LIST, obj))
                .bodyJson(obj)
                .sendJson(ServerDownloadListData.class);
    }

    private String newToken(AuthOtpResponse auth, String url, Object body) {
        return X19Crypt.computeDynamicToken(auth.entity.token, url, GSON.toJson(body));
    }
}
