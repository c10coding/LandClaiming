package net.dohaw.play.landclaiming.runnables;

import me.c10coding.coreapi.chat.ChatFactory;
import me.c10coding.coreapi.helpers.MathHelper;
import net.dohaw.play.landclaiming.LandClaiming;
import net.dohaw.play.landclaiming.PlayerData;
import net.dohaw.play.landclaiming.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

public class ClaimGiver extends BukkitRunnable {

    private PlayerDataManager playerDataManager;
    private int defaultGiveAmount;
    private ChatFactory chatFactory;
    private MathHelper mathHelper;

    private final String PERMISSION_PREFIX = "land.earntime.";
    private final String PREFIX;

    public ClaimGiver(LandClaiming plugin){
        this.playerDataManager = plugin.getPlayerDataManager();
        this.defaultGiveAmount = plugin.getBaseConfig().getDefaultGiveAmount();
        this.chatFactory = plugin.getAPI().getChatFactory();
        this.PREFIX = plugin.getBaseConfig().getPluginPrefix();
        this.mathHelper = plugin.getAPI().getMathHelper();
    }

    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()){
            PlayerData data = playerDataManager.getData(player.getUniqueId());
            int giveAmount = getGiveAmount(player);
            int currentPlayerClaimAmount = data.getClaimAmount();
            data.setClaimAmount(currentPlayerClaimAmount + giveAmount);
            playerDataManager.setData(player.getUniqueId(), data);
            chatFactory.sendPlayerMessage("You have been given &e" + giveAmount + "&f claims. You now have &e" + (giveAmount + currentPlayerClaimAmount) + " &fclaims!", true, player, PREFIX);
        }
    }

    public int getGiveAmount(Player player){
        for(PermissionAttachmentInfo info : player.getEffectivePermissions()){
            if(info.getPermission().startsWith(PERMISSION_PREFIX)){
                String permission = info.getPermission();
                if(mathHelper.isInt(permission.substring(permission.lastIndexOf(".") + 1))){
                    return Integer.parseInt(permission.substring(permission.lastIndexOf(".") + 1));
                }
            }
        }
        return defaultGiveAmount;
    }

}
