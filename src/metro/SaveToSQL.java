package metro;

import metro.core.*;
import org.hibernate.Session;

import java.util.*;
import java.util.stream.Stream;

public class SaveToSQL {
    private final Session session;
    private final StationIndex stationIndex;

    public SaveToSQL(Session session, StationIndex stationIndex) {
        this.session = session;
        this.stationIndex = stationIndex;
    }

    public void saver() {
        saveLines();
        saveConnections();
    }

    private void saveLines() {
        Stream.of(stationIndex.getLines())
                .forEach(l -> {
                    for (Map.Entry<Integer, Line> k : l.entrySet()) {
                        Line line = k.getValue();

                        session.persist(line);
                    }
                });
    }

    private void saveConnections() {
        Map<Station, TreeSet<Station>> connections = stationIndex.getConnections();
        for (Map.Entry<Station, TreeSet<Station>> connect : connections.entrySet()) {
            String stFrom = connect.getKey().getName();
            int lineFrom = connect.getKey().getLine().getNumber();

            Station stationFrom = stationIndex.getLine(lineFrom).getStation(stFrom);

            Set<Station> stations = connect.getValue();
            stations.forEach(station -> {
                String stTo = station.getName();
                int lineTo = station.getLine().getNumber();

                Station stationTo = stationIndex.getLine(lineTo).getStation(stTo);

                Connection connection = new Connection(stationFrom,stationTo);

                session.persist(connection);
            });
        }
    }
}