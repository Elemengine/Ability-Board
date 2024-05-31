package com.elemengine.abilityboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.elemengine.elemengine.Elemengine;
import com.elemengine.elemengine.addon.Addon;
import com.elemengine.elemengine.command.SubCommand;
import com.elemengine.elemengine.event.user.UserBindChangeEvent;
import com.elemengine.elemengine.event.user.UserCooldownEndEvent;
import com.elemengine.elemengine.event.user.UserCooldownStartEvent;
import com.elemengine.elemengine.event.user.UserCreationEvent;
import com.elemengine.elemengine.storage.configuration.Config;
import com.elemengine.elemengine.storage.configuration.Configure;
import com.elemengine.elemengine.user.PlayerUser;
import com.elemengine.elemengine.user.Users;

import net.md_5.bungee.api.ChatColor;

public class AbilityBoards extends Addon {
    
    //values used by the ability board
    @Configure String title = "&l&oBinds";
    @Configure("emptySlot.text") String emptySlot = "empty";
    @Configure("arrow.text") String arrow = "›";
    @Configure("arrow.addSpaceAfter") boolean spaceAfterArrow = true;
    ChatColor arrowIdleColor, arrowOverColor;
    
    //configuration that needs parsing
    @Configure("arrow.normalColor") private String ac = "#111111";
    @Configure("arrow.hoveredColor") private String ahc = "#ffffff";
    
    private final Map<PlayerUser, Board> cache = new HashMap<>();
    
    public AbilityBoards() {
        super("Ability Board", "1.0", "Simplicitee");
    }

    @Override
    public String getDescription() {
        return "Players get a scoreboard that shows their bound abilities.";
    }

    @Override
    protected void startup() {
        new BukkitRunnable() {
            
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerUser user = Users.manager().get(player).getAs(PlayerUser.class);
                    if (user != null) {
                        getFrom(user).ifPresent(Board::show);
                    }
                }
            }

        }.runTaskLater(JavaPlugin.getPlugin(Elemengine.class), 1);
    }

    @Override
    protected void cleanup() {
        cache.clear();
    }

    @Override
    protected Supplier<List<SubCommand>> commandRegistrator() {
        return () -> List.of(new BoardCommand(this));
    }

    @Override
    public void postProcessed(Config config) {
        arrowIdleColor = ChatColor.of(ac);
        arrowOverColor = ChatColor.of(ahc);
    }

    @EventHandler
    public void onUserCreated(UserCreationEvent event) {
        if (event.getUser() instanceof PlayerUser user) {
            new BukkitRunnable() {
    
                @Override
                public void run() {
                    getFrom(user).ifPresent(Board::show);
                }
    
            }.runTaskLater(JavaPlugin.getPlugin(Elemengine.class), 1);
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        PlayerUser user = Users.manager().get(event.getPlayer()).getAs(PlayerUser.class);
        if (user != null) {
            getFrom(user).ifPresent(b -> b.switchSlot(event.getNewSlot()));
        }
    }

    @EventHandler
    public void onCooldownStart(UserCooldownStartEvent event) {
        if (event.getUser() instanceof PlayerUser user) {
            getFrom(user).ifPresent(b -> b.cooldown(event.getTag(), true));
        }
    }

    @EventHandler
    public void onCooldownEnd(UserCooldownEndEvent event) {
        if (event.getUser() instanceof PlayerUser user) {
            getFrom(user).ifPresent(b -> b.cooldown(event.getCooldown().getTag(), false));
        }
    }

    @EventHandler
    public void onBindChange(UserBindChangeEvent event) {
        if (event.getUser() instanceof PlayerUser user) {
            getFrom(user).ifPresent(b -> b.updateBind(event.getSlot(), event.getResult()));
        }
    }
    
    public Optional<Board> getFrom(PlayerUser user) {
        //check for disabled worlds & whatnot here
        
        return Optional.of(cache.computeIfAbsent(user, k -> new Board(this, k)).update());
    }
}
