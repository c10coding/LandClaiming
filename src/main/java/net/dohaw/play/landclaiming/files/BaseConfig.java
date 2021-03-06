package net.dohaw.play.landclaiming.files;

import me.c10coding.coreapi.files.Config;
import org.bukkit.plugin.java.JavaPlugin;

public class BaseConfig extends Config {

    public BaseConfig(JavaPlugin plugin) {
        super(plugin, "config.yml");
    }

    public String getPluginPrefix(){
        return config.getString("Prefix");
    }

    public int getDefaultChunkAmount(){
        return config.getInt("Default Chunk Amount");
    }

    public int getDefaultGiveAmount(){
        return config.getInt("Default Claim Give Amount");
    }

    public int getClaimBlockPlaceAmount(){
        return config.getInt("Claim Block Place Amount");
    }

    public int getPricePerClaim(){
        return config.getInt("Price Per Claim");
    }

}
