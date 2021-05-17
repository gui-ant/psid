package grp07;

import java.sql.Timestamp;

public final class Alert {
    private final long id;
    private final long parameterSetId;
    private final long sensor_id;
    private final long param_id;
    private final Timestamp createdAt;
    private final String msg;

    public long getId() {
        return id;
    }

    public long getParameterSetId() {
        return parameterSetId;
    }

    public long getSensorId() {
        return sensor_id;
    }

    public long getParamId() {
        return param_id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getMsg() {
        return msg;
    }

    public Alert(long id, long parameterSetId, long sensor_id, long param_id, Timestamp createdAt, String msg) {
        this.id = id;
        this.parameterSetId = parameterSetId;
        this.sensor_id = sensor_id;
        this.param_id = param_id;
        this.createdAt = createdAt;
        this.msg = msg;
    }
}
