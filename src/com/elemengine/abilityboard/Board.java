package com.elemengine.abilityboard;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.elemengine.elemengine.ability.Abilities;
import com.elemengine.elemengine.ability.AbilityInfo;
import com.elemengine.elemengine.ability.type.Bindable;
import com.elemengine.elemengine.ability.util.Cooldown;
import com.elemengine.elemengine.user.PlayerUser;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class Board {
    
    private final AbilityBoards provider;
    
    private final BoardSlot[] slots = new BoardSlot[9];
    private final PlayerUser user;
    private int oldSlot;
    private final Scoreboard board;

    Board(AbilityBoards provider, PlayerUser user) {
        this.provider = provider;
        this.user = user;
        this.oldSlot = user.getCurrentSlot();
        this.board = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective obj = board.registerNewObjective("ability_board", Criteria.DUMMY, ChatColor.translateAlternateColorCodes('&', provider.title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < slots.length; ++i) {
            slots[i] = new BoardSlot(board, obj, i).update("" + (oldSlot == i ? provider.arrowOverColor : provider.arrowIdleColor), provider.emptySlot);
        }
    }

    public void hide() {
        user.getEntity().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void show() {
        user.getEntity().setScoreboard(board);
        this.update();
    }

    /**
     * Toggles visibility of the scoreboard
     * 
     * @return true if visible after toggle
     */
    public boolean toggle() {
        if (user.getEntity().getScoreboard().equals(this.board)) {
            this.hide();
            return false;
        }

        this.show();
        return true;
    }

    public Board update() {
        int i = 0;
        for (AbilityInfo bind : user.getBinds()) {
            this.updateBind(i, bind);
            ++i;
        }

        /*
         * ++i; //skip over slot 10, never needs update
         * 
         * Iterator<Cooldown.Tag> iter = misc.iterator(); while (i < 15) { if
         * (iter.hasNext()) { Cooldown.Tag tag = iter.next();
         * slots[i].update(ChatColor.DARK_GRAY + "- ", tag.getColor() +
         * (ChatColor.STRIKETHROUGH + tag.getDisplay())); } else {
         * slots[i].update(ChatColor.DARK_GRAY + "- ", ChatColor.DARK_GRAY + "empty"); }
         * ++i; }
         */

        return this;
    }

    public void updateBind(int slot, AbilityInfo ability) {
        ChatColor color = slot == oldSlot ? provider.arrowOverColor : provider.arrowIdleColor;
        slots[slot].update(color + provider.arrow + (provider.spaceAfterArrow ? " " : ""), ability == null ? color + provider.emptySlot : ability.createComponent().toLegacyText());
        if (ability != null && user.hasCooldown(ability)) {
            bindCooldown(slot, true);
        }
    }

    public void switchSlot(int newSlot) {
        int slot = oldSlot;
        oldSlot = newSlot;
        this.updateBind(slot, user.getBoundAbility(slot).orElse(null));
        this.updateBind(newSlot, user.getBoundAbility(newSlot).orElse(null));
    }

    public void cooldown(Cooldown.Tag tag, boolean added) {
        Optional<AbilityInfo> ability = Abilities.manager().getInfo(tag.getInternal());

        if (ability.isPresent()) {
            if (ability.get() instanceof Bindable) {
                for (int slot : user.getBinds().slotsOf(ability.get())) {
                    bindCooldown(slot, added);
                }
                return;
            }
        }
    }

    public void bindCooldown(int slot, boolean added) {
        user.getBinds().get(slot).ifPresent(ability -> {
            BaseComponent info = ability.createComponent();
            if (added) {
                info.setStrikethrough(true);
            }
            slots[slot].team.setSuffix(info.toLegacyText());
        });
    }

    private static class BoardSlot {

        private final Objective obj;
        private final int slot;
        private final Team team;
        private final String entry;

        @SuppressWarnings("deprecation")
        public BoardSlot(Scoreboard board, Objective obj, int slot) {
            this.obj = obj;
            this.slot = slot + 1;
            this.team = board.registerNewTeam("slot" + slot);
            this.entry = ChatColor.values()[slot % 10] + "" + ChatColor.values()[slot % 16];

            team.addEntry(entry);
        }

        public BoardSlot update(String prefix, String name) {
            team.setPrefix(prefix);
            team.setSuffix(name);
            obj.getScore(entry).setScore(-slot);
            return this;
        }
    }
}