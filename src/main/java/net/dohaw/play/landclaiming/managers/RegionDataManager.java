package net.dohaw.play.landclaiming.managers;

import net.dohaw.play.landclaiming.LandClaiming;
import net.dohaw.play.landclaiming.handlers.RegionDataHandler;
import net.dohaw.play.landclaiming.region.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class RegionDataManager {

    private LandClaiming plugin;
    private RegionDataHandler regionDataHandler;
    private List<RegionData> regionDataList = new ArrayList<>();

    public RegionDataManager(LandClaiming plugin) {
        this.plugin = plugin;
        this.regionDataHandler = new RegionDataHandler(plugin);
    }

    public void loadData() {

        List<RegionData> regionData = new ArrayList<>();
        List<SingleRegionData> singleRegionData = regionDataHandler.load();
        Iterator<SingleRegionData> itr = singleRegionData.iterator();

        while (itr.hasNext()) {
            SingleRegionData srd = itr.next();
            RegionData newData = formulateData(srd, singleRegionData);
            if (newData instanceof ConnectedRegionData) {
                newData = formulateConnections((ConnectedRegionData) newData, singleRegionData);
            }
            regionData.add(newData);
        }

        Iterator<RegionData> itr3 = regionData.iterator();
        while (itr3.hasNext()) {
            RegionData rd = itr3.next();
            if (rd instanceof SingleRegionData) {
                if (((SingleRegionData) rd).isConnected()) {
                    itr3.remove();
                }
            }
        }

        this.regionDataList = regionData;
    }

    private ConnectedRegionData formulateConnections(ConnectedRegionData newData, List<SingleRegionData> loadedList){
        List<ConnectedRegionData> followingConnections = new ArrayList<>();
        for(SingleRegionData s : newData.getConnectedData()){
            RegionData rd = formulateData(s, loadedList);
            if(rd instanceof ConnectedRegionData){
                followingConnections.add(formulateConnections((ConnectedRegionData) rd, loadedList));
            }
        }
        if(followingConnections.size() > 1){
            return ConnectedRegionData.join(followingConnections);
        }else{
            return newData;
        }
    }

    private RegionData formulateData(SingleRegionData current, List<SingleRegionData> loadedList){

        List<SingleRegionData> currentCollection = new ArrayList<>();
        Chunk currentSrdChunk = current.getChunk();

        Chunk chunkRight = getRightChunk(currentSrdChunk);
        if(isInConnection(current, chunkRight, loadedList)){
            SingleRegionData sd = getDataFromChunk(chunkRight, loadedList);
            sd.setIsConnected(true);
            currentCollection.add(sd);
        }

        Chunk chunkLeft = getLeftChunk(currentSrdChunk);
        if(isInConnection(current, chunkLeft, loadedList)){
            SingleRegionData sd = getDataFromChunk(chunkLeft, loadedList);
            sd.setIsConnected(true);
            currentCollection.add(sd);
        }

        Chunk chunkForward = getForwardChunk(currentSrdChunk);
        if(isInConnection(current, chunkForward, loadedList)){
            SingleRegionData sd = getDataFromChunk(chunkForward, loadedList);
            sd.setIsConnected(true);
            currentCollection.add(sd);
        }

        Chunk chunkBackward = getBackwardsChunk(currentSrdChunk);
        if(isInConnection(current, chunkBackward, loadedList)){
            SingleRegionData sd = getDataFromChunk(chunkBackward, loadedList);
            sd.setIsConnected(true);
            currentCollection.add(sd);
        }

        if(currentCollection.size() != 0){
            currentCollection.add(current);
            return new ConnectedRegionData(current.getOwnerUUID(), currentCollection, current.getType(), current.getDescription());
        }

        return current;

    }

    /*
        srdlist is the loaded list of single region data objects
        potdatachunk is the chunk im checking
        srd1 is the current chunk that i know has data and want to see if there are any connecting chunks wih data.
     */
    private boolean isInConnection(SingleRegionData srd1, Chunk potDataChunk, List<SingleRegionData> srdList){
        if(hasData(potDataChunk, srdList)){
            SingleRegionData data = getDataFromChunk(potDataChunk, srdList);
            if(data != null){
                if(data instanceof SingleRegionData){
                    if(srd1.getOwnerUUID().equals(data.getOwnerUUID())){
                        if(srd1.getDescription() == data.getDescription()){
                            if(!data.isConnected()){
                                return srd1.getType() == data.getType();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private Chunk getRightChunk(Chunk chunk){
        int currentX = chunk.getX();
        int currentZ = chunk.getZ();
        return chunk.getWorld().getChunkAt((currentX + 1), currentZ);
    }

    private Chunk getLeftChunk(Chunk chunk){
        int currentX = chunk.getX();
        int currentZ = chunk.getZ();
        return chunk.getWorld().getChunkAt((currentX - 1), currentZ);
    }

    private Chunk getForwardChunk(Chunk chunk){
        int currentX = chunk.getX();
        int currentZ = chunk.getZ();
        return chunk.getWorld().getChunkAt(currentX, (currentZ + 1));
    }

    private Chunk getBackwardsChunk(Chunk chunk){
        int currentX = chunk.getX();
        int currentZ = chunk.getZ();
        return chunk.getWorld().getChunkAt(currentX, (currentZ - 1));
    }

    public void saveData(){
        for(RegionData rd : regionDataList){
            if(rd instanceof ConnectedRegionData){
                ConnectedRegionData connectedRegionData = (ConnectedRegionData) rd;
                List<SingleRegionData> rdList = connectedRegionData.getConnectedData();
                for(SingleRegionData srd : rdList){
                    regionDataHandler.save(srd);
                }
            }else{
                regionDataHandler.save((SingleRegionData) rd);
            }
        }
    }

    public void setRegionData(SingleRegionData singleRegionData){
        Chunk chunk = singleRegionData.getChunk();
        int index = regionDataList.indexOf(getDataFromChunk(chunk));
        regionDataList.set(index, singleRegionData);
    }

    public List<RegionData> getRegionData(){
        return regionDataList;
    }

    public SingleRegionData getDataFromChunk(Chunk chunk, List<SingleRegionData> srdList){
        for(SingleRegionData srd : srdList){
            if(srd.getChunk().equals(chunk)){
                return srd;
            }
        }
        return null;
    }

    public boolean hasData(Chunk chunk){
        for(RegionData rd : regionDataList){
            if(rd instanceof ConnectedRegionData){
                ConnectedRegionData crd = (ConnectedRegionData)rd;
                if(crd.getChunks().contains(chunk)){
                    return true;
                }
            }else{
                SingleRegionData srd = (SingleRegionData) rd;
                if(srd.getChunk().equals(chunk)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasData(Chunk chunk, List<SingleRegionData> dataList){
        for(RegionData rd : dataList){
            if(rd instanceof ConnectedRegionData){
                ConnectedRegionData crd = (ConnectedRegionData)rd;
                if(crd.getChunks().contains(chunk)){
                    return true;
                }
            }else{
                SingleRegionData srd = (SingleRegionData) rd;
                if(srd.getChunk().equals(chunk)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasData(Location location){
        return hasData(location.getChunk());
    }

    public SingleRegionData create(UUID owner, Chunk chunk, RegionDescription desc, RegionType type){
        SingleRegionData singleRegionData = regionDataHandler.create(owner, chunk, desc, type);
        regionDataList.add(singleRegionData);
        return singleRegionData;
    }
    
    public void delete(SingleRegionData data){
        regionDataList.remove(data);
        regionDataHandler.delete(data);
    }
    
    public SingleRegionData getDataFromLocation(Location loc){
        return getDataFromChunk(loc.getChunk());
    }

    public SingleRegionData getDataFromChunk(Chunk chunk){
        for(RegionData rd : regionDataList){
            if(rd instanceof ConnectedRegionData){
                ConnectedRegionData crd = (ConnectedRegionData)rd;
                List<SingleRegionData> srdList = crd.getConnectedData();
                for(SingleRegionData srd : srdList){
                    if(srd.getChunk().equals(chunk)){
                        return srd;
                    }
                }
            }else{
                SingleRegionData srd = (SingleRegionData) rd;
                if(srd.getChunk().equals(chunk)){
                    return srd;
                }
            }
        }
        return null;
    }

    public SingleRegionData getDataFromName(String name){
        for(RegionData rd : regionDataList){
            if(rd instanceof ConnectedRegionData){
                ConnectedRegionData crd = (ConnectedRegionData)rd;
                List<SingleRegionData> srdList = crd.getConnectedData();
                for(SingleRegionData srd : srdList){
                    if(srd.getName().equalsIgnoreCase(name)){
                        return srd;
                    }
                }
            }else{
                SingleRegionData srd = (SingleRegionData) rd;
                if(srd.getName().equalsIgnoreCase(name)){
                    return srd;
                }
            }
        }
        return null;
    }

    public List<RegionData> getPlayerRegionData(UUID uuid){
        List<RegionData> playerRegionData = new ArrayList<>();
        for(RegionData rd : regionDataList){
            Bukkit.broadcastMessage(rd.toString());
            if(rd.getOwnerUUID().equals(uuid)){
                playerRegionData.add(rd);
            }
        }
        return playerRegionData;
    }

    public List<RegionData> getPlayerRegionDataByDescription(UUID uuid, RegionDescription desc, RegionType type){
        List<RegionData> playerSingleRegionData = new ArrayList<>();
        if(!getPlayerRegionData(uuid).isEmpty()){
            for(RegionData rd : getPlayerRegionData(uuid)){
                if(rd.getDescription() == desc && rd.getType() == type){
                    playerSingleRegionData.add(rd);
                }
            }
        }
        return playerSingleRegionData;
    }

    public List<RegionData> getPlayerRegionDataByDescription(UUID uuid, RegionDescription desc){
        List<RegionData> playerSingleRegionData = new ArrayList<>();
        if(!getPlayerRegionData(uuid).isEmpty()){
            for(RegionData rd : getPlayerRegionData(uuid)){
                if(rd.getDescription() == desc){
                    playerSingleRegionData.add(rd);
                }
            }
        }
        return playerSingleRegionData;
    }

}
