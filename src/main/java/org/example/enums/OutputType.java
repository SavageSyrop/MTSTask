package org.example.enums;

import java.util.HashMap;
import java.util.Map;

public enum OutputType {
    FILE(1),       // в файл в формате .csv
    CONSOLE(2),    // в консоль в формате JSON
    COMBINED(3);    // одновременный вывод в файл и в консоль

    private final Integer value;

    private static final Map<Integer, OutputType> lookup = new HashMap<>();

    static {
        for (OutputType d : OutputType.values()) {
            lookup.put(d.getAbbreviation(), d);
        }
    }

    OutputType(Integer value) {
        this.value = value;
    }

    public Integer getAbbreviation() {
        return value;
    }

    public static OutputType get(Integer id) {
        return lookup.get(id);
    }
}
