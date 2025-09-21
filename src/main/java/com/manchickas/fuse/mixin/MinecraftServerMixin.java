package com.manchickas.fuse.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.manchickas.fuse.script.ScriptLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @WrapOperation(method = "method_29440", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;onDataPacksReloaded()V"))
    public void onResourceReload(PlayerManager instance, Operation<Void> original) {
        var server = (MinecraftServer) (Object) this;
        ScriptLoader.getInstance().updateCommandTree(server);
        original.call(instance);
    }
}
