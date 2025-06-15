package com.kumoe.atm.block;

import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.network.packet.PlayerBalancePacket;
import com.kumoe.atm.uitls.PluginUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class AtmBlock extends BaseEntityBlock {
    public static final EnumProperty<AtmPart> PART = EnumProperty.create("part", AtmPart.class);
    private static final Property<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public AtmBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, AtmPart.LOWER));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            if (pLevel.getBlockEntity(pPos) instanceof AtmBlockEntity atmBlockEntity) {
                NetworkHooks.openScreen(serverPlayer, pState.getMenuProvider(pLevel, pPos), buf -> {
                    buf.writeBlockPos(pPos);
                    buf.writeUUID(atmBlockEntity.getOwnerUuid());
                });
                if (PluginUtils.checkBukkitInstalled()) {
                    double balance;
                    balance = PluginUtils.getBalance(pPlayer);
                    NetworkHandler.sendToPlayer(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), new PlayerBalancePacket(pPlayer.getUUID(), balance));
                }
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return blockState.getValue(PART) == AtmPart.UPPER ? new AtmBlockEntity(blockPos, blockState) : null;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        pLevel.setBlock(pPos.above(), pState.setValue(PART, AtmPart.UPPER), 3);
        if (!pLevel.isClientSide() && pLevel.getBlockEntity(pPos.above()) instanceof AtmBlockEntity atmBlockEntity) {
            if (pPlacer != null) {
                atmBlockEntity.setOwnerUuid(pPlacer.getUUID());
                atmBlockEntity.setChanged();
            }
        }
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider(
                (id, inv, player) -> new AtmMenu(id, inv, 3, (Container) pLevel.getBlockEntity(pPos)), // 传递必要的数据
                Component.translatable("menu.atm_mod.title") // 菜单标题
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, PART);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        return pState.getValue(PART) == AtmPart.LOWER || blockstate.is(this);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        AtmPart part = pState.getValue(PART);
        if (pDirection.getAxis() == Direction.Axis.Y && part == AtmPart.LOWER == (pDirection == Direction.UP)) {
            return pNeighborState.is(this) && pNeighborState.getValue(PART) != part ? pState.setValue(FACING, pNeighborState.getValue(FACING)) : Blocks.AIR.defaultBlockState();
        } else {
            return part == AtmPart.LOWER && pDirection == Direction.DOWN && !pState.canSurvive(pLevel, pPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock()) && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof AtmBlockEntity container) {
                // 遍历容器中的每个槽位
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack itemStack = container.getItem(i);
                    if (!itemStack.isEmpty()) {
                        // 在方块位置生成掉落物
                        double x = pPos.getX() + 0.5;
                        double y = pPos.getY() + 0.5;
                        double z = pPos.getZ() + 0.5;

                        ItemEntity itemEntity = new ItemEntity(
                                pLevel, x, y, z,
                                itemStack.copy()
                        );

                        pLevel.addFreshEntity(itemEntity);
                    }
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }
}
