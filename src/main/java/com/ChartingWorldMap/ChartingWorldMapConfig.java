package com.ChartingWorldMap;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ChartingWorldMapConfig.GROUP)
public interface ChartingWorldMapConfig extends Config
{
	String GROUP = "ChartingWorldMap";
	
	@ConfigItem(
		position = 1,
		keyName = "hideCompletedMarkers",
		name = "Hide completed",
		description = "Hide completed charting locations on the world map"
	)
	default boolean hideCompletedMarkers()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "hideGenericMarkers",
		name = "Hide generic markers",
		description = "Hide generic charting locations on the world map"
	)
	default boolean hideGenericMarkers()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "hideSpyglassMarkers",
		name = "Hide spyglass markers",
		description = "Hide spyglass charting locations on the world map"
	)
	default boolean hideSpyglassMarkers()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "hideCrateMarkers",
		name = "Hide drink crate markers",
		description = "Hide drink crate charting locations on the world map"
	)
	default boolean hideCrateMarkers()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "hideDuckMarkers",
		name = "Hide duck markers",
		description = "Hide duck charting locations on the world map"
	)
	default boolean hideDuckMarkers()
	{
		return false;
	}

	@ConfigItem(
		position = 6,
		keyName = "hideDepthMarkers",
		name = "Hide Diving markers",
		description = "Hide Diving charting locations on the world map"
	)
	default boolean hideDepthMarkers()
	{
		return false;
	}

	@ConfigItem(
		position = 7,
		keyName = "hideWeatherMarkers",
		name = "Hide weather markers",
		description = "Hide weather charting locations on the world map"
	)
	default boolean hideWeatherMarkers()
	{
		return false;
	}

}
