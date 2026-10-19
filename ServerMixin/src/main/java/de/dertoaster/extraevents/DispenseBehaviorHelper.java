package de.dertoaster.extraevents;

import de.dertoaster.extraevents.api.event.DispenserDispenseEntityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;

public class DispenseBehaviorHelper {
  public static void onBootstrap() {

    // ARmorstand
    DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior() {
      public ItemStack execute(BlockSource source, ItemStack dispensed) {
        Direction direction = (Direction)source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(direction);
        ServerLevel serverLevel = source.level();
        ItemStack singleItemStack = dispensed.copyWithCount(1);
        Block block = CraftBlock.at(serverLevel, source.pos());
        org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitMirror(singleItemStack);
        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(0, 0, 0));
        serverLevel.getCraftServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
          return dispensed;
        } else {
          boolean shrink = true;
          if (!event.getItem().equals(craftItem)) {
            shrink = false;
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior dispenseBehavior = DispenserBlock.getDispenseBehavior(source, eventStack);
            if (dispenseBehavior != DispenseItemBehavior.NOOP && dispenseBehavior != this) {
              dispenseBehavior.dispense(source, eventStack);
              return dispensed;
            }
          }

          ItemStack newStack = CraftItemStack.unwrap(event.getItem());
          PostSpawnProcessor<ArmorStand> postSpawnConfig = EntityType.appendDefaultStackConfig((armorStandx) -> armorStandx.setYRot(direction.toYRot()), serverLevel, newStack, (LivingEntity)null);
          ArmorStand armorStand = (ArmorStand)EntityTypes.ARMOR_STAND.spawn(serverLevel, postSpawnConfig, pos, EntitySpawnReason.DISPENSER, false, false);
          if (armorStand != null && shrink) {
            dispensed.shrink(1);
          }

          DispenserDispenseEntityEvent ddee = new DispenserDispenseEntityEvent(block, armorStand.getBukkitEntity());
          ddee.callEvent();

          return dispensed;
        }
      }
    });

    // TODO: Boats
    // TODO: Minecarts

    // TNT
    DispenserBlock.registerBehavior(Blocks.TNT, new DefaultDispenseItemBehavior() {
      protected ItemStack execute(BlockSource blockSource, ItemStack item) {
        Level level = blockSource.level();
        BlockPos blockPos = blockSource.pos().relative((Direction)blockSource.state().getValue(DispenserBlock.FACING));
        ItemStack singleItemStack = item.copyWithCount(1);
        Block block = CraftBlock.at(level, blockSource.pos());
        org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitMirror(singleItemStack);
        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5));
        //if (!DispenserBlock.eventFired) {
          level.getCraftServer().getPluginManager().callEvent(event);
        //}

        if (event.isCancelled()) {
          return item;
        } else {
          boolean shrink = true;
          if (!event.getItem().equals(craftItem)) {
            shrink = false;
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior dispenseBehavior = DispenserBlock.getDispenseBehavior(blockSource, eventStack);
            if (dispenseBehavior != DispenseItemBehavior.NOOP && dispenseBehavior != this) {
              dispenseBehavior.dispense(blockSource, eventStack);
              return item;
            }
          }

          PrimedTnt primedTnt = new PrimedTnt(level, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), (LivingEntity)null);
          level.addFreshEntity(primedTnt);
          level.playSound((Player)null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
          level.gameEvent((Entity)null, GameEvent.ENTITY_PLACE, blockPos);
          if (shrink) {
            item.shrink(1);
          }
          DispenserDispenseEntityEvent ddee = new DispenserDispenseEntityEvent(block, primedTnt.getBukkitEntity());
          //if (!DispenserBlock.eventFired) {
            ddee.callEvent();
          //}

          return item;
        }
      }
    });

  }
}
