package carpet.patches;

import carpet.CarpetSettings;
import carpet.utils.extensions.ActionPackOwner;
import carpet.utils.extensions.CameraPlayer;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import java.util.UUID;

public class FakeServerPlayerEntity extends ServerPlayerEntity
{
    private static final ThreadLocal<Boolean> loginMinecartFix = new ThreadLocal<>();
    private double lastReportedPosX;
    private double lastReportedPosY;
    private double lastReportedPosZ;

    private double setX;
    private double setY;
    private double setZ;
    private float setYaw;
    private float setPitch;

    public static boolean shouldFixMinecart() {
        Boolean fix = loginMinecartFix.get();
        return fix != null && fix;
    }

    private static void setShouldFixMinecart(boolean fix) {
        loginMinecartFix.set(fix);
    }

    public static FakeServerPlayerEntity createFake(String username, MinecraftServer server, double x, double y, double z, double yaw, double pitch, int dimension, int gamemode)
    {
        ServerWorld worldIn = server.getWorld(dimension);
        ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getUserCache().findByName(username);
        if (gameprofile == null) {
            UUID uuid = PlayerEntity.getUuidFromProfile(new GameProfile((UUID)null, username));
            gameprofile = new GameProfile(uuid, username);
        }else {
            gameprofile = fixSkin(gameprofile);
        }
        FakeServerPlayerEntity instance = new FakeServerPlayerEntity(server, worldIn, gameprofile, interactionManagerIn);
        instance.setSetPosition(x, y, z, (float)yaw, (float)pitch);
        server.getPlayerManager().method_12827(new FakeClientConnection(NetworkSide.CLIENTBOUND), instance);
        if (instance.dimension != dimension) //player was logged in in a different dimension
        {
            ServerWorld old_world = server.getWorld(instance.dimension);
            instance.dimension = dimension;
            old_world.removeEntity(instance);
            instance.removed = false;
            worldIn.spawnEntity(instance);
            instance.setWorld(worldIn);
            server.getPlayerManager().method_1986(instance, old_world);
            instance.networkHandler.requestTeleport(x, y, z, (float)yaw, (float)pitch);
            instance.interactionManager.setWorld(worldIn);
        }
        instance.setHealth(20.0F);
        instance.removed = false;
        instance.stepHeight = 0.6F;
        interactionManagerIn.setGameMode(GameMode.setGameModeWithId(gamemode));
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance, (byte)(instance.headYaw * 256 / 360) ),instance.dimension);
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance),instance.dimension);
        server.getPlayerManager().method_2003(instance);
        instance.dataTracker.set(field_14796, (byte) 0x7f); // show all model layers (incl. capes)
        createAndAddFakePlayerToTeamBot(instance);
        return instance;
    }

    public static FakeServerPlayerEntity createShadow(MinecraftServer server, ServerPlayerEntity player)
    {
        if (CarpetSettings.cameraModeRestoreLocation && ((CameraPlayer) player).getGamemodeCamera()) {
            GameMode gametype = server.method_3026();
            ((CameraPlayer) player).moveToStoredCameraData();
            player.method_3170(gametype);
            player.removeStatusEffect(StatusEffect.get("night_vision"));
        }
        player.getMinecraftServer().getPlayerManager().method_12830(player);
        player.networkHandler.method_14977(new TranslatableText("multiplayer.disconnect.duplicate_login"));
        ServerWorld worldIn = server.getWorld(player.dimension);
        ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(worldIn);
        GameProfile gameprofile = player.getGameProfile();
        gameprofile = fixSkin(gameprofile);
        FakeServerPlayerEntity playerShadow = new FakeServerPlayerEntity(server, worldIn, gameprofile, interactionManagerIn);
        playerShadow.setSetPosition(player.x, player.y, player.z, player.yaw, player.pitch);
        server.getPlayerManager().method_12827(new FakeClientConnection(NetworkSide.CLIENTBOUND), playerShadow);

        playerShadow.setHealth(player.getHealth());
        playerShadow.networkHandler.requestTeleport(player.x, player.y,player.z, player.yaw, player.pitch);
        interactionManagerIn.setGameMode(player.interactionManager.getGameMode());
        ((ActionPackOwner) playerShadow).getActionPack().copyFrom(((ActionPackOwner) player).getActionPack());
        playerShadow.stepHeight = 0.6F;

        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(playerShadow, (byte)(player.headYaw * 256 / 360) ), playerShadow.dimension);
        server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, playerShadow));
        server.getPlayerManager().method_2003(playerShadow);
        createAndAddFakePlayerToTeamBot(playerShadow);
        return playerShadow;
    }

    public static FakeServerPlayerEntity create(String info, MinecraftServer server)
    {
        String[] infos = info.split("/");
        String username = infos[0];
        ServerWorld worldIn = server.getWorld(0);
        ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getUserCache().findByName(username);
        if (gameprofile == null) {
            UUID uuid = PlayerEntity.getUuidFromProfile(new GameProfile(null, username));
            gameprofile = new GameProfile(uuid, username);
        } else {
            gameprofile = fixSkin(gameprofile);
        }
        FakeServerPlayerEntity instance = new FakeServerPlayerEntity(server, worldIn, gameprofile, interactionManagerIn);
        server.getPlayerManager().loadPlayerData(instance);
        instance.setSetPosition(instance.x, instance.y, instance.z, instance.yaw, instance.pitch);
        setShouldFixMinecart(true);
        server.getPlayerManager().method_12827(new FakeClientConnection(NetworkSide.CLIENTBOUND), instance);
        setShouldFixMinecart(false);
        if (instance.dimension != 0) //player was logged in in a different dimension
        {
            worldIn = server.getWorld(instance.dimension);
            instance.setWorld(worldIn);
            server.getPlayerManager().method_1986(instance, worldIn);
            instance.interactionManager.setWorld(worldIn);
        }
        instance.removed = false;
        instance.stepHeight = 0.6F;
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance, (byte)(instance.headYaw * 256 / 360) ),instance.dimension);
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance),instance.dimension);
        server.getPlayerManager().method_2003(instance);
        instance.dataTracker.set(field_14796, (byte) 0x7f); // show all model layers (incl. capes)
        createAndAddFakePlayerToTeamBot(instance);
        if(infos.length > 1) ((ActionPackOwner) instance).getActionPack().fromString(infos[1]);
        return instance;
    }

    private FakeServerPlayerEntity(MinecraftServer server, ServerWorld worldIn, GameProfile profile, ServerPlayerInteractionManager interactionManagerIn)
    {
        super(server, worldIn, profile, interactionManagerIn);
    }

    private static GameProfile fixSkin(GameProfile gameProfile)
    {
        if (!CarpetSettings.removeFakePlayerSkins && !gameProfile.getProperties().containsKey("texture"))
            return SkullBlockEntity.loadProperties(gameProfile);
        else
            return gameProfile;
    }

    @Override
    public void kill()
    {
        logout();
    }

    @Override
    public void tick()
    {
        super.tick();
        this.tickPlayer();
        this.playerMoved();
    }

    @Override
    public void onKilled(DamageSource cause) {
        super.onKilled(cause);
        logout();
    }

    private void logout() {
        this.stopRiding();
        networkHandler.onDisconnected(new LiteralText("Logout"));
        removePlayerFromTeams(this);
    }

    public void despawn() {
        networkHandler.onDisconnected(new LiteralText("Despawn"));
        removePlayerFromTeams(this);
    }

    private void playerMoved()
    {
        if (x != lastReportedPosX || y != lastReportedPosY || z != lastReportedPosZ)
        {
            server.getPlayerManager().method_2003(this);
            lastReportedPosX = x;
            lastReportedPosY = y;
            lastReportedPosZ = z;
        }
    }

    public void setSetPosition(double x, double y, double z, float yaw, float pitch)
    {
        this.setX = x;
        this.setY = y;
        this.setZ = z;
        this.setYaw = yaw;
        this.setPitch = pitch;
    }

    public void resetToSetPosition()
    {
        refreshPositionAndAngles(setX, setY, setZ, setYaw, setPitch);
    }

    private static void createAndAddFakePlayerToTeamBot(FakeServerPlayerEntity player)
    {
        Scoreboard scoreboard = player.getMinecraftServer().getWorld(0).getScoreboard();
        if(!scoreboard.getTeamNames().contains("Bots")){//TODO: carpet rule for team name?
            scoreboard.addTeam("Bots");
            Team team = scoreboard.getTeam("Bots");
            Formatting textformatting = Formatting.byName("dark_green");
            team.setFormatting(textformatting);
            team.setPrefix(textformatting.toString());
            team.setSuffix(Formatting.RESET.toString());
        }
        scoreboard.addPlayerToTeam(player.getTranslationKey(), "Bots");
    }

    public static void removePlayerFromTeams(FakeServerPlayerEntity player){
        Scoreboard scoreboard = player.getMinecraftServer().getWorld(0).getScoreboard();
        scoreboard.clearPlayerTeam(player.getTranslationKey());
    }

    public static String getInfo(ServerPlayerEntity p){
        return p.getName() + "/" + ((ActionPackOwner) p).getActionPack();
    }

    @Override
    public void incrementStat(Stat stat, int amount) {
        if (CarpetSettings.fakePlayerStats) {
            super.incrementStat(stat, amount);
        }
    }
}
