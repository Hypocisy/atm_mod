package com.kumoe.atm.datagen;

import com.kumoe.atm.block.AtmBlock;
import com.kumoe.atm.block.AtmPart;
import com.kumoe.atm.registry.AtmRegistries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class AtmBlockStateProvider extends BlockStateProvider {

    public AtmBlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        super(output, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
//        this.simpleBlockWithItem(AtmRegistries.ATM_BLOCK.get(), getExistingFile("block/atm_block"));
        this.horizontalBlock(AtmRegistries.ATM_BLOCK.get(), blockState -> {
            AtmPart facing = blockState.getValue(AtmBlock.PART);
            return getExistingFile("block/atm_block_" + facing.getSerializedName());
        });
    }

    private ModelFile.ExistingModelFile getExistingFile(final String name) {
        return this.models().getExistingFile(this.modLoc(name));
    }
}
