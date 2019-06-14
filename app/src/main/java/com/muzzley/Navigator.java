package com.muzzley;

import android.content.Intent;

public interface Navigator {
    Intent gotoInterface();
    Intent newGetStartedIntent(int flags);
    Intent newHomeIntent(int flags);
    Intent newTilesWithRefresh();
}
