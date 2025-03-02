package romeplugin.zoning;

import romeplugin.newtitle.Title;
import static romeplugin.newtitle.Title.*;

import java.util.Arrays;

public enum ZoneType {
    GOVERNMENT(CONSOLE, MAYOR, BUILDER), CITY(MAYOR, BUILDER), SUBURB(), WILDERNESS();

    private final Title[] titles;
    ZoneType(Title... titles) {
        this.titles = titles;
    }

    public Title[] getTitles() {
        return titles;
    }

    public boolean canBuild(Title title) {
        if (this == WILDERNESS) return true;
        return Arrays.asList(this.titles).contains(title);
    }
}
