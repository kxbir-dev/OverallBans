package OverallBans.gui;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.ContainerAnvil;

public class CustomAnvilContainer extends ContainerAnvil {

    public CustomAnvilContainer(PlayerInventory playerInv, World world, BlockPosition pos, EntityHuman entity) {
        super(playerInv, world, pos, entity);
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return true;
    }
}
