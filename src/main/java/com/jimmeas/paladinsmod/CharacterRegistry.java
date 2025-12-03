package com.jimmeas.paladinsmod;

import com.jimmeas.paladinsmod.character.Character;
import com.jimmeas.paladinsmod.character.impl.VictorCharacter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for all available characters
 * Add new characters here
 */
public class CharacterRegistry {
    private static final Map<String, Character> characters = new HashMap<>();

    /**
     * Register all characters
     * Call this during mod initialization
     */
    public static void register() {
        // Register Victor
        registerCharacter(new VictorCharacter());

        // ADD NEW CHARACTERS HERE:
        // registerCharacter(new YourCustomCharacter());

        System.out.println("Registered " + characters.size() + " characters");
    }

    /**
     * Register a single character
     */
    public static void registerCharacter(Character character) {
        if (characters.containsKey(character.getId())) {
            System.err.println("Character with ID '" + character.getId() + "' already registered!");
            return;
        }

        characters.put(character.getId(), character);
        System.out.println("Registered character: " + character.getName());
    }

    /**
     * Get a character by ID
     */
    public static Character getCharacter(String id) {
        return characters.get(id);
    }

    /**
     * Get all registered character IDs
     */
    public static Set<String> getAllCharacterIds() {
        return characters.keySet();
    }

    /**
     * Get all registered characters
     */
    public static Map<String, Character> getAllCharacters() {
        return new HashMap<>(characters);
    }
}