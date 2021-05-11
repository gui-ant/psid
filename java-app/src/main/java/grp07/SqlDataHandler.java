package grp07;

import common.SqlConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public final class SqlDataHandler {
    private static final String MYSQL_CLOUD_URI = "";
    private static final String MYSQL_CLOUD_USER = "";
    private static final String MYSQL_CLOUD_PASS = "";
    private static final String MYSQL_LOCAL_URI = "";
    private static final String MYSQL_LOCAL_USER = "";
    private static final String MYSQL_LOCAL_PASS = "";

    private final Hashtable<Long, User> users = new Hashtable<>();
    private final Hashtable<String, Zone> zones = new Hashtable<>();
    private final Hashtable<String, Sensor> sensors = new Hashtable<>();
    private final Hashtable<Long, Culture> cultures = new Hashtable<>();// Todas as culturas, com as respetivas parametrizações associadas
    private final Hashtable<Long, List<CultureParams>> cultureParamsSet = new Hashtable<>(); // Sets de paramatrizações com culturas associadas

    public SqlConnector getConnCloud() {
        return connCloud;
    }

    public SqlConnector getConnLocal() {
        return connLocal;
    }

    private SqlConnector connCloud = null;
    private SqlConnector connLocal = null;

    public SqlDataHandler() {
        try {
            this.connCloud = new SqlConnector(MYSQL_CLOUD_URI, MYSQL_CLOUD_USER, MYSQL_CLOUD_PASS);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            this.connLocal = new SqlConnector(MYSQL_LOCAL_URI, MYSQL_LOCAL_USER, MYSQL_LOCAL_PASS);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        fetchUsers();
        fetchZones();
        fetchSensors();
        fetchCultures();
        fetchCultureParams();
    }

    public Hashtable<Long, List<CultureParams>> getCultureParamsSet() {
        return cultureParamsSet;
    }

    public Hashtable<String, Sensor> getSensors() {
        return sensors;
    }

    public Hashtable<String, Zone> getZones() {
        return zones;
    }

    private void fetchUsers() {
        String query = "SELECT * FROM users";
        try (Statement st = connLocal.getConnection().createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                User u = new User(res.getInt("id"));
                u.setEmail(res.getString("email"));
                u.setName(res.getString("username"));

                users.put(u.getId(), u);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    private void fetchCultureParams() {
        cultures.forEach((id, culture) -> {
            // Devolve parametrizações por id de cultura
            String query = "SELECT sets.id as id, sensor_type, valmax, valmin, tolerance " +
                    "FROM rel_culture_params_set AS rel " +
                    "JOIN culture_params AS params ON params.id = rel.culture_param_id " +
                    "JOIN culture_params_sets AS sets " +
                    "ON sets.id = rel.set_id " +
                    "WHERE sets.culture_id = " + id;

            try (Statement st = connLocal.getConnection().createStatement()) {
                ResultSet res = st.executeQuery(query);
                if (!res.next()) {
                    // No parameters
                } else {
                    if (!cultureParamsSet.containsKey(res.getInt("id")))
                        cultureParamsSet.put(res.getLong("id"), new ArrayList<>());

                    List<CultureParams> cultureParams = cultureParamsSet.get(res.getLong("id"));
                    do {
                        CultureParams params = new CultureParams();
                        params.setSensorType(res.getString("sensor_type"));
                        params.setValMax(res.getDouble("valmax"));
                        params.setValMin(res.getDouble("valmin"));
                        params.setTolerance(res.getInt("tolerance"));
                        params.setCulture(culture);
                        cultureParams.add(params);
                        culture.setParameters(cultureParams);
                    } while (res.next());
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    private void fetchCultures() {
        String query = "SELECT * FROM cultures " +
                "JOIN users on cultures.manager_id=users.id ";
        try (Statement st = connLocal.getConnection().createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Culture c = new Culture(res.getLong("id"));
                c.setName(res.getString("name"));
                c.setState(res.getBoolean("state"));
                c.setManager(users.get(res.getLong("manager_id")));
                c.setZone(zones.get(res.getInt("zone_id")));
                cultures.put(c.getId(), c);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void fetchZones() {
        String query = "SELECT * FROM zones";
        try (Statement st = connCloud.getConnection().createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Zone z = new Zone(res.getInt("id"));
                zones.put(res.getString("name"), z);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void fetchSensors() {
        String query = "SELECT s.*, z.name FROM sensors as s JOIN zones as z on s.zone_id = z.id";
        try (Statement st = connCloud.getConnection().createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Sensor s = new Sensor(res.getInt("id"));
                Zone z = zones.get(res.getString("z.name"));

                s.setMinLim(res.getInt("minlim"));
                s.setMaxLim(res.getInt("maxlim"));
                s.setZone(z);

                sensors.put(res.getString("name"), s);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


}
