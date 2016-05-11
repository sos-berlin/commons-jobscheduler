package sos.scheduler.misc;

import java.util.Map;

import org.apache.commons.lang3.text.StrLookup;

public class CaseInsensitivLookupForParameter<V> extends StrLookup<V> {

    private final Map<String, V> map;

    CaseInsensitivLookupForParameter(final Map<String, V> map) {
        this.map = map;
    }

    @Override
    public String lookup(final String key) {
        String lowercaseKey = key.toLowerCase();
        if (map == null) {
            return null;
        }
        final Object obj = map.get(lowercaseKey);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}