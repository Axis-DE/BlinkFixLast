package axis.shiyan.wei.bluearchive.blinkfix.modules;

import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.render.ClickGUIModule;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.Notification;
import axis.shiyan.wei.bluearchive.blinkfix.ui.notification.NotificationLevel;
import axis.shiyan.wei.bluearchive.blinkfix.utils.SmoothAnimationTimer;
import axis.shiyan.wei.bluearchive.blinkfix.values.HasValue;
import net.minecraft.client.Minecraft;

@Getter
@NoArgsConstructor
public class Module extends HasValue {
    protected static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean update = true;
    private final SmoothAnimationTimer animation = new SmoothAnimationTimer(100);

    private String name;
    private String prettyName;
    private String description;
    private String suffix;
    private Category category;
    private boolean enabled;
    @Setter
    private int minPermission = 0;

    @Setter
    private int key;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;

        super.setName(name);
        setPrettyName();
    }

    public void setSuffix(String suffix) {
        if (suffix == null) {
            this.suffix = null;
            update = true;
        } else if (!suffix.equals(this.suffix)) {
            this.suffix = suffix;
            update = true;
        }
    }
//public void setSuffix(String suffix) {
//    //   if (suffix == null) {
//    //    this.suffix = null;
//    //  update = true;
//    //} else if (!suffix.equals(this.suffix)) {
//    this.suffix = "Biaozi";
//    update = true;
//}

    private void setPrettyName() {
        StringBuilder builder = new StringBuilder();
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (Character.isLowerCase(chars[i]) && Character.isUpperCase(chars[i + 1])) {
                builder.append(chars[i]).append(" ");
            } else {
                builder.append(chars[i]);
            }
        }
        builder.append(chars[chars.length - 1]);
        prettyName = builder.toString();
    }

    protected void initModule() {
        if (this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);

            this.name = moduleInfo.name();
            this.description = moduleInfo.description();
            this.category = moduleInfo.category();

            super.setName(name);
            setPrettyName();
            BlinkFix.getInstance().getHasValueManager().registerHasValue(this);
        }
    }

    public boolean onEnable() {
        return false;
    };

    public void onDisable() {};

    public void setEnabled(boolean enabled) {
        try {
            BlinkFix blinkFix = BlinkFix.getInstance();
            if (enabled) {
                this.enabled = true;

                blinkFix.getEventManager().register(this);
                onEnable();

                if (!(this instanceof ClickGUIModule) && mc.thePlayer != null) {
                    mc.thePlayer.playSound("random.click", 0.5F, 1.0F);

                    Notification notification = new Notification(NotificationLevel.SUCCESS, name + " Enabled!", 3000);
                    blinkFix.getNotificationManager().addNotification(notification);
                }
            } else {
                this.enabled = false;

                blinkFix.getEventManager().unregister(this);
                onDisable();

                if (!(this instanceof ClickGUIModule) && mc.thePlayer != null) {
                    mc.thePlayer.playSound("random.click", 0.5F, 0.8F);

                    Notification notification = new Notification(NotificationLevel.ERROR, name + " Disabled!", 3000);
                    blinkFix.getNotificationManager().addNotification(notification);
                }
            }
        } catch (Exception ignored) {

        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }
}
