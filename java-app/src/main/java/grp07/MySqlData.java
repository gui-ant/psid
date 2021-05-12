package grp07;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public final class MySqlData {
    private static final String MYSQL_LOCAL_URI = "jdbc:mysql://localhost:3306/g07_local";
    private static final String MYSQL_LOCAL_USER = "root";
    private static final String MYSQL_LOCAL_PASS = "";

    private static final String MYSQL_CLOUD_URI = "jdbc:mysql://194.210.86.10:3306/aluno_g07_cloud";
    private static final String MYSQL_CLOUD_USER = "aluno";
    private static final String MYSQL_CLOUD_PASS = "aluno";

    private final Hashtable<Long, User> users = new Hashtable<>();
    private final Hashtable<Long, Zone> zones = new Hashtable<>();
    private final Hashtable<Long, Sensor> sensors = new Hashtable<>();
    private final Hashtable<Long, Culture> cultures = new Hashtable<>();// Todas as culturas, com as respetivas parametrizações associadas
    private final Hashtable<Long, List<CultureParams>> cultureParamsSet = new Hashtable<>(); // Sets de paramatrizações com culturas associadas

    public static MySqlData get() {
        return new MySqlData();
    }

    public MySqlData() {
        try {
            Connection connCloud = DriverManager.getConnection(MYSQL_CLOUD_URI, MYSQL_CLOUD_USER, MYSQL_CLOUD_PASS);
            Connection connLocal = DriverManager.getConnection(MYSQL_LOCAL_URI, MYSQL_LOCAL_USER, MYSQL_LOCAL_PASS);

            fetchZones(connCloud);
            fetchSensors(connCloud);

            fetchUsers(connLocal);
            fetchCultures(connLocal);
            fetchCultureParams(connLocal);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Hashtable<Long, List<CultureParams>> getCultureParamsSet() {
        return cultureParamsSet;
    }

    public Hashtable<Long, Sensor> getSensors() {
        return sensors;
    }

    public Hashtable<Long, Zone> getZones() {
        return zones;
    }

    private void fetchUsers(Connection connLocal) {
        String query = "SELECT * FROM users";
        try (Statement st = connLocal.createStatement()) {
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

//        for (Long k : users.keySet()) {
//            User u = users.get(k);
//            System.err.println("User id " + u.getId());
//            System.err.println("User name " + u.getName());
//            System.err.println("User mail " + u.getEmail());
//            System.err.println("User  role" + u.getRole());
//            System.err.println("----------------------------");
//        }
    }


    private void fetchCultureParams(Connection connLocal) {
        cultures.forEach((id, culture) -> {
            // Devolve parametrizações por id de cultura
            String query = "SELECT sets.id as id, sensor_type, valmax, valmin, tolerance " +
                    "FROM rel_culture_params_set AS rel " +
                    "JOIN culture_params AS params ON params.id = rel.culture_param_id " +
                    "JOIN culture_params_sets AS sets " +
                    "ON sets.id = rel.set_id " +
                    "WHERE sets.culture_id = " + id;

            try (Statement st = connLocal.createStatement()) {
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

    private void fetchCultures(Connection connLocal) {
        String query = "SELECT * FROM cultures " +
                "JOIN users on cultures.manager_id=users.id ";
        try (Statement st = connLocal.createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Culture c = new Culture(res.getLong("id"));
                c.setName(res.getString("name"));
                c.setState(res.getBoolean("state"));
                c.setManager(users.get(res.getLong("manager_id")));
                c.setZone(zones.get(res.getLong("zone_id")));
                cultures.put(c.getId(), c);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

//        for (Long k : cultures.keySet()) {
//            Culture c = cultures.get(k);
//        }

    }

    private void fetchZones(Connection connCloud) {
        String query = "SELECT * FROM zones";
        try (Statement st = connCloud.createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Zone z = new Zone(res.getInt("id"));
                zones.put(res.getLong("id"), z);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

//        for (Long k : zones.keySet()) {
//            Zone z = zones.get(k);
//            System.err.println("id: " + z.getId());
//            System.err.println("humidity: " + z.getHumidity());
//            System.err.println("light: " + z.getLight());
//            System.err.println("temperature: " + z.getTemperature());
//        }

    }

    private void fetchSensors(Connection connCloud) {
        String query = "SELECT s.*, z.id FROM sensors as s JOIN zones as z on s.zone_id = z.id";
        try (Statement st = connCloud.createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Sensor s = new Sensor(res.getInt("id"));
                Zone z = zones.get(res.getLong("z.id"));

                s.setMinLim(res.getDouble("minlim"));
                s.setMaxLim(res.getDouble("maxlim"));
                s.setZone(z);

                sensors.put(res.getLong("id"), s);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

//        for (Long k : sensors.keySet()) {
//            Sensor s = sensors.get(k);
//            System.err.println("sensor: id " + s.getId());
//            System.err.println("sensor: zone " + s.getZone());
//            System.err.println("sensor: min " + s.getMinLim());
//            System.err.println("sensor: max " + s.getMaxLim());
//        }

    }


    public static final class Sensor {
        private final int id;
        private Zone zone;
        private double minLim;
        private double maxLim;

        public Sensor(int id) {
            this.id = id;
        }

        public Zone getZone() {
            return zone;
        }

        public void setZone(Zone zone) {
            this.zone = zone;
        }

        public int getId() {
            return id;
        }

        public double getMinLim() {
            return minLim;
        }

        public void setMinLim(double minLim) {
            this.minLim = minLim;
        }

        public double getMaxLim() {
            return maxLim;
        }

        public void setMaxLim(double maxLim) {
            this.maxLim = maxLim;
        }

    }

    public static final class User {
        private final long id;
        private String email;
        private String name;
        private int role; // TODO

        public User(long id) {
            this.id = id;
        }


        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public int getRole() {
            return role;
        }

        public boolean isEqual(User u) {
            if (id == u.getId() && email.equals(u.getEmail()) && name.equals(u.getName()) && role == u.getRole()) {
                return true;
            } else {
                return false;
            }
        }


        // TODO - APENAS PARA TESTE, APAGAR!!!
        public void setRole(int r) {
            this.role = r;
        }
    }

    public static final class Zone {
        private final int id;
        private double temperature;
        private double humidity;
        private double light;

        public Zone(int id) {
            this.id = id;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }

        public double getLight() {
            return light;
        }

        public void setLight(double light) {
            this.light = light;
        }

        public int getId() {
            return this.id;
        }

        public boolean isEqual(Zone z) {
            return id == z.getId() && temperature == z.getTemperature() && humidity == z.getHumidity() && light == z.getLight();
        }

    }

    public static final class CultureParams {
        private String sensorType;
        private double valMax;
        private double valMin;
        private int tolerance;
        private Culture culture;

        public Culture getCulture() {
            return culture;
        }

        public void setCulture(Culture culture) {
            this.culture = culture;
        }

        public String getSensorType() {
            return sensorType;
        }

        public void setSensorType(String sensorType) {
            this.sensorType = sensorType;
        }

        public double getValMax() {
            return valMax;
        }

        public void setValMax(double valMax) {
            this.valMax = valMax;
        }

        public double getValMin() {
            return valMin;
        }

        public void setValMin(double valMin) {
            this.valMin = valMin;
        }

        public int getTolerance() {
            return tolerance;
        }

        public void setTolerance(int tolerance) {
            this.tolerance = tolerance;
        }

        public boolean isEqual(CultureParams param) {
            return
                    sensorType.equals(param.getSensorType()) &&
                            valMax == param.getValMax() &&
                            valMin == param.getValMin() &&
                            tolerance == param.getTolerance() &&
                            culture.isEqual(param.getCulture())
                    ;
        }
    }

    public static final class Culture {
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
            } else {
                return false;
            }
        }
    }
}
