package com.jimmeas.paladinsmod.character.impl;

import com.jimmeas.paladinsmod.ability.impl.*;
import com.jimmeas.paladinsmod.character.Character;

public class VictorCharacter extends Character {

    public VictorCharacter() {
        super("victor", "Victor");
    }

    @Override
    protected void setupAbilities() {
        // Ability 1: Frag Grenade (Q)
        setAbility(0, new FragGrenadeAbility());

        // Ability 2: Iron Sights (Crouch - Passive)
        setAbility(1, new IronSightsAbility());

        // Ability 3: Hustle/Sprint (Shift - Native Minecraft Sprint)
        setAbility(2, new HustleAbility());

        // Ultimate: Tactical Visor (R)
        setAbility(3, new TacticalVisorAbility());
    }
}