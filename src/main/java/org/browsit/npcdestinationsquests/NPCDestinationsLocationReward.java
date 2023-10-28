package org.browsit.npcdestinationsquests;

import me.pikamug.quests.module.BukkitCustomReward;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Navigation_NewDestination;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class NPCDestinationsLocationReward extends BukkitCustomReward {

    public NPCDestinationsLocationReward() {
        setName("NPC Destinations Location Reward");
        setAuthor("Browsit, LLC");
        setItem("GOLD_INGOT", (short)0);
        addStringPrompt("NPC ID", "Citizens NPC ID", "ANY");
        addStringPrompt("Location ID", "Location to move NPC to", "ANY");
        addStringPrompt("Duration", "Seconds to stay at the location", "15");
    }

    @Override
    public String getModuleName() {
        return NPCDestinationsModule.getModuleName();
    }

    @Override
    public Map.Entry<String, Short> getModuleItem() {
        return NPCDestinationsModule.getModuleItem();
    }

    @Override
    public void giveReward(UUID uuid, Map<String, Object> data) {
        if (data != null && data.containsKey("NPC ID") && data.containsKey("Location ID")
                && data.containsKey("Duration")) {

            UUID destUUID = null;
            int destID = -1;
            int targetNPC = -1;
            int duration = 15;

            String npcIDString = (String) data.get("NPC ID");
            String locationString = (String)data.get("Location ID");
            String durationString = (String)data.get("Duration");

            if (NumberUtils.isNumber(npcIDString)) {
                targetNPC = Integer.parseInt(npcIDString);
            }
            if (NumberUtils.isNumber(durationString)) {
                duration = Integer.parseInt(durationString);
            }

            if (NumberUtils.isNumber(locationString)) {
                destID = Integer.parseInt(locationString);
            } else {
                destUUID = UUID.fromString(locationString);
            }

            final NPC npc = CitizensAPI.getNPCRegistry().getById(targetNPC);
            if (npc == null) {
                DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                        "destinations", "Console_Messages.quests_error", "Location Reward has invalid NPC ID "
                                + targetNPC);
                return;
            }

            NPCDestinationsTrait trait;
            if (!npc.hasTrait(NPCDestinationsTrait.class)) {
                DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                        "destinations", "Console_Messages.quests_error", "Location Reward has NPC ("
                                + targetNPC + "), but lacks the NPCDestination trait.");
                return;
            } else {
                trait = npc.getTrait(NPCDestinationsTrait.class);
            }

            boolean containsLocation = false;
            if (destID > -1 && destID <= trait.NPCLocations.size()) {
                containsLocation = true;
            } else if (destID == -1 && destUUID != null) {
                for (int i = 0; i < trait.NPCLocations.size(); i++) {
                    if (trait.NPCLocations.get(i).LocationIdent.equals(destUUID)) {
                        containsLocation = true;
                        destID = i;
                        break;
                    }
                }
            }

            if (!containsLocation) {
                DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                        "destinations", "Console_Messages.quests_error", "Location Reward has NPC ("
                                + targetNPC + ") but is missing location (" + locationString + ")");
                return;
            }

            // Notify all plugins that the location has been reached
            for (DestinationsAddon plugin : DestinationsPlugin.Instance.getPluginManager.getPlugins()) {
                if (trait.enabledPlugins.contains(plugin.getActionName())) {
                    try {
                        plugin.onNewDestination(npc, trait, trait.NPCLocations.get(destID));
                    } catch (Exception err) {
                        StringWriter sw = new StringWriter();
                        err.printStackTrace(new PrintWriter(sw));
                        DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                                "destinations", "Console_Messages.plugin_error", err.getMessage() + "\n" + sw);
                    }
                }
            }

            // Fire the navigation event
            Navigation_NewDestination newLocation = new Navigation_NewDestination(npc, trait.NPCLocations
                    .get(destID), true);
            Bukkit.getServer().getPluginManager().callEvent(newLocation);

            npc.getNavigator().cancelNavigation();
            trait.clearPendingDestinations();
            trait.lastResult = "Forced location";
            trait.setLocation = trait.NPCLocations.get(destID);
            DestinationsPlugin.Instance.getCitizensProc.fireLocationChangedEvent(trait, trait.NPCLocations.get(destID));

            trait.currentLocation = trait.NPCLocations.get(destID);
            trait.setLocationLockUntil(LocalDateTime.now().plusSeconds(duration));
            trait.lastPositionChange = LocalDateTime.now();
            trait.setRequestedAction(NPCDestinationsTrait.en_RequestedAction.SET_LOCATION);
        }
    }
}
