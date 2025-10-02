//package axis.shiyan.wei.bluearchive.blinkfix.modules.impl.misc;
//
//import axis.shiyan.wei.bluearchive.blinkfix.BlinkFix;
//import axis.shiyan.wei.bluearchive.blinkfix.events.api.EventTarget;
//import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.EventType;
//import axis.shiyan.wei.bluearchive.blinkfix.events.api.types.Priority;
//import axis.shiyan.wei.bluearchive.blinkfix.events.impl.*;
//import axis.shiyan.wei.bluearchive.blinkfix.modules.Category;
//import axis.shiyan.wei.bluearchive.blinkfix.modules.Module;
//import axis.shiyan.wei.bluearchive.blinkfix.modules.ModuleInfo;
//import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.FlyMeToTheMoon;
//import axis.shiyan.wei.bluearchive.blinkfix.modules.impl.move.Stuck;
//import axis.shiyan.wei.bluearchive.blinkfix.ui.cooldown.CooldownBar;
//import axis.shiyan.wei.bluearchive.blinkfix.utils.ChatUtils;
//import axis.shiyan.wei.bluearchive.blinkfix.utils.ServerUtils;
//import axis.shiyan.wei.bluearchive.blinkfix.values.ValueBuilder;
//import axis.shiyan.wei.bluearchive.blinkfix.values.impl.BooleanValue;
//import lombok.extern.log4j.Log4j2;
//import net.minecraft.client.entity.EntityOtherPlayerMP;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.projectile.EntityArrow;
//import net.minecraft.entity.projectile.EntityThrowable;
//import net.minecraft.item.ItemFood;
//import net.minecraft.network.Packet;
//import net.minecraft.network.play.INetHandlerPlayClient;
//import net.minecraft.network.play.INetHandlerPlayServer;
//import net.minecraft.network.play.client.*;
//import net.minecraft.network.play.server.*;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//@Log4j2
//@ModuleInfo(name = "Disabler", description = "Disable some checks of anticheats.", category = Category.MISC)
//public class Disabler extends Module {
//    private final BooleanValue logging = ValueBuilder.create(this, "Logging").setDefaultBooleanValue(false).build().getBooleanValue();
//
//    public final Queue<Queue<Packet<INetHandlerPlayClient>>> delayedServerPackets = new ConcurrentLinkedQueue<>();
//    public final Queue<Packet<INetHandlerPlayServer>> delayedClientPackets = new ConcurrentLinkedQueue<>();
//    private final Queue<Packet<INetHandlerPlayClient>> releasingPackets = new ConcurrentLinkedQueue<>();
//    private Queue<Packet<INetHandlerPlayClient>> lastServerPackets;
//
//    public static boolean disabled = false;
//    private boolean added = false;
//    private final Map<Integer, EntityOtherPlayerMP> fakePlayers = new ConcurrentHashMap<>();
//
//    private final CooldownBar cooldownBar = new CooldownBar(1000, "Time Balance") {
//        @Override
//        public float getState() {
//            return 1F - (delayedServerPackets.size() / 190f);
//        }
//    };
//
//    private static final List<Class<? extends Packet<INetHandlerPlayClient>>> whitelistPackets = Arrays.asList(
//            S06PacketUpdateHealth.class,
//            S3EPacketTeams.class,
//            S3DPacketDisplayScoreboard.class,
//            S29PacketSoundEffect.class,
//            S47PacketPlayerListHeaderFooter.class,
//            S3BPacketScoreboardObjective.class,
//            S44PacketWorldBorder.class,
//            S21PacketChunkData.class,
//            S03PacketTimeUpdate.class,
//            S19PacketEntityStatus.class,
//            S02PacketChat.class,
//            S45PacketTitle.class,
//            S2APacketParticles.class,
//            S1FPacketSetExperience.class,
//            S3CPacketUpdateScore.class,
//            S3FPacketCustomPayload.class,
//            S37PacketStatistics.class,
//            S07PacketRespawn.class,
//            S3APacketTabComplete.class,
//            S49PacketUpdateEntityNBT.class,
//            S2CPacketSpawnGlobalEntity.class,
//            S01PacketJoinGame.class,
//            S0EPacketSpawnObject.class
//    );
//
//    @Override
//    public void onDisable() {
//        disabled = false;
//        releaseAllServerPackets();
//    }
//
//    @Override
//    public boolean onEnable() {
//        delayedServerPackets.clear();
//        delayedClientPackets.clear();
//        return false;
//    }
//
//    @EventTarget
//    public void onRespawn(EventRespawn e) {
//        delayedClientPackets.clear();
//        fakePlayers.clear();
//        worldTick = 0;
//        playerTick = 0;
//        positions.clear();
//
//        if (e.getType() == EventType.JOIN_GAME) {
//            mc.getNetHandler().playerInfoMap.clear();
//            delayedServerPackets.clear();
//        }
//    }
//
//    @EventTarget
//    public void onRenderEntity(EventRenderEntity e) {
//        Stuck stuck = (Stuck) BlinkFix.getInstance().getModuleManager().getModule(Stuck.class);
//        if (e.getEntity() instanceof EntityOtherPlayerMP && ((EntityOtherPlayerMP) e.getEntity()).isFakePlayer() && delayedServerPackets.size() > 1 && !stuck.isEnabled()) {
//            e.setCancelled(false);
//        }
//    }
//
//    @EventTarget
//    public void onRenderEntity(EventRendererLivingEntity e) {
//        if (e.getEntity() instanceof EntityOtherPlayerMP && ((EntityOtherPlayerMP) e.getEntity()).isFakePlayer()) {
//            e.setAlpha(0.5f);
//            e.setShouldInvisible(true);
//        }
//    }
//
//    private boolean shouldDisable() {
//        FlyMeToTheMoon module = (FlyMeToTheMoon) BlinkFix.getInstance().getModuleManager().getModule(FlyMeToTheMoon.class);
//        return mc.thePlayer != null && mc.thePlayer.ticksExisted > 20 && ServerUtils.getGrimTransactionCount() > 50 && !mc.thePlayer.isDead && !mc.thePlayer.isSpectator() && !mc.isSingleplayer() && (!module.isEnabled() || module.ticks < 30);
//    }
//
//    @EventTarget(Priority.LOWEST)
//    public void onTicks(EventRunTicks e) {
//        if (mc.thePlayer == null) {
//            delayedServerPackets.clear();
//        }
//
//        if (shouldDisable()) {
//            if (!added) {
//                setSuffix(delayedServerPackets.size() + " Ticks Delayed");
//                synchronized (delayedServerPackets) {
//                    delayedServerPackets.add(lastServerPackets = new ConcurrentLinkedQueue<>());
//                }
//            }
//        } else {
//            setSuffix("Stopped");
//        }
//
//        added = false;
//
//        if (delayedServerPackets.size() > 1) {
//            cooldownBar.setCreateTime(System.currentTimeMillis());
//            BlinkFix.getInstance().getCooldownBarManager().addBar(cooldownBar);
//        }
//    }
//
//    @EventTarget
//    public void onMotion(EventMotion e) {
//        if (e.getType() == EventType.PRE) {
//            if (shouldDisable()) {
//                if (!BlinkFix.getInstance().getModuleManager().getModule(TimeBalanceAbuse.class).isEnabled()) {
//                    if (delayedServerPackets.size() > 1) {
//                        mc.thePlayer.timer = 3;
//                    } else {
//                        mc.thePlayer.timer = 1;
//                    }
//                }
//            }
//        }
//    }
//
//    private void log(String message) {
//        if (logging.getCurrentValue()) {
//            ChatUtils.addChatMessage(message);
//        }
//    }
//
//    private int dispatchEntityActionCount = 0;
//
//    @EventTarget
//    public void badPacketUDetector(EventDispatchPacket e) {
//        if (e.getPacket() instanceof C03PacketPlayer) {
//            dispatchEntityActionCount = 0;
//        }
//
//        if (e.getPacket() instanceof C0BPacketEntityAction) {
//            C0BPacketEntityAction packet = (C0BPacketEntityAction) e.getPacket();
//            if (packet.getAction() == C0BPacketEntityAction.Action.START_SPRINTING || packet.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
//                if (dispatchEntityActionCount > 0) {
//                    log("You may just flagged BadPacketU!");
//                }
//                dispatchEntityActionCount++;
//            }
//        }
//    }
//
//    private int heldItemChange = 0, clickWindow = 0;
//
//    @EventTarget(Priority.LOW)
//    public void duplicatePacketDisabler(EventPacket e) {
//        if (e.getType() == EventType.SEND && !e.isCancelled() && mc.thePlayer != null) {
//            if (e.getPacket() instanceof C09PacketHeldItemChange) {
//                if (heldItemChange > 0) {
//                    // log("Bypassing Duplicate HeldItemChange Packet!");
//                    mc.getNetHandler().getNetworkManager().sendPacket(new C0FPacketConfirmTransaction(-1337, (short) 1337, true));
//                }
//
//                heldItemChange++;
//            } else {
//                heldItemChange = 0;
//            }
//
//            if (e.getPacket() instanceof C0EPacketClickWindow) {
//                C0EPacketClickWindow packet = (C0EPacketClickWindow) e.getPacket();
//
//                if (packet.getWindowId() != 0) {
//                    if (clickWindow > 0) {
//                        // log("Bypassing Duplicate ClickWindow Packet!");
//                        mc.getNetHandler().getNetworkManager().sendPacket(new C0FPacketConfirmTransaction(-1337, (short) 1337, true));
//                    }
//
//                    clickWindow++;
//                }
//            } else {
//                clickWindow = 0;
//            }
//        }
//    }
//
//    long lastPacketTime = 0;
//
//    @EventTarget(Priority.LOWEST)
//    public void blinkDetectionDisabler(EventDispatchPacket e) {
//        if (!e.isCancelled() && mc.getNetHandler() != null) {
//            if (System.currentTimeMillis() - lastPacketTime > 100) {
//                if (!(e.getPacket() instanceof C0FPacketConfirmTransaction) && !(e.getPacket() instanceof C03PacketPlayer)) {
//                    log("Disabling Blink!");
//                    e.getAdditionalPackets().add(new C0FPacketConfirmTransaction(-1337, (short) 1337, true));
//                }
//            }
//
//            lastPacketTime = System.currentTimeMillis();
//        }
//    }
//
//    public static int worldTick, playerTick;
//
//    @EventTarget
//    public void worldTickCounter(EventRunTicks e) {
//        if (mc.thePlayer != null) {
//            worldTick++;
//        }
//    }
//
//    @EventTarget
//    public void playerTickCounter(EventMotion e) {
//        if (e.getType() == EventType.POST) {
//            playerTick = worldTick - delayedServerPackets.size();
//        }
//    }
//
//    private float playerYaw;
//    private float deltaYaw;
//    private float lastPlacedDeltaYaw;
//    private boolean rotated = false;
//
//    @EventTarget(Priority.LOW)
//    public void duplicateRotPlaceDisabler(EventPacket e) {
//        if (e.getType() == EventType.SEND && !e.isCancelled() && mc.thePlayer != null) {
//            if (e.getPacket() instanceof C03PacketPlayer.C05PacketPlayerLook || e.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
//                C03PacketPlayer packet = (C03PacketPlayer) e.getPacket();
//                float lastPlayerYaw = playerYaw;
//                playerYaw = packet.getYaw();
//                deltaYaw = Math.abs(playerYaw - lastPlayerYaw);
//                rotated = true;
//
//                // Guess what will happen if you placed the block in current tick
//                if (deltaYaw > 2) {
//                    float xDiff = Math.abs(deltaYaw - lastPlacedDeltaYaw);
//                    if (xDiff < 0.0001) {
//                        log("Disabling DuplicateRotPlace!");
//                        packet.setYaw(packet.getYaw() + 0.0002F);
//                    }
//                }
//            } else if (e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
//                if (rotated) {
//                    lastPlacedDeltaYaw = deltaYaw;
//                    rotated = false;
//                }
//            }
//        }
//    }
//
//    public static final Map<Integer, Map<Entity, Double[]>> positions = new ConcurrentHashMap<>();
//
//    private static void putData(int tick, Entity entity, Double[] position) {
//        if (!positions.containsKey(tick)) {
//            positions.put(tick, new ConcurrentHashMap<>());
//        }
//
//        Map<Entity, Double[]> positionMap = positions.get(tick);
//        positionMap.put(entity, position);
//    }
//
//    private static Double[] getData(int tick, Entity entity) {
//        if (!positions.containsKey(tick)) {
//            return null;
//        }
//
//        Map<Entity, Double[]> positionMap = positions.get(tick);
//        return positionMap.get(entity);
//    }
//
//    public static Double[] getNextTickPosition(Entity entity) {
//        Double[] nextTickData = getData(playerTick + 1, entity);
//        if (nextTickData != null) {
//            return nextTickData;
//        }
//
//        Double[] currentTickData = getData(playerTick, entity);
//        if (currentTickData != null) {
//            return currentTickData;
//        }
//
//        return new Double[]{entity.posX, entity.posY, entity.posZ};
//    }
//
//    @EventTarget
//    public void onAllPacket(EventGlobalPacket e) {
//        Stuck stuck = (Stuck) BlinkFix.getInstance().getModuleManager().getModule(Stuck.class);
//
//        if (e.getType() == EventType.RECEIVE) {
//            if (e.getPacket() instanceof S18PacketEntityTeleport) {
//                S18PacketEntityTeleport packet = (S18PacketEntityTeleport) e.getPacket();
//                // set real position
//                Entity realEntity = mc.theWorld.getEntityByID(packet.getEntityId());
//
//                if (realEntity != null) {
//                    realEntity.serverPosX2 = packet.getX();
//                    realEntity.serverPosY2 = packet.getY();
//                    realEntity.serverPosZ2 = packet.getZ();
//
//                    double x = (double) realEntity.serverPosX2 / 32.0D;
//                    double y = (double) realEntity.serverPosY2 / 32.0D;
//                    double z = (double) realEntity.serverPosZ2 / 32.0D;
//
//                    putData(worldTick, realEntity, new Double[]{x, y, z});
//
//                    if (fakePlayers.containsKey(packet.getEntityId())) {
//                        EntityOtherPlayerMP fakePlayer = fakePlayers.get(packet.getEntityId());
//                        float yaw = (float) (packet.getYaw() * 360) / 256.0F;
//                        float pitch = (float) (packet.getPitch() * 360) / 256.0F;
//
//                        fakePlayer.setPositionAndRotation2(x, y, z, yaw, pitch, 3, true);
//                    }
//                }
//            } else if (e.getPacket() instanceof S14PacketEntity) {
//                S14PacketEntity packet = (S14PacketEntity) e.getPacket();
//                Entity entity = packet.getEntity(mc.theWorld);
//
//                if (entity != null) {
//                    entity.serverPosX2 += packet.func_149062_c();
//                    entity.serverPosY2 += packet.func_149061_d();
//                    entity.serverPosZ2 += packet.func_149064_e();
//
//                    double x = (double) entity.serverPosX2 / 32.0D;
//                    double y = (double) entity.serverPosY2 / 32.0D;
//                    double z = (double) entity.serverPosZ2 / 32.0D;
//
//                    putData(worldTick, entity, new Double[]{x, y, z});
//
//                    if (fakePlayers.containsKey(entity.getEntityId())) {
//                        EntityOtherPlayerMP fakePlayer = fakePlayers.get(entity.getEntityId());
//                        float yaw = packet.func_149060_h() ? (float) (packet.func_149066_f() * 360) / 256.0F : entity.rotationYaw;
//                        float pitch = packet.func_149060_h() ? (float) (packet.func_149063_g() * 360) / 256.0F : entity.rotationPitch;
//
//                        fakePlayer.setPositionAndRotation2(x, y, z, yaw, pitch, 3, false);
//                    }
//                }
//            } else if (e.getPacket() instanceof S0FPacketSpawnMob) {
//                S0FPacketSpawnMob packet = (S0FPacketSpawnMob) e.getPacket();
//                Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());
//
//                if (entity != null) {
//                    entity.serverPosX2 = packet.getX();
//                    entity.serverPosY2 = packet.getY();
//                    entity.serverPosZ2 = packet.getZ();
//
//                    double x = (double) entity.serverPosX2 / 32.0D;
//                    double y = (double) entity.serverPosY2 / 32.0D;
//                    double z = (double) entity.serverPosZ2 / 32.0D;
//
//                    putData(worldTick, entity, new Double[]{x, y, z});
//                }
//            } else if (e.getPacket() instanceof S0EPacketSpawnObject) {
//                S0EPacketSpawnObject packet = (S0EPacketSpawnObject) e.getPacket();
//
//                Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());
//
//                if (entity != null) {
//                    entity.serverPosX2 = packet.getX();
//                    entity.serverPosY2 = packet.getY();
//                    entity.serverPosZ2 = packet.getZ();
//
//                    double x = (double) entity.serverPosX2 / 32.0D;
//                    double y = (double) entity.serverPosY2 / 32.0D;
//                    double z = (double) entity.serverPosZ2 / 32.0D;
//
//                    putData(worldTick, entity, new Double[]{x, y, z});
//                }
//            } else if (e.getPacket() instanceof S13PacketDestroyEntities) {
//                S13PacketDestroyEntities packet = (S13PacketDestroyEntities) e.getPacket();
//                for (int id : packet.getEntityIDs()) {
//                    if (fakePlayers.containsKey(id)) {
//                        mc.theWorld.removeEntityFromWorld(fakePlayers.get(id).getEntityId());
//                        fakePlayers.remove(id);
//                    }
//                }
//            } else if (e.getPacket() instanceof S0BPacketAnimation) {
//                S0BPacketAnimation packet = (S0BPacketAnimation) e.getPacket();
//
//                if (fakePlayers.containsKey(packet.getEntityID())) {
//                    EntityOtherPlayerMP player = fakePlayers.get(packet.getEntityID());
//
//                    if (packet.getAnimationType() == 0) {
//                        player.swingItem();
//                    } else if (packet.getAnimationType() == 1) {
//                        player.performHurtAnimation();
//                    }
//                }
//            }
//        }
//
//        if (shouldDisable() && !e.isCancelled()) {
//            if (e.getPacket() instanceof C03PacketPlayer) {
//                disabled = true;
//
//                C03PacketPlayer packet = (C03PacketPlayer) e.getPacket();
//
//                if (!packet.setback) {
//                    releaseClientPackets();
//                }
//
//                if (!stuck.isEnabled()) {
//                    sendPacketWithEvent(e.getPacket());
//                }
//
//                if (!packet.setback) {
//                    releaseServerPackets();
//                }
//
//                e.setCancelled(true);
//            } else if (e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
//                C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) e.getPacket();
//                if (packet.getStack() != null && packet.getStack().getItem() instanceof ItemFood) {
//                    return;
//                }
//                delayedClientPackets.add(packet);
//                e.setCancelled(true);
//            } else if (e.getPacket() instanceof C07PacketPlayerDigging) {
//                C07PacketPlayerDigging packet = (C07PacketPlayerDigging) e.getPacket();
//                delayedClientPackets.add(packet);
//                e.setCancelled(true);
//            } else if (e.getType() == EventType.RECEIVE) {
//                if (e.getPacket() instanceof S0FPacketSpawnMob) {
//                    S0FPacketSpawnMob packet = (S0FPacketSpawnMob) e.getPacket();
//
//                    Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());
//
//                    if (entity instanceof EntityArrow || entity instanceof EntityThrowable) {
//                        return;
//                    }
//                } else if (e.getPacket() instanceof S14PacketEntity) {
//                    S14PacketEntity packet = (S14PacketEntity) e.getPacket();
//                    Entity entity = packet.getEntity(mc.theWorld);
//
//                    if (entity instanceof EntityThrowable || entity instanceof EntityArrow) {
//                        return;
//                    }
//                } else if (e.getPacket() instanceof S12PacketEntityVelocity) {
//                    S12PacketEntityVelocity packet = (S12PacketEntityVelocity) e.getPacket();
//                    Entity entity = mc.theWorld.getEntityByID(packet.getEntityID());
//                    if (entity instanceof EntityThrowable || entity instanceof EntityArrow) {
//                        return;
//                    }
//                } else if (e.getPacket() instanceof S08PacketPlayerPosLook) {
//                    if (stuck.isEnabled()) {
//                        stuck.toggle();
//                        releaseAllServerPackets();
//                    }
//                } else if (e.getPacket() instanceof S38PacketPlayerListItem) {
//                    S38PacketPlayerListItem packet = (S38PacketPlayerListItem) e.getPacket();
//                    if (packet.getAction() == S38PacketPlayerListItem.Action.UPDATE_GAME_MODE) {
//                        return;
//                    }
//                } else if (e.getPacket() instanceof S1CPacketEntityMetadata) {
//                    S1CPacketEntityMetadata packet = (S1CPacketEntityMetadata) e.getPacket();
//
//                    if (mc.theWorld.getEntityByID(packet.getEntityId()) != mc.thePlayer) {
//                        return;
//                    }
//                } else if (e.getPacket() instanceof S20PacketEntityProperties) {
//                    S20PacketEntityProperties packet = (S20PacketEntityProperties) e.getPacket();
//
//                    if (mc.theWorld.getEntityByID(packet.getEntityId()) != mc.thePlayer) {
//                        return;
//                    }
//                } else if (whitelistPackets.contains(e.getPacket().getClass())) {
//                    return;
//                }
//
//                addDelayServerPacket((Packet<INetHandlerPlayClient>) e.getPacket());
//                e.setCancelled(true);
//            }
//        } else if (disabled) {
//            disabled = false;
//            releaseAllServerPackets();
//        }
//    }
//
//    @EventTarget
//    public void onPlayerSpawn(EventSpawnPlayer e) {
//        EntityOtherPlayerMP player = e.getPlayer();
//
//        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.theWorld, player.getGameProfile());
//        fakePlayer.copyLocationAndAnglesFrom(player);
//        fakePlayer.setFakePlayer(true);
//        fakePlayers.put(player.getEntityId(), fakePlayer);
//
//        mc.theWorld.addEntityToWorld(-player.getEntityId() - 65535, fakePlayer);
//    }
//
//    private void sendPacketWithEvent(Packet<?> packet) {
//        EventPacket event = new EventPacket(EventType.SEND, packet, EventPacket.EventState.SEND);
//        BlinkFix.getInstance().getEventManager().call(event);
//
//        if (event.isCancelled()) {
//            return;
//        }
//
//        packet = event.getPacket();
//        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
//    }
//
//    private void addDelayServerPacket(Packet<INetHandlerPlayClient> packet) {
//        synchronized (delayedServerPackets) {
//            if (delayedServerPackets.isEmpty() || lastServerPackets == null) {
//                added = true;
//                delayedServerPackets.add(lastServerPackets = new ConcurrentLinkedQueue<>());
//            }
//
//            lastServerPackets.add(packet);
//        }
//    }
//
//    private void releaseAllServerPackets() {
//        while (!delayedServerPackets.isEmpty()) {
//            Queue<Packet<INetHandlerPlayClient>> queue = delayedServerPackets.poll();
//
//            synchronized (delayedServerPackets) {
//                if (queue == lastServerPackets) {
//                    lastServerPackets = null;
//                }
//            }
//
//            while (!queue.isEmpty()) {
//                releasingPackets.offer(queue.poll());
//            }
//        }
//
//        releasePackets();
//    }
//
//    private void releaseServerPackets() {
//        if (!delayedServerPackets.isEmpty()) {
//            Queue<Packet<INetHandlerPlayClient>> serverPackets = delayedServerPackets.poll();
//
//            synchronized (delayedServerPackets) {
//                if (serverPackets == lastServerPackets) {
//                    lastServerPackets = null;
//                }
//            }
//
//            while (!serverPackets.isEmpty()) {
//                releasingPackets.offer(serverPackets.poll());
//            }
//        }
//
//        releasePackets();
//    }
//
//    private void releasePackets() {
//        mc.addScheduledTask(() -> {
//            while (!releasingPackets.isEmpty()) {
//                Packet<INetHandlerPlayClient> packet = releasingPackets.poll();
//                releasePacket(packet);
//            }
//        });
//    }
//
//    private void releaseClientPackets() {
//        while (!delayedClientPackets.isEmpty()) {
//            sendPacketWithEvent(delayedClientPackets.poll());
//        }
//    }
//
//    private void releasePacket(Packet<INetHandlerPlayClient> packet) {
//        EventPacket event = new EventPacket(EventType.SEND, packet, EventPacket.EventState.SEND);
//        BlinkFix.getInstance().getEventManager().call(event);
//        packet = (Packet<INetHandlerPlayClient>) event.getPacket();
//
//        if (!event.isCancelled() && mc.getNetHandler() != null) {
//            try {
//                packet.processPacket(mc.getNetHandler());
//            } catch (Exception e) {
//                log.error("Error while releasing packet: " + packet.getClass().getSimpleName(), e);
//            }
//        }
//    }
//}
