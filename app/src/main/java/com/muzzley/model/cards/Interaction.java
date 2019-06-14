
package com.muzzley.model.cards;

import java.util.ArrayList;
import java.util.List;

public class Interaction {

    public List<Stage> stages = new ArrayList<Stage>();

    public volatile int currStage = 0;
    public volatile int destStage = 0;

}
