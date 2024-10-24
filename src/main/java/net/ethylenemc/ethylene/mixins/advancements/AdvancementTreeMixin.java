package net.ethylenemc.ethylene.mixins.advancements;

import net.minecraft.advancements.AdvancementTree;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdvancementTree.class)
public class AdvancementTreeMixin {
    @Redirect(method = "addAll", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void ethylene$addAll(Logger instance, String s, Object o) {
        // CraftBukkit - moved to AdvancementDataWorld#reload
    }
}
