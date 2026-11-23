package com.AkkhaTileEffectHider;

import com.AkkhaTileEffectHider.AkkhaTileEffectHiderPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AkkhaTileEffectHiderTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AkkhaTileEffectHiderPlugin.class);
		RuneLite.main(args);
	}
}