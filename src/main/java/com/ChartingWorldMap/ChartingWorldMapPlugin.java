package com.ChartingWorldMap;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "Charting World Map"
)
public class ChartingWorldMapPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ChartingWorldMapConfig config;

	@Inject
	private WorldMapPointManager worldMapPointManager;


	private static final String[] CRATE_ICON = {"Charting_icon_-_Crate.png", "Charting_icon_-_Crate_complete.png"};
	private static final String[] DEPTH_ICON = {"Charting_icon_-_Depth.png", "Charting_icon_-_Depth_complete.png"};
	private static final String[] GENERIC_ICON = {"Charting_icon_-_Generic.png", "Charting_icon_-_Generic_complete.png"};
	private static final String[] SPYGLASS_ICON = {"Charting_icon_-_Spyglass.png", "Charting_icon_-_Spyglass_complete.png"};
	private static final String[] WEATHER_ICON = {"Charting_icon_-_Weather.png", "Charting_icon_-_Weather_complete.png"};
	private static final String[] DUCK_ICON = {"Charting_icon_-_Duck.png", "Charting_icon_-_Duck_complete.png"};

	private static final BufferedImage[] CrateIcons = new BufferedImage[2];
	private static final BufferedImage[] DepthIcons = new BufferedImage[2];
	private static final BufferedImage[] GenericIcons = new BufferedImage[2];
	private static final BufferedImage[] SpyglassIcons = new BufferedImage[2];
	private static final BufferedImage[] WeatherIcons = new BufferedImage[2];
	private static final BufferedImage[] DuckIcons = new BufferedImage[2];

	private static final int startChartingVarbitId = 18574;
	private final List<MarkerData> allMarkerData = new ArrayList<MarkerData>();
	private final List<WorldMapPoint> addedWorldMapPoints = new ArrayList<WorldMapPoint>();

	private boolean hideCompletedMarkers;
	private boolean hideCrateMarkers;
	private boolean hideDepthMarkers;
	private boolean hideGenericMarkers;
	private boolean hideSpyglassMarkers;
	private boolean hideWeatherMarkers;
	private boolean hideDuckMarkers;


	@Override
	protected void startUp() throws Exception
	{
		initializeChartingMarkers();
		loadChartingIcons();
	}

	@Override
	protected void shutDown() throws Exception
	{
		clearAddedWorldMapPoints();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			syncChartingProgress();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		if (e.getVarbitId() >= startChartingVarbitId && e.getVarbitId() < startChartingVarbitId + allMarkerData.size())
		{
			var markerIndex = e.getVarbitId() - startChartingVarbitId;
			if (markerIndex >= 0 && markerIndex < allMarkerData.size())
			{
				allMarkerData.stream()
					.filter(marker -> Integer.parseInt(marker.id) == markerIndex)
					.findFirst()
					.ifPresent(marker -> marker.isComplete = e.getValue() == 1);
				updateSingleIcon(markerIndex);
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(ChartingWorldMapConfig.GROUP))
		{
			updateConfig();
			updateAllIcons();
		}
	}

	@Provides
	ChartingWorldMapConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChartingWorldMapConfig.class);
	}

	private void updateConfig()
	{
		hideCompletedMarkers = config.hideCompletedMarkers();
		hideCrateMarkers = config.hideCrateMarkers();
		hideDepthMarkers = config.hideDepthMarkers();
		hideGenericMarkers = config.hideGenericMarkers();
		hideSpyglassMarkers = config.hideSpyglassMarkers();
		hideWeatherMarkers = config.hideWeatherMarkers();
		hideDuckMarkers = config.hideDuckMarkers();
	}

	private void syncChartingProgress()
	{
		int completedMarkers = 0;
		for (MarkerData marker : allMarkerData)
		{
			var varbitId = startChartingVarbitId + Integer.parseInt(marker.id);
			if (client.getVarbitValue(varbitId) == 1)
			{
				marker.isComplete = true;
				completedMarkers++;
			}
		}

		updateAllIcons();
	}


	private void clearAddedWorldMapPoints()
	{
		for (WorldMapPoint point : addedWorldMapPoints)
		{
			worldMapPointManager.remove(point);
		}
		addedWorldMapPoints.clear();
	}

	private void updateIcon(MarkerData marker)
	{

		if (hideCompletedMarkers && marker.isComplete) return;
		if (hideCrateMarkers && Objects.equals(marker.categoryId, "drink_crate_charting")) return;
		if (hideDepthMarkers && Objects.equals(marker.categoryId, "depth_charting")) return;
		if (hideGenericMarkers && Objects.equals(marker.categoryId, "sea_charting")) return;
		if (hideSpyglassMarkers && Objects.equals(marker.categoryId, "spyglass_charting")) return;
		if (hideWeatherMarkers && Objects.equals(marker.categoryId, "weather_charting")) return;
		if (hideDuckMarkers && Objects.equals(marker.categoryId, "current_duck_charting")) return;


		var icon = getWikiChartingIcon(marker.categoryId);
		var point = WorldMapPoint.builder()
			.worldPoint(new WorldPoint(marker.position[0], marker.position[1], 0))
			.tooltip(marker.popup.title)
			.image(icon[marker.isComplete ? 1 : 0])
			.build();
		worldMapPointManager.add(point);
		addedWorldMapPoints.add(point);
	}

	private void updateSingleIcon(int markerIndex)
	{
		var marker = allMarkerData.stream().filter(m -> Integer.parseInt(m.id) == markerIndex).findFirst().orElse(null);
		if (marker != null)
		{
			for (WorldMapPoint point : addedWorldMapPoints)
			{
				if (point.getWorldPoint().equals(new WorldPoint(marker.position[0], marker.position[1], 0)))
				{
					worldMapPointManager.remove(point);
					addedWorldMapPoints.remove(point);
					break;
				}
			}
			updateIcon(marker);
		}
	}

	private void updateAllIcons()
	{
		clearAddedWorldMapPoints();

		for (MarkerData marker : allMarkerData)
		{
			updateIcon(marker);
		}
	}

	private void initializeChartingMarkers()
	{
		try (InputStream in = getClass().getResourceAsStream("/com/ChartingWorldMap/ChartingMarkers.json"))
		{
			if (in == null)
			{
				log.error("Resource ChartingMarkers.json not found in classpath!");
				return;
			}

			Gson gson = new Gson();
			Type listType = new TypeToken<List<MarkerData>>() {}.getType();
			allMarkerData.addAll(gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), listType));
		}
		catch (Exception e)
		{
			log.error("Failed to load charting markers from JSON", e);
		}
	}

	private void loadChartingIcons()
	{
		CrateIcons[0] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, CRATE_ICON[0]);
		CrateIcons[1] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, CRATE_ICON[1]);
		DepthIcons[0] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, DEPTH_ICON[0]);
		DepthIcons[1] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, DEPTH_ICON[1]);
		GenericIcons[0] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, GENERIC_ICON[0]);
		GenericIcons[1] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, GENERIC_ICON[1]);
		SpyglassIcons[0] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, SPYGLASS_ICON[0]);
		SpyglassIcons[1] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, SPYGLASS_ICON[1]);
		WeatherIcons[0] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, WEATHER_ICON[0]);
		WeatherIcons[1] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, WEATHER_ICON[1]);
		DuckIcons[0] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, DUCK_ICON[0]);
		DuckIcons[1] = ImageUtil.loadImageResource(ChartingWorldMapPlugin.class, DUCK_ICON[1]);
	}

	private BufferedImage[] getWikiChartingIcon(String categoryId)
	{
		switch (categoryId)
		{
			case "spyglass_charting":
				return SpyglassIcons;
			case "drink_crate_charting":
				return CrateIcons;
			case "depth_charting":
				return DepthIcons;
			case "weather_charting":
				return WeatherIcons;
			case "current_duck_charting":
				return DuckIcons;
			default:
				return GenericIcons;
		}
	}

	private static class MarkerData
	{
		String categoryId;
		int[] position;
		Popup popup;
		String id;
		boolean isComplete = false;

		static class Popup
		{
			String title;
			String description;
		}
	}
}
