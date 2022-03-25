package metro;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import metro.core.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@SuppressWarnings("unchecked")
public class ParserJson {
    public StationIndex stationIndex;
    private static final String METRO = "src/metro/resources/map.json";

    public StationIndex getStationIndex() {
        return stationIndex;
    }

    public void createStationIndex() {
        stationIndex = new StationIndex();
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(getJsonFile());

            JSONArray linesArray = (JSONArray) jsonData.get("lines");
            parseLines(linesArray);

            JSONObject stationsObject = (JSONObject) jsonData.get("stations");
            parseStations(stationsObject);

            JSONArray connectionsArray = (JSONArray) jsonData.get("connections");
            parseConnections(connectionsArray);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseConnections(@NonNull JSONArray connectionsArray) {
        connectionsArray.forEach(connectionObject -> {
            JSONArray connection = (JSONArray) connectionObject;
            List<Station> connectionStations = new ArrayList<>();

            connection.forEach(item -> {
                JSONObject itemObject = (JSONObject) item;
                int lineNumber = ((Long) itemObject.get("line")).intValue();
                String stationName = (String) itemObject.get("station");

                Station station = stationIndex.getStation(stationName, lineNumber);
                if (station == null) {
                    throw new IllegalArgumentException("core.Station " +
                            stationName + " on line " + lineNumber + " not found");
                }
                connectionStations.add(station);
            });
            stationIndex.addConnection(connectionStations);
        });
    }

    private void parseStations(@NonNull JSONObject stationsObject) {
        stationsObject.keySet().forEach(lineNumberObject ->
        {
            AtomicInteger number = new AtomicInteger(1);
            int lineNumber = Integer.parseInt((String) lineNumberObject);
            Line line = stationIndex.getLine(lineNumber);
            JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);

            stationsArray.forEach(stationObject -> {
                Station station = new Station();

                station.setNumber(number.get());
                station.setName((String) stationObject);

                number.set(number.get() + 1);

                line.addStation(station);
                stationIndex.addStation(station);
            });
        });
    }

    private void parseLines(@NonNull JSONArray linesArray) {
        linesArray.forEach(lineObject -> {
            JSONObject lineJsonObject = (JSONObject) lineObject;
            Line line = new Line();
            String colorLine = (String) lineJsonObject.get("color");

            line.setNumber(((Long) lineJsonObject.get("number")).intValue());
            line.setName((String) lineJsonObject.get("name"));

            switch (colorLine) {
                case "red" -> line.setColor("красная");
                case "blue" -> line.setColor("синяя");
                case "green" -> line.setColor("зелёная");
                case "orange" -> line.setColor("оранжевая");
                case "violet" -> line.setColor("фиолетовая");
            }
            stationIndex.addLine(line);
        });
    }

    private @NonNull String getJsonFile() {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(METRO));
            lines.forEach(builder::append);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }
}