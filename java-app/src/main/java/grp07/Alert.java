package grp07;

import java.sql.Timestamp;

public final class Alert {
    private final long id;
    private final long parameterSetId;
    private final Timestamp createdAt;
    private final String msg;

    public long getId() {
        return id;
    }

    public long getParameterSetId() {
        return parameterSetId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getMsg() {
        return msg;
    }

    public Alert(long id, long parameterSetId, Timestamp createdAt, String msg) {
        this.id = id;
        this.parameterSetId = parameterSetId;
        this.createdAt = createdAt;
        this.msg = msg;
    }
}
