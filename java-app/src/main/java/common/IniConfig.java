package common;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;

import java.io.File;
import java.io.IOException;

public class IniConfig {
    private Ini ini = null;
    private IniPreferences prefs;
    private Ini.Section section = null;

    public IniConfig(String iniFile) {
        try {
            ini = new Ini(new File(iniFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("File " + iniFile + " not found.");
        }
        this.prefs = new IniPreferences(ini);
    }

    public Ini getIni() {
        return this.ini;
    }

    public void useIniSection(String name) {
        this.section = ini.get(name);
    }

    public String getConfig(String key) {
        if (section != null)
            return section.get(key);
        else
            return getConfig(key, null, null);
    }

    public String getConfig(String node, String key) {
        return getConfig(node, key, null);
    }

    public String getConfig(String node, String key, String def) {
        return prefs.node(node).get(key, def);
    }


}