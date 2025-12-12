package moe.ichinomiya.naven.protocols.HuaYuTing.api.utils.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static moe.ichinomiya.naven.protocols.HuaYuTing.api.NavenAPi.GSON;

public class PathDataBuilder {
    public StringBuilder urlPath;

    private boolean first;

    public PathDataBuilder(String urlPath) {
        this.urlPath = new StringBuilder(urlPath);
        first = true;
    }

    public PathDataBuilder append(String header, String val) {
        if (first) {
            urlPath.append("?");
            first = false;
        } else urlPath.append("&");
        urlPath.append(URLEncoder.encode(header, StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(val, StandardCharsets.UTF_8));
        return this;
    }

    public PathDataBuilder append(String header, Number val) {
        return append(header, val.toString());
    }

    public PathDataBuilder appendJson(String header, Object obj) {
        return append(header, GSON.toJson(obj));
    }

    public PathDataBuilder appendCrypt(Function<String, String> function, String header, String obj) {
        return append(header, function.apply(obj));
    }

    public PathDataBuilder appendCryptJson(Function<String, String> function, String header, Object obj) {
        return appendCrypt(function, header, GSON.toJson(obj));
    }

    public PathDataBuilder appendCrypt(Function<String, String> function, String header, Number val) {
        return appendCrypt(function, header, val.toString());
    }

    public String get() {
        return urlPath.toString();
    }
}
