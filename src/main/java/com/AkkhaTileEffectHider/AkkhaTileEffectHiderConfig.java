package com.AkkhaTileEffectHider;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("akkhatileeffecthider")
public interface AkkhaTileEffectHiderConfig extends Config
{
    @Range(min = 2, max = 20)
    @ConfigItem(
            keyName = "size",
            name = "Area size",
            description = "Width and height in tiles of the square area, grown evenly around the center 2x2. Odd values round up to the next even number."
    )
    default int size()
    {
        return 6;
    }
}
