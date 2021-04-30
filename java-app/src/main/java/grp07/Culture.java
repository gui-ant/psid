package grp07;

import java.util.ArrayList;
import java.util.List;

public class Culture {
    private final Long id;
    private String name;
    private Zone zone;
    private User manager;
    private boolean state;

    private List<CultureParams> parameters;

    public Culture(Long id) {
        this.id = id;
        this.parameters = new ArrayList<>();
    }

    public List<CultureParams> getParameters() {
        return parameters;
    }

    public void setParameters(List<CultureParams> parameters) {
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public boolean isEqual(Culture cul) {
        if (id == cul.getId() && name.equals(cul.getName()) && zone.isEqual(cul.getZone()) && manager.isEqual(cul.getManager()) && state == cul.isState()) {
            return true;
        }
        else {
            return false;
        }
    }
}
