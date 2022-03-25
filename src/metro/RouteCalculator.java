package metro;

import lombok.NonNull;
import metro.core.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RouteCalculator {
    private static final double INTER_STATION_DURATION = 2.5;
    private static final double INTER_CONNECTION_DURATION = 3.5;

    public List<Station> getShortestRoute(Station from, Station to) {
        List<Station> route = getRouteOnTheLine(from, to);
        if (route != null) {
            return route;
        }

        route = getRouteWithOneConnection(from, to);
        if (route != null) {
            return route;
        }

        route = getRouteWithTwoConnections(from, to);
        return route;
    }

    public double calculateDuration(@NonNull List<Station> route) {
        double duration = 0;
        Station previousStation = null;
        for (int i = 0; i < route.size(); i++) {
            Station station = route.get(i);
            if (i > 0) {
                duration += previousStation.getLine().equals(station.getLine()) ?
                        INTER_STATION_DURATION : INTER_CONNECTION_DURATION;
            }
            previousStation = station;
        }
        return duration;
    }

    private List<Station> getRouteOnTheLine(@NonNull Station from, @NonNull Station to) {
        if (!from.getLine().equals(to.getLine())) {
            return null;
        }
        List<Station> route = new ArrayList<>();
        List<Station> stations = from.getLine().getStations();
        int direction = 0;
        for (Station station : stations) {
            if (direction == 0) {
                if (station.equals(from)) {
                    direction = 1;
                } else if (station.equals(to)) {
                    direction = -1;
                }
            }

            if (direction != 0) {
                route.add(station);
            }

            if ((direction == 1 && station.equals(to)) ||
                    (direction == -1 && station.equals(from))) {
                break;
            }
        }
        if (direction == -1) {
            Collections.reverse(route);
        }
        return route;
    }

    private List<Station> getRouteWithOneConnection(@NonNull Station from, @NonNull Station to) {
        if (from.getLine().equals(to.getLine())) {
            return null;
        }

        List<Station> route = new ArrayList<>();

        List<Station> fromLineStations = from.getLine().getStations();
        List<Station> toLineStations = to.getLine().getStations();
        for (Station fromStation : fromLineStations) {
            for (Station toStation : toLineStations) {
                if (isConnected(fromStation, toStation)) {
                    ArrayList<Station> way = new ArrayList<>();
                    way.addAll(getRouteOnTheLine(from, fromStation));
                    way.addAll(getRouteOnTheLine(toStation, to));
                    if (route.isEmpty() || route.size() > way.size()) {
                        route.clear();
                        route.addAll(way);
                    }
                }
            }
        }
        if (route.size() == 0) {
            return null;
        }
        return route;
    }

    private boolean isConnected(@NonNull Station fromStation, @NonNull Station toStation) {
        AtomicBoolean isContains = new AtomicBoolean(false);

        List<Connection> listConnections = fromStation.getConnections();
        if (!listConnections.isEmpty()) {
            listConnections.forEach(connection -> {
                if (connection.getStationTo().equals(toStation)) {
                    isContains.set(true);
                }
            });
        }
        return isContains.get();
    }

    private List<Station> getRouteViaConnectedLine(@NonNull Station from, @NonNull Station to) {
        List<Connection> connectionsFrom = from.getConnections();
        List<Connection> connectionsTo = to.getConnections();

        Set<Station> fromConnected = new TreeSet<>();
        Set<Station> toConnected = new TreeSet<>();

        connectionsFrom.forEach(c -> fromConnected.add(c.getStationTo()));
        connectionsTo.forEach(c -> toConnected.add(c.getStationTo()));

        for (Station fromStation : fromConnected) {
            for (Station toStation : toConnected) {
                if (fromStation.getLine().equals(toStation.getLine())) {
                    return getRouteOnTheLine(fromStation, toStation);
                }
            }
        }
        return null;
    }

    private List<Station> getRouteWithTwoConnections(@NonNull Station from, @NonNull Station to) {
        if (from.getLine().equals(to.getLine())) {
            return null;
        }

        ArrayList<Station> route = new ArrayList<>();

        List<Station> fromLineStations = from.getLine().getStations();
        List<Station> toLineStations = to.getLine().getStations();

        for (Station fromStation : fromLineStations) {
            for (Station toStation : toLineStations) {
                List<Station> connectedLineRoute =
                        getRouteViaConnectedLine(fromStation, toStation);
                if (connectedLineRoute == null) {
                    continue;
                }
                List<Station> way = new ArrayList<>();
                way.addAll(getRouteOnTheLine(from, fromStation));
                way.addAll(connectedLineRoute);
                way.addAll(getRouteOnTheLine(toStation, to));
                if (route.isEmpty() || route.size() > way.size()) {
                    route.clear();
                    route.addAll(way);
                }
            }
        }
        return route;
    }
}