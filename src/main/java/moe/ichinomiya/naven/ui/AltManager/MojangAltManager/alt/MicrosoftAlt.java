package moe.ichinomiya.naven.ui.AltManager.MojangAltManager.alt;


import moe.ichinomiya.naven.ui.AltManager.MojangAltManager.AccountEnum;
import moe.ichinomiya.naven.ui.AltManager.MojangAltManager.Alt;

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
