package metro.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "metro_lines")
@NoArgsConstructor
@Getter
@Setter
public class Line implements Comparable<Line> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @Column(name = "number")
    private int number;

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;

    @OneToMany(cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.REFRESH},
            mappedBy = "line")
    private List<Station> stations;

    public void addStation(Station station) {
        if (stations == null) {
            stations = new ArrayList<>();
        }
        station.setLine(this);
        stations.add(station);
    }

    public Station getStation(String nameStation) {
        for (Station station : stations) {
            if (station.getName().equalsIgnoreCase(nameStation)) {
                return station;
            }
        }
        return null;
    }

    @Override
    public int compareTo(@NonNull Line line) {
        return Integer.compare(number, line.getNumber());
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo((Line) obj) == 0;
    }
}