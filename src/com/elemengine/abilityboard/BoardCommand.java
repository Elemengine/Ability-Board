package com.elemengine.abilityboard;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elemengine.elemengine.command.SubCommand;
import com.elemengine.elemengine.storage.configuration.Config;
import com.elemengine.elemengine.user.PlayerUser;
import com.elemengine.elemengine.user.Users;

import net.md_5.bungee.api.ChatColor;

public class BoardCommand extends SubCommand {

    private AbilityBoards addon;
    
    public BoardCommand(AbilityBoards addon) {
        super("board", "Toggle the ability board on/off.", "/bending board", List.of());
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
        
        PlayerUser user = Users.manager().get(player).getAs(PlayerUser.class);
        
        if (args.length > 0) {
            sender.sendMessage(ChatColor.YELLOW + "Note: this command does not take any arguments.");
        }
        
        addon.getFrom(user).ifPresentOrElse(board -> {
            boolean on = board.toggle();
            sender.sendMessage(ChatColor.GOLD + "Your ability board has been toggled " + (on ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        }, () -> {
            sender.sendMessage(ChatColor.RED + "Unable to use that command right now.");
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }

}
