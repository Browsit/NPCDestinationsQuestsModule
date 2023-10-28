package org.browsit.npcdestinationsquests;

import me.pikamug.quests.module.BukkitCustomRequirement;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.utilities.Utilities;

import java.util.Map;
import java.util.UUID;

public class NPCDestinationsExactLocationRequirement extends BukkitCustomRequirement {

    public NPCDestinationsExactLocationRequirement() {
        setName("NPC Destinations Exact Location Requirement");
        setAuthor("Browsit, LLC");
        setItem("BEACON", (short)0);
        setDisplay("Individual is not in required location. Try again later!");
        addStringPrompt("NPC ID", "Citizens NPC ID", "ANY");
        addStringPrompt("Location ID", "Location ID for NPC to be at", "ANY");
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
    public boolean testRequirement(UUID uuid, Map<String, Object> data) {
        if (data != null && data.containsKey("NPC ID") && data.containsKey("Location ID")) {

            int npcID = -1;
            if (Utilities.tryParseInt((String) data.get("NPC ID"))) {
                npcID = Integer.parseInt((String) data.get("NPC ID"));
            }

            if (npcID == -1) {
                return false;
            }

            NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
            if (npc == null) {
                DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                        "destinations", "Console_Messages.quests_error", "Exact Location Requirement has invalid NPC ID "
                                + npcID);
                return false;
            }

            NPCDestinationsTrait trait;
            if (!npc.hasTrait(NPCDestinationsTrait.class)) {
                DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                        "destinations", "Console_Messages.quests_error", "Exact Location Requirement has NPC ("
                                + npcID + "), but lacks the NPCDestination trait.");
                return false;
            } else {
                trait = npc.getTrait(NPCDestinationsTrait.class);
            }

            String locationString = (String)data.get("Location ID");

            if (locationString.contains("-")) {
                return trait.currentLocation.LocationIdent.toString().equalsIgnoreCase(locationString);
            } else {
                int destID = -1;
                if (Utilities.tryParseInt((String) data.get("Location ID")))
                    destID = Integer.parseInt((String) data.get("Location ID"));

                if (destID >= trait.NPCLocations.size()) {
                    DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                            "destinations", "Requirement_Messages.quests_error", "Exact Location Requirement has NPC ("
                                    + npcID + ") but is missing location (" + destID + ")");
                    return false;
                }
                return trait.NPCLocations.get(destID).destination.toString().equals(trait.currentLocation.destination.toString());
            }
        }
        return false;
    }
}
