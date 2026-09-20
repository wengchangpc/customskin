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
        modBus.addListener(this::onAddLayers);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterClientCommands);

        LOGGER.info("[CustomSkin] 已初始化，你的皮肤你做主！");
    }

    /** 把流光镀层挂到玩家渲染器上（Forge 47.x 全版本兼容写法）。 */
    private void onAddLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer pr) {
                pr.addLayer(new GlossLayer(pr));
            }
        }
        LOGGER.info("[CustomSkin] 流光镀层已挂载！");
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
                // 反馈必须在加载完成之后给出（load 在渲染线程执行）
                Minecraft.getInstance().execute(() -> {
                    SkinTextureManager.load();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            SkinTextureManager.isAvailable()
                                ? "[CustomSkin] 皮肤已重新加载！(model=" + (SkinTextureManager.isSlim() ? "slim" : "classic") + ")"
                                : "[CustomSkin] 未找到 skin.png，请把皮肤放到 config/CustomSkin/skin.png"),
                        false);
                });
                return 1;
            })))
            .then(Commands.literal("gloss").executes(ctx -> {
                SkinConfig.gloss = !SkinConfig.gloss;
                SkinConfig.save();
                ctx.getSource().sendSuccess(() -> Component.literal(
                        SkinConfig.gloss ? "[CustomSkin] 流光镀层已开启！" : "[CustomSkin] 流光镀层已关闭。"), false);
                return 1;
            }))
            .then(Commands.literal("vivid").executes(ctx -> {
                SkinConfig.vivid = !SkinConfig.vivid;
                SkinConfig.save();
                // 重新加载贴图使增艳开关立即生效，反馈在加载完成后给出
                Minecraft.getInstance().execute(() -> {
                    SkinTextureManager.load();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "[CustomSkin] 增艳" + (SkinConfig.vivid ? "已开启，" : "已关闭，") + "皮肤已重新加载！"), false);
                });
                return 1;
            })));
    }
}
