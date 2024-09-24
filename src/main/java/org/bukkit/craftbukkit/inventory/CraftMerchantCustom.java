package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.util.CraftChatMessage;

public class CraftMerchantCustom implements CraftMerchant {

    private MinecraftMerchant merchant;

    public CraftMerchantCustom(String title) {
        this.merchant = new MinecraftMerchant(title);
        getMerchant().craftMerchant = this;
    }

    @Override
    public String toString() {
        return "CraftMerchantCustom";
    }

    @Override
    public MinecraftMerchant getMerchant() {
        return this.merchant;
    }

    public static class MinecraftMerchant implements net.minecraft.world.item.trading.Merchant {

        private final net.minecraft.network.chat.Component title;
        private final net.minecraft.world.item.trading.MerchantOffers trades = new net.minecraft.world.item.trading.MerchantOffers();
        private net.minecraft.world.entity.player.Player tradingPlayer;
        protected CraftMerchant craftMerchant;

        public MinecraftMerchant(String title) {
            Preconditions.checkArgument(title != null, "Title cannot be null");
            this.title = CraftChatMessage.fromString(title)[0];
        }

        @Override
        public CraftMerchant getCraftMerchant() {
            return craftMerchant;
        }

        @Override
        public void setTradingPlayer(net.minecraft.world.entity.player.Player entityhuman) {
            this.tradingPlayer = entityhuman;
        }

        @Override
        public net.minecraft.world.entity.player.Player getTradingPlayer() {
            return this.tradingPlayer;
        }

        @Override
        public net.minecraft.world.item.trading.MerchantOffers getOffers() {
            return this.trades;
        }

        @Override
        public void notifyTrade(net.minecraft.world.item.trading.MerchantOffer merchantrecipe) {
            // increase recipe's uses
            merchantrecipe.increaseUses();
        }

        @Override
        public void notifyTradeUpdated(net.minecraft.world.item.ItemStack itemstack) {
        }

        public net.minecraft.network.chat.Component getScoreboardDisplayName() {
            return title;
        }

        @Override
        public int getVillagerXp() {
            return 0; // xp
        }

        @Override
        public void overrideXp(int i) {
        }

        @Override
        public boolean showProgressBar() {
            return false; // is-regular-villager flag (hides some gui elements: xp bar, name suffix)
        }

        @Override
        public net.minecraft.sounds.SoundEvent getNotifyTradeSound() {
            return net.minecraft.sounds.SoundEvents.VILLAGER_YES;
        }

        @Override
        public void overrideOffers(net.minecraft.world.item.trading.MerchantOffers merchantrecipelist) {
        }

        @Override
        public boolean isClientSide() {
            return false;
        }
    }
}
