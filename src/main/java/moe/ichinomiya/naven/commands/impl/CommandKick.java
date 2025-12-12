//package moe.ichinomiya.naven.commands.impl;
//
//import com.google.gson.JsonObject;
//import moe.ichinomiya.naven.commands.Command;
//import moe.ichinomiya.naven.utils.ChatUtils;
//
//public class CommandKick extends Command {
//    public CommandKick() {
//        super("ikick", "Kick someone", new String[0]);
//    }
//
//    @Override
//    public void onCommand(String[] args) {
//        if (args.length == 0) {
//            ChatUtils.addChatMessage(".ikick <username> <optional reason>");
//            return;
//        }
//
//        if (!LiveClient.INSTANCE.isActive()) {
//            ChatUtils.addChatMessage("LiveClient is not active");
//            return;
//        }
//
//        boolean hasPermission = false;
//
//        if (LiveClient.INSTANCE.liveUser.getLevel() == dev.yalan.live.LiveUser.Level.ADMINISTRATOR) {
//            hasPermission = true;
//        }
//
//        String userRank = LiveClient.INSTANCE.liveUser.getRank();
//        if (userRank != null && userRank.equals("§eBeta")) {
//            hasPermission = true;
//        }
//
//        if (!hasPermission) {
//            ChatUtils.addChatMessage("You don't have permission to use this command");
//            return;
//        }
//
//        final String username = args[0];
//        final String reason;
//
//        if (args.length == 1) {
//            reason = "";
//        } else {
//            final StringBuilder sb = new StringBuilder();
//
//            for (int i = 1; i < args.length; i++) {
//                sb.append(args[i]);
//
//                if (i + 1 != args.length) {
//                    sb.append(' ');
//                }
//            }
//
//            reason = sb.toString();
//        }
//
//        final JsonObject payload = new JsonObject();
//        payload.addProperty("operator", LiveClient.INSTANCE.liveUser.getName());
//        payload.addProperty("reason", reason);
//
//        LiveClient.INSTANCE.sendPacket(LiveProto.createSendOperation("Kick", username, LiveClient.GSON.toJson(payload)));
//
//        ChatUtils.addChatMessage("尝试踢出: " + username);
//    }
//
//    @Override
//    public String[] onTab(String[] args) {
//        return new String[0];
//    }
//}
