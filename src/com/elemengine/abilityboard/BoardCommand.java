package com.elemengine.abilityboard;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elemengine.abilityboard.Board.Mode;
import com.elemengine.elemengine.command.SubCommand;
import com.elemengine.elemengine.command.TabComplete;
import com.elemengine.elemengine.storage.configuration.Config;
import com.elemengine.elemengine.storage.configuration.Configure;
import com.elemengine.elemengine.user.PlayerUser;
import com.elemengine.elemengine.user.Users;

import net.md_5.bungee.api.ChatColor;

public class BoardCommand extends SubCommand {
    
    @Configure String toggleMsg = "Your ability board has been toggled {toggle}.";
    @Configure String toggleOn = "ON";
    @Configure String toggleOff = "OFF";
    
    @Configure String modeMsg = "Your ability board has been set to {mode} mode.";

    private AbilityBoards addon;
    
    public BoardCommand(AbilityBoards addon) {
        super("board", "Toggle the ability board on/off.", "/bending board <toggle / mode>", List.of());
        this.addon = addon;
    }

    @Override
    public void postProcessed(Config config) {
        
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Player only command.");
            return;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.GOLD + "Try " + ChatColor.DARK_AQUA + "/bending board toggle" + ChatColor.GOLD + " or " + ChatColor.DARK_AQUA + "/bending board mode" + ChatColor.GOLD + ".");
            return;
        }
        
        PlayerUser user = Users.manager().get(player).getAs(PlayerUser.class);
        Consumer<Board> ifPresent = null;
        
        if (args[0].equalsIgnoreCase("toggle")) {
            ifPresent = (board) -> {
                boolean on = board.toggle();
                sender.sendMessage(ChatColor.GOLD + toggleMsg.replace("{toggle}", on ? ChatColor.GREEN + toggleOn : ChatColor.RED + toggleOff));
            };
        } else if (args[0].equalsIgnoreCase("mode")) {
            ifPresent = (board) -> {
                Mode mode = board.toggleMode();
                sender.sendMessage(ChatColor.GOLD + modeMsg.replace("{mode}", ChatColor.DARK_AQUA + mode.toString()));
            };
        } else if (args[0].equalsIgnoreCase("icon")) {
            String text = "Placeholder";
            
            if (args.length > 1) {
                text = args[1];
            }
            
            sender.sendMessage(IconUtil.translate(true, false, null, text));
        }
        
        if (ifPresent == null) {
            return;
        }
        
        addon.getFrom(user).ifPresentOrElse(ifPresent, () -> {
            sender.sendMessage(ChatColor.RED + "Unable to use that command right now.");
        });
    }

    @Override
    public TabComplete tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return new TabComplete().add("toggle").add("mode");
        }
        
        return TabComplete.ERROR;
    }

}
