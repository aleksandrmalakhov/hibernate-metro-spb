package metro.core;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "connections")
@NoArgsConstructor
@Getter
@Setter
public class Connection {

    @EmbeddedId
    private ConnectionKey id;

    @ManyToOne(cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.REFRESH})
    @JoinColumn(name = "station_from", insertable = false, updatable = false)
    private Station stationFrom;

    @ManyToOne(cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.REFRESH})
    @JoinColumn(name = "station_to", insertable = false, updatable = false)
    private Station stationTo;

    public Connection(@NonNull Station stationFrom, @NonNull Station stationTo) {
        this.id = new ConnectionKey(stationFrom.getId(), stationTo.getId());
        this.stationFrom = stationFrom;
        this.stationTo = stationTo;
    }

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Setter
    private static class ConnectionKey implements Serializable {

        @Column(name = "station_from")
        private Integer stationFrom;

        @Column(name = "station_to")
        private Integer stationTo;
    }
}