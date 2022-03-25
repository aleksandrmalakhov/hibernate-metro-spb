package metro.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "metro_stations")
@AllArgsConstructor
@Getter
@Setter
public class Station implements Comparable<Station> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @Column(name = "number")
    private int number;

    @Column(name = "name")
    private String name;

    @ManyToOne(cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.REFRESH})
    @JoinColumn(name = "line_id")
    private Line line;

    @OneToMany(cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.REFRESH},
            mappedBy = "stationFrom")
    private List<Connection> connections;

    public Station() {
        connections = new ArrayList<>();
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    @Override
    public int compareTo(@NonNull Station station) {
        int lineComparison = line.compareTo(station.getLine());
        if (lineComparison != 0) {
            return lineComparison;
        }
        return name.compareToIgnoreCase(station.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo((Station) obj) == 0;
    }

    @Override
    public String toString() {
        return name;
    }
}