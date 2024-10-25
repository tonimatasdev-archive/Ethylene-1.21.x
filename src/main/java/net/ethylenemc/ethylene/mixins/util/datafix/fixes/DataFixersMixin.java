package net.ethylenemc.ethylene.mixins.util.datafix.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.EntityCustomNameToComponentFix;
import net.minecraft.util.datafix.fixes.References;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataFixers.class)
public class DataFixersMixin {
    @Inject(method = "addFixers", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/DataFixerBuilder;addFixer(Lcom/mojang/datafixers/DataFix;)V", ordinal = 58))
    private static void ethylene$addFixers(DataFixerBuilder dataFixerBuilder, CallbackInfo ci, @Local(ordinal = 45) Schema schema46) {
        dataFixerBuilder.addFixer(new com.mojang.datafixers.DataFix(schema46, false) {
            @Override
            protected com.mojang.datafixers.TypeRewriteRule makeRule() {
                return this.fixTypeEverywhereTyped("Player CustomName", this.getInputSchema().getType(References.PLAYER), (typed) -> typed.update(DSL.remainderFinder(), EntityCustomNameToComponentFix::fixTagCustomName));
            }
        });
    }
}
