package com.AkkhaTileEffectHider;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Renderable;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "Akkha Tile Effect Hider", description = "Hides Akkha's shadow attack after Akkha starts his memory special")
public class AkkhaTileEffectHiderPlugin extends Plugin
{
	private static final Set<Integer> GRAPHIC_IDS = ImmutableSet.of(2256, 2257, 2258, 2259);
	private static final Set<Integer> NPC_IDS = ImmutableSet.of(11790, 11791, 11792);
	private static final int HIDE_TICKS = 6;

	// Center 2x2's south-west tile (template coordinates)
	private static final int CENTER_SW_X = 3680;
	private static final int CENTER_SW_Y = 5407;
	private static final int PLANE = 1;

	@Inject private Client client;
	@Inject private Hooks hooks;
	@Inject private AkkhaTileEffectHiderConfig config;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;
	private int ticksRemaining;

	@Provides
	AkkhaTileEffectHiderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AkkhaTileEffectHiderConfig.class);
	}

	@Override
	protected void startUp()
	{
		hooks.registerRenderableDrawListener(drawListener);
	}

	@Override
	protected void shutDown()
	{
		hooks.unregisterRenderableDrawListener(drawListener);
		ticksRemaining = 0;
	}

	// Akkha disappears: either he started the memory special or the shadow slam can't happen so it doesn't matter
	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (NPC_IDS.contains(npc.getId()))
		{
			ticksRemaining = HIDE_TICKS;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (ticksRemaining > 0)
		{
			ticksRemaining--;
		}
	}

	private boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (ticksRemaining > 0 && renderable instanceof GraphicsObject)
		{
			GraphicsObject go = (GraphicsObject) renderable;
			if (GRAPHIC_IDS.contains(go.getId()) && inArea(go.getLocation()))
			{
				return false;
			}
		}
		return true;
	}

	private boolean inArea(LocalPoint lp)
	{
		if (lp == null)
		{
			return false;
		}
		WorldPoint wp = WorldPoint.fromLocalInstance(client, lp);
		if (wp.getPlane() != PLANE)
		{
			return false;
		}

		int size = config.size();
		if (size % 2 != 0)
		{
			size++; // snap odd sizes up to the next even number
		}

		int offset = (size - 2) / 2;
		int dx = wp.getX() - (CENTER_SW_X - offset);
		int dy = wp.getY() - (CENTER_SW_Y - offset);
		return dx >= 0 && dx < size && dy >= 0 && dy < size;
	}
}
