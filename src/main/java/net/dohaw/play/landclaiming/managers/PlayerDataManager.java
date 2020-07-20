package net.dohaw.play.landclaiming.managers;

import net.dohaw.play.landclaiming.LandClaiming;
import net.dohaw.play.landclaiming.PlayerData;
import net.dohaw.play.landclaiming.datahandlers.PlayerDataHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private LandClaiming plugin;
    private PlayerDataHandler playerDataHandler;
    private HashMap<UUID, PlayerData> playerData = new HashMap<>();

    public PlayerDataManager(LandClaiming plugin){
        this.plugin = plugin;
        this.playerDataHandler = new PlayerDataHandler(plugin);
    }

    public PlayerData getPlayerData(UUID uuid){
        return playerData.get(uuid);
    }

    public void setPlayerData(UUID uuid, PlayerData data){
        playerData.replace(uuid, data);
    }

    public void removePlayerData(UUID uuid){
        playerData.remove(uuid);
    }

    /*
        Run this if the player has had player data previously.
     */
    public void addPlayerData(UUID uuid){

    }

    /*
        Run this if the player has never had player data
     */
    public void createPlayerData(){

    }

    public void shutDown(){
        Iterator<Map.Entry<UUID, PlayerData>> itr = playerData.entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry<UUID, PlayerData> entry = itr.next();
            playerDataHandler.save(entry.getValue());
        }
    }

    private void createPlayerFiles(){

    }

}
