package space.mori.regenblock;

import java.util.logging.*;
import org.bukkit.plugin.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;

public class RegenBlockLogHandler
{
    private RegenBlock plugin;
    private Logger logger;
    
    public RegenBlockLogHandler(final RegenBlock plugin) {
        this.plugin = plugin;
        this.logger = Logger.getLogger("Minecraft");
    }
    
    private String buildString(final String message) {
        final PluginDescriptionFile pdFile = this.plugin.getDescription();
        return "[" + pdFile.getName() + "] (" + pdFile.getVersion() + ") " + message;
    }
    
    private String buildStringPlayer(final String message, final ChatColor color) {
        final PluginDescriptionFile pdFile = this.plugin.getDescription();
        return color + "[" + pdFile.getName() + "] " + message;
    }
    
    public void info(final String message) {
        this.logger.info(this.buildString(message));
    }
    
    public void warn(final String message) {
        this.logger.warning(this.buildString(message));
    }
    
    public void sendPlayerNormal(final Player player, final String message) {
        player.sendMessage(this.buildStringPlayer(message, ChatColor.AQUA));
    }
    
    public void sendPlayerWarn(final Player player, final String message) {
        player.sendMessage(this.buildStringPlayer(message, ChatColor.YELLOW));
    }
    
    public void sendPlayerRegionInfo(final Player player, final String regionName) {
        final String left = this.plugin.getConfiguration().getRegionLeft(regionName);
        final String right = this.plugin.getConfiguration().getRegionRight(regionName);
        final String worldName = this.plugin.getConfiguration().getRegionWorldName(regionName);
        final int respawnTime = this.plugin.getConfiguration().getRegionRespawnTime(regionName);
        final int sync = this.plugin.getConfiguration().getRegionSync(regionName);
        final int type = this.plugin.getConfiguration().getRegionType(regionName);
        final int feedbackId = this.plugin.getConfiguration().getRegionFeedbackID(regionName);
        final int alarmTime = this.plugin.getConfiguration().getRegionAlarmTime(regionName);
        final int alarmRadius = this.plugin.getConfiguration().getRegionAlarmRadius(regionName);
        final String alarmMessage = this.plugin.getConfiguration().getRegionAlarmMessage(regionName);
        this.sendPlayerNormal(player, String.valueOf(regionName) + ": [W] " + worldName + "; [L] " + left + "; [R] " + right + "; [T] " + respawnTime + "s.");
        this.sendPlayerNormal(player, "[Sync] " + sync + "; [Type] " + type + "; [Feedback ID] " + feedbackId);
        this.sendPlayerNormal(player, "Alarm [Time] " + alarmTime + "; [Radius] " + alarmRadius + "; [Message] " + alarmMessage);
    }
    
    public void listRegion(final Player player, final Set<String> listRegion) {
        for (final String regionName : listRegion) {
            final String left = this.plugin.getConfiguration().getRegionLeft(regionName);
            final String right = this.plugin.getConfiguration().getRegionRight(regionName);
            final String worldName = this.plugin.getConfiguration().getRegionWorldName(regionName);
            final int respawnTime = this.plugin.getConfiguration().getRegionRespawnTime(regionName);
            this.sendPlayerNormal(player, String.valueOf(regionName) + ": [W] " + worldName + " [L] " + left + "; [R] " + right + "; [T] " + respawnTime + "s.");
        }
    }
}
