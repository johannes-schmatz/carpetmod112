package narcolepticfrog.rsmm.events;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface PlayerConnectionListener {

    public void onPlayerConnect(ServerPlayerEntity player);

    public void onPlayerDisconnect(ServerPlayerEntity player);

}
