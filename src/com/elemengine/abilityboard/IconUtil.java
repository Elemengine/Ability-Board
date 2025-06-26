package com.elemengine.abilityboard;

import java.util.Map;

import com.elemengine.elemengine.ability.AbilityInfo;
import com.elemengine.elemengine.element.Element;
import com.elemengine.elemengine.element.relation.ElementRelation;
import com.elemengine.elemengine.element.relation.SingleRelation;

import net.md_5.bungee.api.ChatColor;

public final class IconUtil {
    
    public static final char BACKGROUND_LIT = '\uE700';
    public static final char BACKGROUND_DIM = '\uE701';
    
    private static final int BACKGROUND_WIDTH = 96;
    private static final char[] NEGATIVES = {'\uE800', '\uE801', '\uE802', '\uE803', '\uE804'};
    
    private static final Map<Character, Character> ALPHABET = Map.ofEntries(
            Map.entry('A', '\uE600'), Map.entry('B', '\uE601'), Map.entry('C', '\uE602'), Map.entry('D', '\uE603'), Map.entry('E', '\uE604'), 
            Map.entry('F', '\uE605'), Map.entry('G', '\uE606'), Map.entry('H', '\uE607'), Map.entry('I', '\uE608'), Map.entry('J', '\uE609'),
            Map.entry('K', '\uE610'), Map.entry('L', '\uE611'), Map.entry('M', '\uE612'), Map.entry('N', '\uE613'), Map.entry('O', '\uE614'),
            Map.entry('P', '\uE615'), Map.entry('Q', '\uE616'), Map.entry('R', '\uE617'), Map.entry('S', '\uE618'), Map.entry('T', '\uE619'),
            Map.entry('U', '\uE620'), Map.entry('V', '\uE621'), Map.entry('W', '\uE622'), Map.entry('X', '\uE623'), Map.entry('Y', '\uE624'),
            Map.entry('Z', '\uE625')
    );

    private IconUtil() {}
    
    public static StringBuilder alphabetize(String text) {
        text = text.toUpperCase();
        StringBuilder output = new StringBuilder();
        for (char c : text.toCharArray()) {
            output.append(ALPHABET.get(c));
        }
        
        return output;
    }
    
    public static StringBuilder generateNegatives(int space) {
        int i = NEGATIVES.length - 1;
        int value = (int) Math.pow(2, i);
        StringBuilder output = new StringBuilder();
        
        while (i >= 0) {
            if (space >= value) {
                output.append(NEGATIVES[i]);
                space -= value;
                continue;
            }
            
            value = (int) Math.pow(2, --i);
        }
        
        return output;
    }
    
    public static String translate(boolean lit, boolean strike, AbilityInfo info) {
        if (info == null) {
            return translate(lit, strike, null, "EMPTY");
        }
        
        return translate(lit, strike, info.getElementRelation(), info.getName());
    }
    
    public static String translate(boolean lit, boolean strike, ElementRelation relation, String text) {
        StringBuilder translated = new StringBuilder();
        
        if (relation == null) {
            translated.append(ChatColor.GRAY);
        } else if (relation instanceof SingleRelation single) {
            translated.append(single.element().getChatColor());
        } else {
            translated.append(Element.ENERGY.getChatColor());
        }
        
        translated.append(lit ? BACKGROUND_LIT : BACKGROUND_DIM);
        translated.append(generateNegatives(BACKGROUND_WIDTH / 2 + text.length() * 7 / 2));
        translated.append(lit ? ChatColor.WHITE : ChatColor.GRAY);
        if (strike) {
            translated.append(ChatColor.STRIKETHROUGH);
        }
        translated.append(alphabetize(text));
        
        return translated.toString();
    }
}
