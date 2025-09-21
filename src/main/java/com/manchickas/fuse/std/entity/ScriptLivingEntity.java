package com.manchickas.fuse.std.entity;

import net.minecraft.entity.LivingEntity;

public class ScriptLivingEntity<E extends LivingEntity> extends ScriptEntity<E> {

    protected ScriptLivingEntity(E wrapped) {
        super(wrapped);
    }
}
