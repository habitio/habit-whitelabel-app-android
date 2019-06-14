
package com.muzzley.model.tiles;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Options {

    public String inputUnit;
    public String suffix;
    public String prefix;
    public String mathExpression;
    public String inputPath;
    public String format;
    @SerializedName("char")
    @Expose
    public String _char;

}
