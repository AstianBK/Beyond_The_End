package com.TBK.beyond_the_end.client;

import com.TBK.beyond_the_end.CommonProxy;
import com.ibm.icu.math.BigDecimal;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.antlr.v4.runtime.atn.ATNConfigSet;

public class ClientProxy extends CommonProxy {

    public void init(){
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }
}
