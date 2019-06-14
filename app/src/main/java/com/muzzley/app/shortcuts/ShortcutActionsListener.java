package com.muzzley.app.shortcuts;

import com.muzzley.model.shortcuts.Shortcut;

/**
 * Created by caan on 06-12-2017.
 */
public interface ShortcutActionsListener {
    void onCreateNew();

    void onExecuteShortcut(Shortcut shortcut, ShortcutsVH viewHolder, int index);

    void onShowMore(ShortcutsVH viewHolder);
}
