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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.function.Consumer;

public class DispenseBehaviorHelper {
  public static void onBootstrap() {

    DefaultDispenseItemBehavior spawnEggDispenseItemBehavior = new DefaultDispenseItemBehavior() {
      public ItemStack execute(BlockSource blockSource, ItemStack item) {
        Direction direction = (Direction)blockSource.state().getValue(DispenserBlock.FACING);
        EntityType<?> type = ((SpawnEggItem)item.getItem()).getType(blockSource.level().registryAccess(), item);
        ServerLevel serverLevel = blockSource.level();
        ItemStack singleItemStack = item.copyWithCount(1);
        Block block = CraftBlock.at(serverLevel, blockSource.pos());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(singleItemStack);
        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(0, 0, 0));
        //if (!DispenserBlock.eventFired) {
          serverLevel.getCraftServer().getPluginManager().callEvent(event);
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

            singleItemStack = CraftItemStack.unwrap(event.getItem());
            type = ((SpawnEggItem)singleItemStack.getItem()).getType(serverLevel.registryAccess(), singleItemStack);
          }

          try {
            Entity entity = type.spawn(blockSource.level(), singleItemStack, (Player)null, blockSource.pos().relative(direction), EntitySpawnReason.DISPENSER, direction != Direction.UP, false);
            DispenserDispenseEntityEvent ddee = new DispenserDispenseEntityEvent(block, entity.getBukkitEntity());
            //if (!DispenserBlock.eventFired) {
              ddee.callEvent();
            //}
          } catch (Exception var13) {
            Exception var6 = var13;
            LOGGER.error("Error while dispensing spawn egg from dispenser at {}", blockSource.pos(), var6);
            return ItemStack.EMPTY;
          }

          if (shrink) {
            item.shrink(1);
          }

          blockSource.level().gameEvent((Entity)null, GameEvent.ENTITY_PLACE, blockSource.pos());
          return item;
        }
      }
    };
    Iterator var1 = SpawnEggItem.eggs().iterator();

    while(var1.hasNext()) {
      SpawnEggItem spawnEggItem = (SpawnEggItem)var1.next();
      DispenserBlock.registerBehavior(spawnEggItem, spawnEggDispenseItemBehavior);
    }

    // ARmorstand
    DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior() {
      public ItemStack execute(BlockSource blockSource, ItemStack item) {
        Direction direction = (Direction)blockSource.state().getValue(DispenserBlock.FACING);
        BlockPos blockPos = blockSource.pos().relative(direction);
        ServerLevel serverLevel = blockSource.level();
        ItemStack singleItemStack = item.copyWithCount(1);
        Block block = CraftBlock.at(serverLevel, blockSource.pos());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(singleItemStack);
        BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(0, 0, 0));
        //if (!DispenserBlock.eventFired) {
          serverLevel.getCraftServer().getPluginManager().callEvent(event);
        //}

        if (event.isCancelled()) {
          return item;
        } else {
          boolean shrink = true;
          ItemStack eventStack;
          if (!event.getItem().equals(craftItem)) {
            shrink = false;
            eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior dispenseBehavior = DispenserBlock.getDispenseBehavior(blockSource, eventStack);
            if (dispenseBehavior != DispenseItemBehavior.NOOP && dispenseBehavior != this) {
              dispenseBehavior.dispense(blockSource, eventStack);
              return item;
            }
          }

          eventStack = CraftItemStack.unwrap(event.getItem());
          Consumer<ArmorStand> consumer = EntityType.appendDefaultStackConfig((armorStand1) -> {
            armorStand1.setYRot(direction.toYRot());
          }, serverLevel, eventStack, (Player)null);
          ArmorStand armorStand = (ArmorStand)EntityType.ARMOR_STAND.spawn(serverLevel, consumer, blockPos, EntitySpawnReason.DISPENSER, false, false);
          if (armorStand != null && shrink) {
            item.shrink(1);
          }
          DispenserDispenseEntityEvent ddee = new DispenserDispenseEntityEvent(block, armorStand.getBukkitEntity());
          //if (!DispenserBlock.eventFired) {
            ddee.callEvent();
          //}

          return item;
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
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(singleItemStack);
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
