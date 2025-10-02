package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease;

import axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.receive.*;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.receive.*;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.send.WPFHwid;
import axis.shiyan.wei.bluearchive.blinkfix.protocols.api.netease.send.auth.AuthOtpRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static axis.shiyan.wei.bluearchive.blinkfix.protocols.api.NavenAPi.GSON;
public class NeteaseAccount {
    public CookiesData cookies;
    public LoginOtpResponse loginOtpResponse;
    public AuthOtpResponse authInfo;
    public final WPFHwid hwid;
    public AvailableQueryResponse servers;

    private final NeteaseApi api;

    private String userId;
    public NeteaseAccount(String cookiesData) {
        this(cookiesData, "暂未登录");
    }
    public NeteaseAccount(String cookiesData,String userId) {
        this.api = NeteaseApi.getInstance();
        this.cookies = new CookiesData(cookiesData);
        this.hwid = new WPFHwid(
                false,
                "windows",
                "Microsoft Windows 10",
                "34-C5-E9-AD-89-D0-4F-A7-8B-46-33-38-91-CB-C3-BB",
                "B7ED7F7CF052454BB5D1E73C2EAABF84",
                api.wpf_ver.version,
                "C977A029"
        );
        this.userId = userId;
    }

    public NeteaseAccount(NeteaseApi api, WPFHwid hwid, String cookiesData) {
        this(api, hwid, new CookiesData(cookiesData));
    }

    public NeteaseAccount(NeteaseApi api, WPFHwid hwid, CookiesData cookies) {
        this.api = api;
        this.cookies = cookies;
        this.hwid = hwid;
    }

    public void login() {
        this.loginOtpResponse = api.login_otp(this.cookies);
        AuthOtpRequest.Version version = new AuthOtpRequest.Version(api.wpf_ver.version);
        version.launcher_md5 = api.wpf_ver.md5;
        AuthOtpRequest authOtp = new AuthOtpRequest(this.hwid, this.cookies.sauth, version, this.loginOtpResponse);
        this.authInfo = api.auth_otp(authOtp);
    }

    public boolean isLogged() {
        return authInfo != null && authInfo.entity != null && authInfo.entity.token != null && !authInfo.entity.token.isEmpty();
    }
    

    public ServersRuntimeResponse queryServersRuntime(List<String> ids) {
        var response = api.search_by_items(authInfo, ids);
        return response;
    }

    public ServersRuntimeResponse queryServersRuntime(String... ids) {
        var response = api.search_by_items(authInfo, ids);
        return response;
    }

    public ServersTitleResponse queryServersIcon(String... ids) {
        var response = api.search_item_channel_list(authInfo, 24, ids);
        return response;
    }

    public void queryServersIcon(List<String> ids) {
        var response = api.search_item_channel_list(authInfo, 24, ids);
    }

    public GameCharactersResponse queryServerCharacters(String id, int type) {
        return api.get_game_user_characters(authInfo, id, type);
    }

    public List<AvailableQueryResponse.Server> queryServerList() {
        var response = api.available_query(this.authInfo, 50, 0);
        System.out.println(GSON.toJson(response));
        var list = new ArrayList<AvailableQueryResponse.Server>();
        if (response.entities != null) {
            list.addAll(response.entities);
        }
        list.sort(Comparator.comparingInt(c -> -Integer.parseInt(c.onlineCount)));
        this.servers = response;
        return list;
    }

    public AvailableQueryResponse.Server findServer(String id) {
        return this.servers.entities.stream().filter(c -> c.id.equals(id)).findFirst().orElse(null);
    }

    public String getEntityId() {
        if (isLogged())
            return this.authInfo.entity.entity_id;
        return userId;
    }

    public CreateCharacterResponse createGameCharacter(String serverId, String name, int game_type) {
        return api.create_game_character(authInfo, name, serverId, game_type);
    }

    public ServerAddressResponse queryServerAddress(String id) {
        return api.get_item_address(authInfo, id);
    }

    public ServerDownloadListData queryDownloadList(String... ids) {
        return api.get_item_download_list(authInfo,ids);
    }

    public ServerDownloadListData queryDownloadList(List<String> ids) {
        return api.get_item_download_list(authInfo,ids);
    }
}
