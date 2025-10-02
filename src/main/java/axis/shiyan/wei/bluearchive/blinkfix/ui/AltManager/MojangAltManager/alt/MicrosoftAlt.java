package axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.MojangAltManager.alt;


import axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.MojangAltManager.AccountEnum;
import axis.shiyan.wei.bluearchive.blinkfix.ui.AltManager.MojangAltManager.Alt;

public final class MicrosoftAlt extends Alt {
    private final String refreshToken;

    public MicrosoftAlt(String userName,String refreshToken) {
        super(userName, AccountEnum.MICROSOFT);
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
