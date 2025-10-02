package axis.shiyan.wei.bluearchive.blinkfix.protocols.api.utils.aes;

public enum PaddingMode {
    None("NoPadding"),
    PKCS5("PKCS5Padding"),
    Zero("ZeroPadding");

    public final String name;

    PaddingMode(String name) {
        this.name = name;
    }
}
