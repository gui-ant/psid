package grp07;

import common.IniConfig;

import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public final class MySqlData extends IniConfig {
    private static final String MYSQL_LOCAL_USER = "root";
    private static final String MYSQL_LOCAL_PASS = "";

    private static final String MYSQL_CLOUD_USER = "aluno";
    private static final String MYSQL_CLOUD_PASS = "aluno";

    private final Hashtable<Long, User> users = new Hashtable<>();
    private final Hashtable<Long, Zone> zones = new Hashtable<>();
    private final Hashtable<Long, Sensor> sensors = new Hashtable<>();
    private final Hashtable<Long, Culture> cultures = new Hashtable<>();// Todas as culturas, com as respetivas parametrizações associadas
    private final Hashtable<Long, CultureParams> cultureParams = new Hashtable<>(); // todas as paramatrizações com culturas associadas (sem ser em sets)

    private final Hashtable<Long, List<CultureParams>> cultureParamSet = new Hashtable<>(); // Sets de todas as paramatrizações com culturas associadas

    public MySqlData(String iniFile) {
        super(iniFile);
        String mysqlCloudUri = getConfig("mysql", "cloud_uri");
        String mysqlLocalUri = getConfig("mysql", "local_uri");
        try {
            Connection connCloud = DriverManager.getConnection(mysqlCloudUri, MYSQL_CLOUD_USER, MYSQL_CLOUD_PASS);
            Connection connLocal = DriverManager.getConnection(mysqlLocalUri, MYSQL_LOCAL_USER, MYSQL_LOCAL_PASS);

            fetchZones(connCloud);
            fetchSensors(connCloud);

            fetchUsers(connLocal);
            fetchCultures(connLocal);
            fetchCultureParams(connLocal);
            setParamSet();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Sensor getSensorByName(String name) {
        for (Map.Entry<Long, Sensor> s : getSensors().entrySet()) {
            if (s.getValue().getSensorName().equals(name))
                return s.getValue();
        }
        return null;
    }

    public Zone getZoneByName(String name) {
        for (Map.Entry<Long, Zone> z : getZones().entrySet()) {
            if (z.getValue().getName().equals(name))
                return z.getValue();
        }
        return null;
    }

    public Hashtable<Long, List<CultureParams>> getCultureParamsSet() {
        return cultureParamSet;
    }

    private void setParamSet() {
        for (CultureParams p : cultureParams.values()) {
            Long setId = p.getSetId();
            List<CultureParams> list = cultureParamSet.get(setId);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(p);
            cultureParamSet.put(setId, list);
        }
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
    }


    private void fetchCultureParams(Connection connLocal) {
        cultures.forEach((id, culture) -> {
            // Devolve parametrizações por id de cultura
            String query = "SELECT params.id as param_id, sets.id as set_id, sets.culture_id, sensor_type, valmax, valmin, tolerance " +
                    "FROM rel_culture_params_set AS rel " +
                    "JOIN culture_params AS params ON params.id = rel.culture_param_id " +
                    "JOIN culture_params_sets AS sets " +
                    "ON sets.id = rel.set_id " +
                    "WHERE sets.culture_id = " + id;

            Hashtable<Long, List<CultureParams>> cultureParamsSets = new Hashtable<>();


            try (Statement st = connLocal.createStatement()) {
                ResultSet res = st.executeQuery(query);
                if (res.next()) {


                    do {
                        if (!cultureParamsSets.containsKey(res.getLong("set_id")))
                            cultureParamsSets.put(res.getLong("set_id"), new ArrayList<>());

                        CultureParams p = new CultureParams();
                        p.setParamId((long) res.getInt("param_id"));
                        p.setSensorType(res.getString("sensor_type"));
                        p.setValMax(res.getDouble("valmax"));
                        p.setValMin(res.getDouble("valmin"));
                        p.setTolerance(res.getInt("tolerance"));
                        p.setSetId(res.getLong("set_id"));
                        p.setCulture(culture);

                        cultureParams.put(res.getLong("param_id"), p);
                        cultureParamsSets.get(p.getSetId()).add(p);

                    } while (res.next());
                    culture.setParameters(cultureParamsSets);

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
    }

    private void fetchZones(Connection connCloud) {
        String query = "SELECT * FROM zones";
        try (Statement st = connCloud.createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Zone z = new Zone(res.getInt("id"));
                z.setName(res.getString("name"));
                z.setHumidity(res.getDouble("humidity"));
                z.setLight(res.getDouble("light"));
                z.setTemperature(res.getDouble("temperature"));
                zones.put(res.getLong("id"), z);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void fetchSensors(Connection connCloud) {
        String query = "SELECT s.*, z.id FROM sensors as s JOIN zones as z on s.zone_id = z.id";
        try (Statement st = connCloud.createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Sensor s = new Sensor(res.getInt("s.id"));
                Zone z = zones.get(res.getLong("z.id"));

                s.setSensorName(res.getString("name"));
                s.setMinLim(res.getDouble("minlim"));
                s.setMaxLim(res.getDouble("maxlim"));
                s.setZone(z);
                z.getSensors().add(s);
                sensors.put(res.getLong("s.id"), s);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public static final class Sensor {
        private final int id;
        private Zone zone;
        private String sensorName;
        private double minLim;
        private double maxLim;

        public Sensor(int id) {
            this.id = id;
        }

        public String getSensorName() {
            return sensorName;
        }

        public void setSensorName(String name) {
            this.sensorName = name;
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
            return id == u.getId() && email.equals(u.getEmail()) && name.equals(u.getName()) && role == u.getRole();
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

        public void setName(String name) {
            this.name = name;
        }

        private String name;

        private final ArrayList<Sensor> sensors = new ArrayList<>();

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

        public ArrayList<Sensor> getSensors() {
            return sensors;
        }

        public String getName() {
            return this.name;
        }
    }

    public static final class CultureParams {
        private Long paramId;
        private String sensorType;
        private double valMax;
        private double valMin;
        private int tolerance;
        private Culture culture;
        private Long setId;

        public void setSetId(Long setId) {
            this.setId = setId;
        }

        public Long getSetId() {
            return this.setId;
        }

        public void setParamId(Long paramId) {
            this.paramId = paramId;
        }

        public Long getParamId() {
            return paramId;
        }

        public CultureParams() {
        }

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

        public Hashtable<Long, List<CultureParams>> getParameters() {
            return parameters;
        }

        private Hashtable<Long, List<CultureParams>> parameters;

        public Culture(Long id) {
            this.id = id;
            this.parameters = new Hashtable<>();
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
            return id.equals(cul.getId()) && name.equals(cul.getName()) && zone.isEqual(cul.getZone()) && manager.isEqual(cul.getManager()) && state == cul.isState();
        }

        public void setParameters(Hashtable<Long, List<CultureParams>> cultureParamsSets) {
            this.parameters = cultureParamsSets;
        }
    }


    private void testUsers() {
        for (Long k : users.keySet()) {
            User u = users.get(k);
            System.err.println("User id " + u.getId());
            System.err.println("User name " + u.getName());
            System.err.println("User mail " + u.getEmail());
            System.err.println("User  role" + u.getRole());
            System.err.println("----------------------------");
        }
    }

    private void testZones() {
        for (Long k : zones.keySet()) {
            Zone z = zones.get(k);
            System.err.println("zona id: " + z.getId());
            System.err.println("zona humidity: " + z.getHumidity());
            System.err.println("zona light: " + z.getLight());
            System.err.println("zona temperature: " + z.getTemperature());
        }
    }

    private void testSensors() {
        for (Long k : sensors.keySet()) {
            Sensor s = sensors.get(k);
            System.err.println("sensor: id " + s.getId());
            System.err.println("sensor: name " + s.getSensorName());
            System.err.println("sensor: zone " + s.getZone().getName());
            System.err.println("sensor: min " + s.getMinLim());
            System.err.println("sensor: max " + s.getMaxLim());
            System.err.println("----------------------");
        }
    }

    private void testCultures() {
        for (Long k : cultures.keySet()) {
            Culture c = cultures.get(k);
            System.err.println("Cultura: id - " + c.getId());
            System.err.println("Cultura: name - " + c.getName());
            System.err.println("Cultura: zone - " + c.getZone());
            System.err.println("Cultura: parametros - " + c.getParameters());
            System.err.println("Cultura: manager - " + c.getManager());
            System.err.println("Cultura: estado - " + c.isState());
            System.err.println("----------------------------------------");
        }
    }

    private void testCultureParams() {
        for (Long k : getCultureParamsSet().keySet()) {
            List<CultureParams> lst = getCultureParamsSet().get(k);
            for(CultureParams p : lst) {
                System.err.println("Parametro: set_id ?- " + k);
                System.err.println("Parametro: set_id - " + p.getSetId());
                System.err.println("Parametro: id - " + p.getParamId());
                System.err.println("Parametro: tipo - " + p.getSensorType());
                System.err.println("Parametro: max - " + p.getValMax());
                System.err.println("Parametro: min - " + p.getValMin());
                System.err.println("Parametro: cultura - " + p.getCulture());
                System.err.println("Parametro: tolerance - " + p.getTolerance());
                System.err.println("----------------------------------------");
            }
        }
    }
}
