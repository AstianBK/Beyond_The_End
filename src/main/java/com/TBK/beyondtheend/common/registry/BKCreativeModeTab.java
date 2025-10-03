package com.TBK.beyondtheend.common.registry;

import com.google.common.collect.Ordering;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BKCreativeModeTab {
    static Comparator<ItemStack> stackComparator;
    public static final CreativeModeTab BK_TAB = new CreativeModeTab("bk_tab") {

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(BkCommonRegistry.GLOWING_ENERGY.get());
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> pItems) {
            super.fillItemList(pItems);
            PreOrdenInit();
            pItems.sort(stackComparator);
        }
    };

    public static void PreOrdenInit(){
        List<Item> itemList= Arrays.asList(
                BkCommonRegistry.GLOWING_ENERGY.get().asItem(),
                BkCommonRegistry.GLOWING_ENERGY_STREAK.get().asItem(),
                BkCommonRegistry.COBBLED_DIABASE.get().asItem(),
                BkCommonRegistry.COBBLED_TEPHRITE.get().asItem(),BkCommonRegistry.CRATERED_DIABASE.get().asItem(),
                BkCommonRegistry.SMOOTH_DIABASE.get().asItem(),
                BkCommonRegistry.SMOOTH_TEPHRITE.get().asItem(),
                BkCommonRegistry.PORTAL.get().asItem(),
                BkCommonRegistry.FALLEN_DRAGON_SPAWN_EGG.get()
        );

        stackComparator= Ordering.explicit(itemList).onResultOf(ItemStack::getItem);
    }


}
