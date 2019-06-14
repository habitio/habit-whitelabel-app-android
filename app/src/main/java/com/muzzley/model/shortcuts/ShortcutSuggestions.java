package com.muzzley.model.shortcuts;

import java.util.List;

/**
 * Created by kyryloryabin on 06/12/15.
 * Suggestions doesn't have id, actions, showinwatch. See documentation
 */
public class ShortcutSuggestions {

    private List<Shortcut> shortcutSuggestions;

    public ShortcutSuggestions(List<Shortcut> shortcutSuggestions) {
        this.shortcutSuggestions = shortcutSuggestions;
    }

    public List<Shortcut> getShortcuts() {
        return shortcutSuggestions;
    }

    public void setShortcuts(List<Shortcut> shortcutSuggestions) {
        this.shortcutSuggestions = shortcutSuggestions;
    }

}
