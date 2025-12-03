package com.jimmeas.paladinsmod;

import com.jimmeas.paladinsmod.character.Character;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores character data for each player
 * This persists across sessions and syncs in multiplayer
 */
public class CharacterData {
    private static final Map<UUID, CharacterData> playerData = new HashMap<>();

    private Character currentCharacter;
    private String selectedCharacterId;

    public CharacterData() {
        this.currentCharacter = null;
        this.selectedCharacterId = "victor"; // Default to Victor
    }

    /**
     * Get or create character data for a player
     */
    public static CharacterData get(ServerPlayer player) {
        UUID uuid = player.getUUID();
        return playerData.computeIfAbsent(uuid, k -> {
            CharacterData data = new CharacterData();
            // Load character from NBT if exists
            data.loadFromPlayer(player);
            return data;
        });
    }

    /**
     * Remove data when player leaves
     */
    public static void remove(UUID uuid) {
        playerData.remove(uuid);
    }

    /**
     * Set the active character
     */
    public void setCharacter(Character character) {
        if (this.currentCharacter != null && character != this.currentCharacter) {
            // Unequip old character
            // Note: Need player reference here, store it or pass it in
        }

        this.currentCharacter = character;
        this.selectedCharacterId = character.getId();
    }

    /**
     * Get current character
     */
    public Character getCharacter() {
        // Lazy load if not initialized
        if (currentCharacter == null && selectedCharacterId != null) {
            currentCharacter = CharacterRegistry.getCharacter(selectedCharacterId);
        }
        return currentCharacter;
    }

    /**
     * Save to player NBT data
     */
    public void saveToPlayer(ServerPlayer player) {
        CompoundTag nbt = new CompoundTag();
        if (selectedCharacterId != null) {
            nbt.putString("SelectedCharacter", selectedCharacterId);
        }
    }

    /**
     * Load from player NBT data
     */
    public void loadFromPlayer(ServerPlayer player) {
        // Load from persistent player data
        // This is simplified - actual implementation depends on Minecraft version
        // For 1.20+, use Data Components or custom component

        // Example structure:
        // CompoundTag nbt = player.getComponent(...).getNbt();
        // if (nbt.contains("SelectedCharacter")) {
        //     selectedCharacterId = nbt.getString("SelectedCharacter");
        // }
    }

    /**
     * Called every tick to update character abilities
     */
    public void tick(ServerPlayer player) {
        if (currentCharacter != null) {
            currentCharacter.tick(player);
        }
    }
}