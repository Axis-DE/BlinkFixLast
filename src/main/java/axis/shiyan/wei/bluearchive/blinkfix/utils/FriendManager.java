package axis.shiyan.wei.bluearchive.blinkfix.utils;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FriendManager {
    @Getter
    private static final List<String> friends = new CopyOnWriteArrayList<>();

    public static boolean isFriend(Entity player) {
        if (!(player instanceof EntityPlayer)) {
            return false;
        }

        return friends.contains(player.getName());
    }

    public static boolean isFriend(String player) {
        return friends.contains(player);
    }

    public static void addFriend(EntityLivingBase player) {
        friends.add(player.getName());
    }

    public static void addFriend(String name) {
        friends.add(name);
    }

    public static void removeFriend(EntityLivingBase player) {
        friends.remove(player.getName());
    }
}
