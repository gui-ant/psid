package grp07;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SqlSender {
    private final Connection connCloud;
    private final Connection connLocal;

    private final Hashtable<Long, User> users = new Hashtable<>();
    private final Hashtable<String, Zone> zones = new Hashtable<>();
    private final Hashtable<String, Sensor> sensors = new Hashtable<>();
    private final Hashtable<Long, Culture> cultures = new Hashtable<>();// Todas as culturas, com as respetivas parametrizações associadas
    private final Hashtable<Long, List<CultureParams>> cultureParamsSet = new Hashtable<>(); // Sets de paramatrizações com culturas associadas

    public SqlSender(Connection connCloud, Connection connLocal) {

        this.connCloud = connCloud;
        this.connLocal = connLocal;
        fetchUsers();
        fetchZones();
        fetchSensors();
        fetchCultures();
        fetchCultureParams();
    }

    private void fetchUsers() {
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

    public Hashtable<Long, List<CultureParams>> getCultureParamsSet() {
        return cultureParamsSet;
    }

    public Hashtable<String, Sensor> getSensors() {
        return sensors;
    }

    public Hashtable<String, Zone> getZones() {
        return zones;
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

    private void fetchCultures() {
        String query = "SELECT * FROM cultures " +
                "JOIN users on cultures.manager_id=users.id ";
        try (Statement st = connLocal.createStatement()) {
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
        try (Statement st = connCloud.createStatement()) {
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
        try (Statement st = connCloud.createStatement()) {
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

    public synchronized void send(Connection connection, Measurement measurement, boolean isValid) {

        // buscar dados e extrair valores

        System.out.println("To insert: " + measurement);
        try {
            String id = measurement.getId().toString();
            Zone zone = zones.get(measurement.getZone());
            Sensor sensor = sensors.get(measurement.getSensor());
            String value = measurement.getMeasure();
            //Timestamp date = measurement.getTimestamp();
            Timestamp date = new Timestamp(System.currentTimeMillis());


            //enviar para SQL
            String sql = "INSERT INTO measurements (id, value, sensor_id, zone_id, date, is_correct) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, value);
            statement.setInt(3, zone.getId());
            statement.setInt(4, sensor.getId());
            statement.setTimestamp(5, date);
            statement.setBoolean(6, isValid);


            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("Inserted value successfully!!!");
            }

            statement.close();


        } catch (Exception e) {
            System.out.println("Connection failed!!!");
            e.printStackTrace();
        }
    }
}
