package space.mori.regenblock;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.block.*;
import org.bukkit.*;

public class RegenBlockCommandExecutor implements CommandExecutor
{
    private RegenBlock plugin;
    
    public RegenBlockCommandExecutor(final RegenBlock instance) {
        this.plugin = instance;
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            this.plugin.getLog().info("/rb is only available in game.");
            return true;
        }
        if (args.length == 0) {
            return false;
        }
        final Player player = (Player)sender;
        final RootCommand rootCommand = this.getRootCommand(args[0].toLowerCase());
        if (rootCommand != null) {
            this.execute(rootCommand, args, player);
            return true;
        }
        return false;
    }
    
    private void execute(final RootCommand rootCommand, final String[] args, final Player player) {
        switch (rootCommand) {
            case monitor: {
                this.executeMonitor(args, player);
                break;
            }
            case test: {
                this.executeTest(args, player);
                break;
            }
            case reload: {
                this.executeReload(args, player);
                break;
            }
            case blacklist: {
                this.executeBlacklist(args, player);
                break;
            }
            case listselection: {
                this.executeListselection(args, player);
                break;
            }
            case edit: {
                this.executeEdit(args, player);
                break;
            }
            case select: {
                this.executeSelect(args, player);
                break;
            }
            case create: {
                this.executeCreate(args, player);
                break;
            }
            case remove: {
                this.executeRemove(args, player);
                break;
            }
            case modify: {
                this.executeModify(args, player);
                break;
            }
            case list: {
                this.executeList(args, player);
                break;
            }
            case rblacklist: {
                this.executeRblacklist(args, player);
                break;
            }
            case type: {
                this.executeType(args, player);
                break;
            }
            case sync: {
                this.executeSync(args, player);
                break;
            }
            case alarm: {
                this.executeAlarm(args, player);
                break;
            }
            case feedback: {
                this.executeFeedback(args, player);
                break;
            }
            case spawnblock: {
                this.executeSpawnblock(args, player);
                break;
            }
            case info: {
                this.executeInfo(args, player);
                break;
            }
            case repop: {
                this.executeRepop(args, player);
                break;
            }
        }
    }
    
    private void executeRepop(final String[] args, final Player player) {
        if (args.length < 2) {
            this.printUse(player, RootCommand.repop);
            return;
        }
        final String regionName = args[1].toLowerCase();
        if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
            this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
            return;
        }
        this.plugin.getQueue().regenRegion(regionName);
    }
    
    private void executeInfo(final String[] args, final Player player) {
        final Block block = player.getTargetBlock((HashSet)null, 100);
        if (block == null) {
            this.plugin.getLog().sendPlayerWarn(player, "No region targeted.");
            return;
        }
        final String regionName = this.plugin.getListenerBlock().getBlockRegion(block.getLocation());
        if (regionName == null) {
            this.plugin.getLog().sendPlayerWarn(player, "No region targeted.");
            return;
        }
        this.plugin.getLog().sendPlayerRegionInfo(player, regionName);
    }
    
    private void executeSpawnblock(final String[] args, final Player player) {
        if (args.length < 2) {
            this.printUse(player, RootCommand.spawnblock);
            return;
        }
        if (args[1].equalsIgnoreCase("add")) {
            final String regionName = args[2].toLowerCase();
            if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
                this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
                return;
            }
            if (args.length < 5) {
                this.printUse(player, RootCommand.spawnblock);
                return;
            }
            for (int i = 3; i + 1 < args.length; i += 2) {
                Material type = Material.AIR;
                try {
                    type = Material.getMaterial(args[i]);
                }
                catch (NumberFormatException ex) {}
                int chance = 0;
                try {
                    chance = Integer.parseInt(args[i + 1]);
                }
                catch (NumberFormatException ex2) {}
                if (type != null && chance > 0) {
                    this.plugin.getConfiguration().setRegionSpawnBlock(regionName, type, chance);
                }
            }
            this.plugin.getLog().sendPlayerNormal(player, "Region spawn blocks: " + this.plugin.getConfiguration().getRegionSpawnBlocks(regionName));
        }
        else if (args[1].equalsIgnoreCase("remove")) {
            final String regionName = args[2].toLowerCase();
            if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
                this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
                return;
            }
            if (args.length < 4) {
                this.printUse(player, RootCommand.spawnblock);
                return;
            }
            for (int i = 3; i < args.length; ++i) {
                Material type = Material.AIR;
                try {
                    type = Material.getMaterial(args[i]);
                }
                catch (NumberFormatException ex3) {}
                if (type != null) {
                    this.plugin.getConfiguration().removeRegionSpawnBlock(regionName, type);
                }
            }
            this.plugin.getLog().sendPlayerNormal(player, "Region spawn blocks: " + this.plugin.getConfiguration().getRegionSpawnBlocks(regionName));
        }
        else {
            final String regionName = args[1].toLowerCase();
            if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
                this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
                return;
            }
            this.plugin.getLog().sendPlayerNormal(player, "Region spawn blocks: " + this.plugin.getConfiguration().getRegionSpawnBlocks(regionName));
        }
    }
    
    private void executeFeedback(final String[] args, final Player player) {
        if (args.length < 3) {
            this.printUse(player, RootCommand.feedback);
            return;
        }
        if (args[1].equalsIgnoreCase("set")) {
            String feedbackString = "";
            for (int i = 3; i < args.length; ++i) {
                feedbackString = String.valueOf(feedbackString) + args[i] + " ";
            }
            if (feedbackString.length() > 0) {
                this.plugin.getConfiguration().setFeedbackString(feedbackString);
                this.plugin.getLog().sendPlayerNormal(player, "Feedback string was set to [" + feedbackString + "]");
            }
            else {
                this.plugin.getLog().sendPlayerWarn(player, "Feedback string was not changed.");
            }
            return;
        }
        final String regionName = args[1].toLowerCase();
        if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
            this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
            return;
        }
        int feedbackId = 0;
        try {
            feedbackId = Integer.parseInt(args[3]);
        }
        catch (NumberFormatException ex) {}
        feedbackId = this.plugin.getConfiguration().setRegionFeedbackID(regionName, feedbackId);
        this.plugin.getLog().sendPlayerNormal(player, "Region " + regionName + " feedback type was set to " + feedbackId);
    }
    
    private void executeAlarm(final String[] args, final Player player) {
        if (args.length < 4) {
            this.printUse(player, RootCommand.alarm);
            return;
        }
        final String regionName = args[2].toLowerCase();
        if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
            this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
            return;
        }
        if (args[1].equalsIgnoreCase("time")) {
            int time = 0;
            try {
                time = Integer.parseInt(args[3]);
            }
            catch (NumberFormatException ex) {}
            this.plugin.getConfiguration().setRegionAlarmTime(regionName, time);
            this.plugin.getLog().sendPlayerNormal(player, "Alarm time of [" + regionName + "] was set to " + time + ".");
        }
        if (args[1].equalsIgnoreCase("message")) {
            String message = "";
            for (int i = 4; i < args.length; ++i) {
                message = String.valueOf(message) + args[i] + " ";
            }
            this.plugin.getConfiguration().setRegionAlarmMessage(regionName, message);
            this.plugin.getLog().sendPlayerNormal(player, "Alarm message of [" + regionName + "] was set to [" + message + "].");
        }
        if (args[1].equalsIgnoreCase("radius")) {
            int radius = 0;
            try {
                radius = Integer.parseInt(args[3]);
            }
            catch (NumberFormatException ex2) {}
            this.plugin.getConfiguration().setRegionAlarmRadius(regionName, radius);
            this.plugin.getLog().sendPlayerNormal(player, "Alarm radius of [" + regionName + "] was set to " + radius + ".");
        }
    }
    
    private void executeSync(final String[] args, final Player player) {
        if (args.length < 2) {
            this.printUse(player, RootCommand.sync);
            return;
        }
        final String regionName = args[1].toLowerCase();
        if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
            this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
            return;
        }
        if (args.length < 3) {
            this.plugin.getLog().sendPlayerNormal(player, "Region [" + regionName + "] sync is " + this.plugin.getConfiguration().getRegionSync(regionName));
            return;
        }
        int sync = 0;
        try {
            sync = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException ex) {}
        this.plugin.getConfiguration().setRegionSync(regionName, sync);
        this.plugin.getLog().sendPlayerNormal(player, "Region [" + regionName + "] sync was set to " + sync);
    }
    
    private void executeType(final String[] args, final Player player) {
        if (args.length < 2) {
            this.printUse(player, RootCommand.type);
            return;
        }
        final String regionName = args[1].toLowerCase();
        if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
            this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
            return;
        }
        if (args.length < 3) {
            this.plugin.getLog().sendPlayerNormal(player, "Region [" + regionName + "] type is " + this.plugin.getConfiguration().getRegionType(regionName));
            return;
        }
        int type = 0;
        try {
            type = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException ex) {}
        this.plugin.getConfiguration().setRegionType(regionName, type);
        if (type == 1) {
            this.plugin.getConfiguration().regionAddSpawnBlocks(regionName);
        }
        this.plugin.getLog().sendPlayerNormal(player, "Region [" + regionName + "] type was set to " + type);
    }
    
    private void executeRblacklist(final String[] args, final Player player) {
        if (args.length < 2) {
            this.printUse(player, RootCommand.rblacklist);
            return;
        }
        final String regionName = args[1].toLowerCase();
        if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
            this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
            return;
        }
        if (args.length == 2) {
            this.plugin.getLog().sendPlayerNormal(player, new StringBuilder().append(this.plugin.getConfiguration().listRegionBlacklistBlockId(regionName)).toString());
            return;
        }
        if (args[2].equalsIgnoreCase("add")) {
            for (int i = 3; i < args.length; ++i) {
                this.plugin.getConfiguration().addRegionBlacklistBlockId(args[2], Integer.parseInt(args[i]));
            }
        }
        else {
            if (!args[2].equalsIgnoreCase("remove")) {
                this.printUse(player, RootCommand.rblacklist);
                return;
            }
            for (int i = 3; i < args.length; ++i) {
                this.plugin.getConfiguration().removeRegionBlacklistBlockId(args[2], Integer.parseInt(args[i]));
            }
        }
        this.plugin.getLog().sendPlayerNormal(player, "Region's blacklist updated.");
    }
    
    private void executeList(final String[] args, final Player player) {
        if (this.plugin.getConfiguration().listRegions() != null) {
            this.plugin.getLog().listRegion(player, this.plugin.getConfiguration().listRegions());
        }
        else {
            this.plugin.getLog().sendPlayerNormal(player, "There are no regions.");
        }
    }
    
    private void executeModify(final String[] args, final Player player) {
        if (args.length < 2) {
            this.printUse(player, RootCommand.modify);
            return;
        }
        if (args[1].equalsIgnoreCase("time")) {
            if (args.length < 4) {
                this.printUse(player, RootCommand.modify);
                return;
            }
            final String regionName = args[2].toLowerCase();
            int respawnTime = 1;
            try {
                respawnTime = Integer.parseInt(args[3]);
            }
            catch (NumberFormatException ex) {}
            if (respawnTime < 1) {
                respawnTime = this.plugin.getConfiguration().getRegionDefaultRespawnTime();
            }
            this.plugin.getConfiguration().setRegionRespawnTime(regionName, respawnTime);
            this.plugin.getLog().sendPlayerNormal(player, "Region " + regionName + " was updated to respawn time of " + respawnTime + "s.");
            this.plugin.getLog().info(String.valueOf(player.getName()) + " updated region " + regionName + " to respawn time of " + respawnTime + "s.");
        }
        else {
            final String regionName = args[1].toLowerCase();
            if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
                this.plugin.getLog().sendPlayerWarn(player, "Region name does not exist.");
                return;
            }
            int respawnTime = 1;
            if (args.length == 3) {
                try {
                    respawnTime = Integer.parseInt(args[2]);
                }
                catch (NumberFormatException ex2) {}
                if (respawnTime < 1) {
                    respawnTime = this.plugin.getConfiguration().getRegionRespawnTime(regionName);
                }
            }
            else {
                respawnTime = this.plugin.getConfiguration().getRegionRespawnTime(regionName);
            }
            this.plugin.getConfiguration().setRegion(regionName, respawnTime, this.plugin.getListenerPlayer().getPlayerSelectionRight().get(player.getName()), this.plugin.getListenerPlayer().getPlayerSelectionLeft().get(player.getName()));
            this.plugin.getLog().sendPlayerRegionInfo(player, regionName);
        }
    }
    
    private void executeRemove(final String[] args, final Player player) {
        if (args.length == 1) {
            this.printUse(player, RootCommand.remove);
            return;
        }
        final String regionName = args[1].toLowerCase();
        if (this.plugin.getConfiguration().getRegionName(regionName) == null) {
            this.plugin.getLog().sendPlayerWarn(player, "Region " + regionName + " does not exist.");
            return;
        }
        this.plugin.getConfiguration().removeRegion(regionName);
        this.plugin.getLog().sendPlayerNormal(player, "Region " + regionName + " was removed.");
        this.plugin.getLog().info(String.valueOf(player.getName()) + " removed region " + regionName);
    }
    
    private void executeCreate(final String[] args, final Player player) {
        if (!this.plugin.getListenerPlayer().getPlayerSelectionLeft().containsKey(player.getName()) || !this.plugin.getListenerPlayer().getPlayerSelectionRight().containsKey(player.getName())) {
            this.plugin.getLog().sendPlayerWarn(player, "You need to select two points before creating a region.");
            return;
        }
        if (args.length < 2) {
            this.printUse(player, RootCommand.create);
            return;
        }
        final String regionName = args[1].toLowerCase();
        if (this.plugin.getConfiguration().getRegionName(regionName) != null) {
            this.plugin.getLog().sendPlayerWarn(player, "Region name is already in use.");
            return;
        }
        int respawnTime = 1;
        if (args.length == 3) {
            try {
                respawnTime = Integer.parseInt(args[2]);
            }
            catch (NumberFormatException ex) {}
            if (respawnTime < 1) {
                respawnTime = this.plugin.getConfiguration().getRegionDefaultRespawnTime();
            }
        }
        else {
            respawnTime = this.plugin.getConfiguration().getRegionDefaultRespawnTime();
        }
        this.plugin.getConfiguration().setRegion(regionName, respawnTime, this.plugin.getListenerPlayer().getPlayerSelectionRight().get(player.getName()), this.plugin.getListenerPlayer().getPlayerSelectionLeft().get(player.getName()));
        this.plugin.getLog().sendPlayerRegionInfo(player, regionName);
    }
    
    private void executeSelect(final String[] args, final Player player) {
        if (args.length == 1) {
            if (this.plugin.getListenerPlayer().getPlayerSelectionStatus().contains(player.getName())) {
                this.plugin.getListenerPlayer().getPlayerSelectionStatus().remove(player.getName());
                this.plugin.getLog().sendPlayerNormal(player, "Selection mode is OFF");
            }
            else {
                this.plugin.getListenerPlayer().getPlayerSelectionStatus().add(player.getName());
                this.plugin.getLog().sendPlayerNormal(player, "Selection mode is ON");
            }
        }
        else {
            if (!this.plugin.getListenerPlayer().getPlayerSelectionLeft().containsKey(player.getName()) || !this.plugin.getListenerPlayer().getPlayerSelectionRight().containsKey(player.getName())) {
                this.plugin.getLog().sendPlayerWarn(player, "Set both left and right points before modifying your selection through commands.");
                return;
            }
            if (args[1].equalsIgnoreCase("ex")) {
                this.plugin.getListenerPlayer().getPlayerSelectionLeft().get(player.getName()).setX(-1000000.0);
                this.plugin.getListenerPlayer().getPlayerSelectionRight().get(player.getName()).setX(1000000.0);
                this.executeListselection(args, player);
                return;
            }
            if (args[1].equalsIgnoreCase("ey")) {
                this.plugin.getListenerPlayer().getPlayerSelectionLeft().get(player.getName()).setY(0.0);
                this.plugin.getListenerPlayer().getPlayerSelectionRight().get(player.getName()).setY(254.0);
                this.executeListselection(args, player);
                return;
            }
            if (args[1].equalsIgnoreCase("ez")) {
                this.plugin.getListenerPlayer().getPlayerSelectionLeft().get(player.getName()).setZ(-1000000.0);
                this.plugin.getListenerPlayer().getPlayerSelectionRight().get(player.getName()).setZ(1000000.0);
                this.executeListselection(args, player);
                return;
            }
            this.printUse(player, RootCommand.select);
        }
    }
    
    private void executeEdit(final String[] args, final Player player) {
        if (this.plugin.getListenerPlayer().getPlayerEditStatus().contains(player.getName())) {
            this.plugin.getListenerPlayer().getPlayerEditStatus().remove(player.getName());
            this.plugin.getLog().sendPlayerNormal(player, "Edit mode is OFF");
        }
        else {
            this.plugin.getListenerPlayer().getPlayerEditStatus().add(player.getName());
            this.plugin.getLog().sendPlayerNormal(player, "Edit mode is ON");
        }
    }
    
    private void executeListselection(final String[] args, final Player player) {
        String right = "Nothing is selected";
        String left = "Nothing is selected";
        if (this.plugin.getListenerPlayer().getPlayerSelectionLeft().containsKey(player.getName())) {
            left = String.valueOf(this.plugin.getListenerPlayer().getPlayerSelectionLeft().get(player.getName()).getBlockX()) + " " + this.plugin.getListenerPlayer().getPlayerSelectionLeft().get(player.getName()).getBlockY() + " " + this.plugin.getListenerPlayer().getPlayerSelectionLeft().get(player.getName()).getBlockZ();
        }
        if (this.plugin.getListenerPlayer().getPlayerSelectionRight().containsKey(player.getName())) {
            right = String.valueOf(this.plugin.getListenerPlayer().getPlayerSelectionRight().get(player.getName()).getBlockX()) + " " + this.plugin.getListenerPlayer().getPlayerSelectionRight().get(player.getName()).getBlockY() + " " + this.plugin.getListenerPlayer().getPlayerSelectionRight().get(player.getName()).getBlockZ();
        }
        this.plugin.getLog().sendPlayerNormal(player, "Right: " + right + " Left: " + left);
    }
    
    private void executeBlacklist(final String[] args, final Player player) {
        if (args.length < 2) {
            this.plugin.getLog().sendPlayerNormal(player, new StringBuilder().append(this.plugin.getConfiguration().listBlacklistBlockId()).toString());
            return;
        }
        if (args.length < 3) {
            this.printUse(player, RootCommand.blacklist);
            return;
        }
        if (args[1].equalsIgnoreCase("add")) {
            for (int i = 2; i < args.length; ++i) {
                this.plugin.getConfiguration().addBlacklistBlockId(Integer.parseInt(args[i]));
            }
        }
        else {
            if (!args[1].equalsIgnoreCase("remove")) {
                this.printUse(player, RootCommand.blacklist);
                return;
            }
            for (int i = 2; i < args.length; ++i) {
                this.plugin.getConfiguration().removeBlacklistBlockId(Integer.parseInt(args[i]));
            }
        }
        this.plugin.getLog().sendPlayerNormal(player, "Blacklist updated. " + this.plugin.getConfiguration().listBlacklistBlockId());
    }
    
    private void executeReload(final String[] args, final Player player) {
        this.plugin.getLog().sendPlayerNormal(player, "RegenBlock is reloading settings.");
        this.plugin.getConfiguration().reload();
        this.plugin.getLog().sendPlayerNormal(player, "Done.");
    }
    
    private void executeTest(final String[] args, final Player player) {
    }
    
    private void executeMonitor(final String[] args, final Player player) {
        if (args.length <= 2) {
            this.plugin.getLog().sendPlayerNormal(player, "Monitor place - " + this.plugin.getConfiguration().doMonitorPlace() + ", break - " + this.plugin.getConfiguration().doMonitorBreak());
        }
        else if (args[1].equalsIgnoreCase("break")) {
            if (args[2].equalsIgnoreCase("false")) {
                this.plugin.getConfiguration().setMonitorBreak(false);
            }
            else {
                this.plugin.getConfiguration().setMonitorBreak(true);
            }
        }
        else if (args[1].equalsIgnoreCase("place")) {
            if (args[2].equalsIgnoreCase("false")) {
                this.plugin.getConfiguration().setMonitorPlace(false);
            }
            else {
                this.plugin.getConfiguration().setMonitorPlace(true);
            }
        }
        else {
            this.plugin.getLog().sendPlayerNormal(player, "Monitor place - " + this.plugin.getConfiguration().doMonitorPlace() + ", break - " + this.plugin.getConfiguration().doMonitorBreak());
        }
    }
    
    private RootCommand getRootCommand(final String command) {
        RootCommand retval = null;
        try {
            retval = RootCommand.valueOf(command);
        }
        catch (IllegalArgumentException ex) {}
        return retval;
    }
    
    private void printUse(final Player player, final RootCommand command) {
        switch (command) {
            case blacklist: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb blacklist [add/remove] id id id id id.. - lists, adds, removes blacklisted block IDs for all regions.");
                break;
            }
            case create: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb create (name) [re-spawn time] - creates a new region at points selected with optional re-spawn time, default otherwise.");
                break;
            }
            case remove: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb remove (name) - removes region from the list.");
                break;
            }
            case modify: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb modify (name) [re-spawn time] - modify existing region's location and time.");
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb modify time (name) (re-spawn time) - modify existing region's re-spawn time only.");
                break;
            }
            case rblacklist: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb rblacklist (name) [add/remove] id id id id id.. - lists, adds, removes blacklisted block IDs for the region.");
                break;
            }
            case type: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb type (name) (type - 0,1)- changes the region's type. 0 - normal, 1 - regen up only, with randomization based on spawnblocks");
                break;
            }
            case sync: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb sync (name) (0/1/2/3)- changes the region's sync state. 0 - blocks repop separately, 1 - all at once based on first block broken, 2 - all at once based on last block broken, 3 - Same as 2, but preserving the order.");
                break;
            }
            case alarm: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb alarm time/message/radius (name) (value)- changes the region's alarm settings.");
                break;
            }
            case feedback: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb feedback (name) (feedback type [0,1,2])- changes the region's feedback type.");
                break;
            }
            case repop: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb repop (name) - Respawns all blocks in a given region.");
                break;
            }
            case spawnblock: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb spawnblock (name) - lists region's spawn blocks.");
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb spawnblock add (name) [id chance id chance...] - adds new blocks with spawn chance.");
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb spawnblock remove (name) [id id id...] - removes blocks.");
                break;
            }
            case select: {
                this.plugin.getLog().sendPlayerWarn(player, "Usage: /rb select [ex,ey,ez] - Toggles selection mode for the player. If ex/ey/ez is specified selection will be expanded in that direction. Y is vertical.");
                break;
            }
        }
    }
    
    private enum RootCommand
    {
        monitor("monitor", 0), 
        test("test", 1), 
        reload("reload", 2), 
        blacklist("blacklist", 3), 
        listselection("listselection", 4), 
        edit("edit", 5), 
        select("select", 6), 
        create("create", 7), 
        remove("remove", 8), 
        modify("modify", 9), 
        list("list", 10), 
        rblacklist("rblacklist", 11), 
        type("type", 12), 
        sync("sync", 13), 
        alarm("alarm", 14), 
        feedback("feedback", 15), 
        spawnblock("spawnblock", 16), 
        info("info", 17), 
        repop("repop", 18);
        
        private RootCommand(final String s, final int n) {
        }
    }
}
