package xyz.whoneedspacee.ssmos.utilities;

import java.util.Collection;
import java.util.List;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import xyz.whoneedspacee.ssmos.Main;

public class SkinsUtil {

    private Player player;
    private Collection<PotionEffect> effects;
    private Location location;
    private int slot;
    private Property original_property = null;

    public SkinsUtil(Player player) {
        this.player = player;
    }

    public void changeSkin(String data, String signature) {
        ServerPlayer ePlayer = ((CraftPlayer) player).getHandle();
        GameProfile profile = ePlayer.getGameProfile();
        PropertyMap pMap = profile.getProperties();
        Property property = pMap.get("textures").iterator().next();
        if(original_property == null) {
            original_property = new Property(property.name(), property.value(), property.signature());
        }
        pMap.remove("textures", property);
        pMap.put("textures", new Property("textures", data, signature));
        updateSkin();
    }

    public void removeSkin() {
        if(original_property != null) {
            changeSkin(original_property.value(), original_property.signature());
        }
    }

    public void updateSkin() {
        effects = player.getActivePotionEffects();
        location = player.getLocation();
        slot = player.getInventory().getHeldItemSlot();

        CraftPlayer craftPlayer = ((CraftPlayer) player);
        ServerPlayer entityPlayer = craftPlayer.getHandle();
        entityPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(entityPlayer.getUUID())));
        entityPlayer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(entityPlayer)));

        player.teleport(location);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(Main.getInstance(), this.player);
            player.showPlayer(Main.getInstance(), this.player);
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {public void run() {
            player.getInventory().setHeldItemSlot(slot);
            player.addPotionEffects(effects);
            player.setExp(player.getExp());
            player.setHealth(player.getHealth()-0.0001);
            player.openInventory(player.getEnderChest());
            player.closeInventory();
        }}, 2);
    }

}
