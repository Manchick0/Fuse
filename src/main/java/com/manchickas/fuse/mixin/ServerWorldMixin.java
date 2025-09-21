package com.manchickas.fuse.mixin;

import com.manchickas.fuse.event.type.JoinEvent;
import com.manchickas.fuse.script.ScriptLoader;
import com.manchickas.fuse.std.entity.ScriptPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "onPlayerConnected", at = @At("TAIL"))
    public void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        var event = new JoinEvent(ScriptPlayer.wrap(player));
        ScriptLoader.getInstance().dispatch(event);
    }
}
