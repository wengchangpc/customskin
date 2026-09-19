package com.example.customskin;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomSkinMod.MODID)
public class CustomSkinMod {
    public static final String MODID = "customskin";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public CustomSkinMod() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            // 纯客户端模组：在专用服务器上什么都不做
            return;
        }

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterClientCommands);

        LOGGER.info("[CustomSkin] 已初始化，你的皮肤你做主！");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // GL 纹理操作必须回到渲染线程执行
        Minecraft.getInstance().execute(() -> {
            SkinTextureManager.copyDefaultIfMissing();
            SkinTextureManager.load();
        });
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("customskin")
            .then(Commands.literal("reload").executes(ctx -> {
                Minecraft.getInstance().execute(SkinTextureManager::load);
                ctx.getSource().sendSuccess(() -> Component.literal(
                        SkinTextureManager.isAvailable()
                            ? "[CustomSkin] 皮肤已重新加载！(model=" + (SkinTextureManager.isSlim() ? "slim" : "classic") + ")"
                            : "[CustomSkin] 未找到 skin.png，请把皮肤放到 .minecraft/config/CustomSkin/skin.png"),
                    false);
                return 1;
            })));
    }
}
