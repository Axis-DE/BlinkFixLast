//package moe.ichinomiya.naven.commands.impl;
//
//import com.google.gson.JsonObject;
//import moe.ichinomiya.naven.commands.Command;
//import moe.ichinomiya.naven.utils.ChatUtils;
//import dev.yalan.live.LiveClient;
//import dev.yalan.live.netty.LiveProto;
//
//public class CommandCrash extends Command {
//    public CommandCrash() {
//        super("icrash", "Crash someone", new String[0]);
//    }
//
//    @Override
//    public void onCommand(String[] args) {
//        if (args.length == 0) {
//            ChatUtils.addChatMessage(".icrash <username> <optional reason>");
//            return;
//        }
//
//        if (!LiveClient.INSTANCE.isActive()) {
//            ChatUtils.addChatMessage("LiveClient is not active");
//            return;
//        }
//
//        // Check if user has permission to use this command
//        // Allow if level is Administrator OR rank is §eBeta
//        boolean hasPermission = false;
//
//        // Check level permission
//        if (LiveClient.INSTANCE.liveUser.getLevel() == dev.yalan.live.LiveUser.Level.ADMINISTRATOR) {
//            hasPermission = true;
//        }
//
//        // Check rank permission
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
//        LiveClient.INSTANCE.sendPacket(LiveProto.createSendOperation("Crash", username, LiveClient.GSON.toJson(payload)));
//
//        ChatUtils.addChatMessage("尝试崩溃: " + username);
//    }
//
//    @Override
//    public String[] onTab(String[] args) {
//        return new String[0];
//    }
//}
