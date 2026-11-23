package com.AkkhaTileEffectHider;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Renderable;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.client.callback.Hooks;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;

@PluginDescriptor(name = "Akkha Tile Effect Hider", description = "Hides Akkha's Shadow Attack over the memory puzzle so they no longer overlap")
public class AkkhaTileEffectHiderPlugin extends Plugin
{
	private static final Set<Integer> IDS = ImmutableSet.of(2256, 2257, 2258, 2259);

	// South-west corner of the 4x4 area (template coordinates)
	private static final int CENTER_SW_X = 3680;
	private static final int CENTER_SW_Y = 5407;
	private static final int PLANE = 1;

	@Inject private Client client;
	@Inject private Hooks hooks;
	@Inject private AkkhaTileEffectHiderConfig config;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;
	private boolean hideActive;

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
		hideActive = false;
	}

	// Detection: all four IDs present anywhere
	@Subscribe
	public void onClientTick(ClientTick tick)
	{
		Set<Integer> found = new HashSet<>();
		for (GraphicsObject go : client.getTopLevelWorldView().getGraphicsObjects())
		{
			if (IDS.contains(go.getId()))
			{
				found.add(go.getId());
				if (found.size() == IDS.size())
				{
					break;
				}
			}
		}
		hideActive = found.size() == IDS.size();
	}

	// Hiding: only inside the 4x4
	private boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (hideActive && renderable instanceof GraphicsObject)
		{
			GraphicsObject go = (GraphicsObject) renderable;
			if (IDS.contains(go.getId()) && inArea(go.getLocation()))
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
