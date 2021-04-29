package grp07;

import java.sql.Timestamp;

public class Alert {
    private long id;
    private long parameterSetId;
    private Timestamp createdAt;
    private String msg;

    public Alert(long id, long parameterSetId, Timestamp createdAt, String msg) {
        this.id = id;
        this.parameterSetId = parameterSetId;
        this.createdAt = createdAt;
        this.msg = msg;
    }
}
