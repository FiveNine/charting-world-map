package com.ChartingWorldMap;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChartingWorldMapPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ChartingWorldMapPlugin.class);
		RuneLite.main(args);
	}
}