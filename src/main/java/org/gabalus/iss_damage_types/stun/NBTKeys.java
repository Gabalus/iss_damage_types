package org.gabalus.iss_damage_types.stun;

public final class NBTKeys {
    private NBTKeys(){}
    public static final String GAUGE        = "iss_stun_gauge";
    public static final String LAST_UPDATE  = "iss_stun_last_update";
    public static final String LAST_HIT     = "iss_stun_last_hit"; // for 4s decay delay

    public static final String LAST_ELEM_ADD      = "iss_last_elem_add";
    public static final String LAST_ELEM_TICK     = "iss_last_elem_tick";
    public static final String LAST_ELEM_ATTACKER = "iss_last_elem_attacker";

    public static final String STUN_CD     = "iss_stun_cd";

    // shield helpers
    public static final String BLOCK_TICK   = "iss_block_last_tick";
    public static final String BLOCK_AMOUNT = "iss_block_last_amount";
}
