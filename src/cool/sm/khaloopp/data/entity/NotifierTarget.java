package cool.sm.khaloopp.data.entity;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public class NotifierTarget {

    public static enum State{
        New,
        Active,
        Blocked
    };

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected long id;
    @Temporal(TemporalType.TIMESTAMP)
    protected Date captured;
    protected State state;

    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public Date getCaptured() {
        return this.captured;
    }
    public void setCaptured(Date captured) {
        this.captured = captured;
    }
    public State getState() {
        return this.state;
    }
    public void setState(State state) {
        this.state = state;
    }
}