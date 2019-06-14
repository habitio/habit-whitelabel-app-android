package com.muzzley.model.channels;

import java.io.Serializable;
import java.util.List;

public class InterfaceComponents implements Serializable {
    public List<NativeComponents> nativeComponents;

    public InterfaceComponents(List<NativeComponents> components) {
        nativeComponents = components;
    }

    /*public List<NativeComponents> getNative() {
        return native;
    }*/

}
