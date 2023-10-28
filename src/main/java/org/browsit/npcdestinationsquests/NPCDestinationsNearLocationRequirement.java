package org.browsit.npcdestinationsquests;

import me.pikamug.quests.module.BukkitCustomRequirement;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Map;
import java.util.UUID;

public class NPCDestinationsNearLocationRequirement extends BukkitCustomRequirement {

    public NPCDestinationsNearLocationRequirement() {
        setName("NPC Destinations Near Location Requirement");
        setAuthor("Browsit, LLC");
        setItem("COMPASS", (short)0);
        setDisplay("Individual is too far from required location. Try again later!");
        addStringPrompt("NPC ID", "Citizens NPC ID", "ANY");
        addStringPrompt("Location ID", "Location ID to be near", "ANY");
        addStringPrompt("Max Distance", "Maximum block distance from location", "ANY");
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
        if (data != null && data.containsKey("NPC ID") && data.containsKey("Location ID")
                && data.containsKey("Max Distance")) {

            UUID destUUID = null;
            int destID = -1;
            int destDistance = -1;
            int targetNPC = -1;

            String npcIDString = (String) data.get("NPC ID");
            String locationString = (String)data.get("Location ID");
            String maxDistanceString = (String) data.get("Max Distance");

            if (NumberUtils.isNumber(npcIDString)) {
                targetNPC = Integer.parseInt(npcIDString);
            }

            if (NumberUtils.isNumber(locationString)) {
                destID = Integer.parseInt(locationString);
            } else {
                destUUID = UUID.fromString(locationString);
            }

            if (NumberUtils.isNumber(maxDistanceString)) {
                destDistance = Integer.parseInt(maxDistanceString);
            }

            final NPC npc = CitizensAPI.getNPCRegistry().getById(targetNPC);
            if (npc == null) {
                DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                        "destinations", "Console_Messages.quests_error", "Near Location Requirement has invalid NPC ID "
                                + targetNPC);
                return false;
            }

            NPCDestinationsTrait trait;
            if (!npc.hasTrait(NPCDestinationsTrait.class)) {
                DestinationsPlugin.Instance.getMessageManager.consoleMessage(DestinationsPlugin.Instance,
                        "destinations", "Console_Messages.quests_error", "Near Location Requirement has NPC ("
                                + targetNPC + "), but lacks the NPCDestination trait.");
                return false;
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
                        "destinations", "Console_Messages.quests_error", "Near Location Requirement has NPC ("
                                + targetNPC + ") but is missing location (" + locationString + ")");
                return false;
            }

            return trait.NPCLocations.get(destID).destination.distance(npc.getEntity().getLocation()) <= destDistance;
        }
        return false;
    }
}
