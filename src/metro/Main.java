package metro;

import lombok.NonNull;
import metro.core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;

import java.util.*;

public class Main {
    private static Logger logger;
    private static Session session;
    private static Scanner scanner;
    private static final SessionFactory sessionFactory;

    static {
        try {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml").build();
            Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
            sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        return sessionFactory.openSession();
    }

    public static void main(String[] args) {
        // Перед первым запуском:
        // 1. создать базу SQL с именем - metrospb;
        // 2. из класса Main один раз запустить метод fillingDatabase();

        System.out.println("Программа расчёта маршрутов метрополитена Санкт-Петербурга\n");

        try {
            RouteCalculator calculator = new RouteCalculator();
            scanner = new Scanner(System.in);
            logger = LogManager.getLogger();
            session = getSession();
            session.beginTransaction();

            Station from = takeStation("Введите станцию отправления:");
            Station to = takeStation("Введите станцию назначения:");
            List<Station> route = calculator.getShortestRoute(from, to);

            session.getTransaction().commit();

            System.out.println("Маршрут:");
            printRoute(route);

            System.out.println("Длительность: " +
                    calculator.calculateDuration(route) + " минут");

        } catch (Exception ex) {
            logger.error(ex.toString());
        } finally {
            session.close();
            sessionFactory.close();
        }
    }

    private static @NonNull Station takeStation(String message) {
        for (; ; ) {
            System.out.println(message);
            String line = scanner.nextLine().trim();

            String hql = "From " + Station.class.getSimpleName() + " As s Where s.name = " + "'" + line + "'";
            Object query = session.createQuery(hql).uniqueResult();

            if (query != null) {
                logger.info("Поиск станции - " + line);
                return (Station) query;
            }
            logger.warn("Станция не найдена - " + line);
            System.out.println("Станция не найдена :(");
        }
    }

    private static void printRoute(@NonNull List<Station> route) {
        Station previousStation = null;
        for (Station station : route) {
            if (previousStation != null) {
                Line prevLine = previousStation.getLine();
                Line nextLine = station.getLine();
                if (!prevLine.equals(nextLine)) {
                    System.out.println("\tПереход на станцию " +
                            station.getName() + " (" + nextLine.getName() + " линия)");
                }
            }
            System.out.println("\t" + station.getName());
            previousStation = station;
        }
    }

    private static void fillingDatabase() {
        try {
            logger = LogManager.getLogger();

            ParserJson parser = new ParserJson();
            parser.createStationIndex();

            session = getSession();
            session.beginTransaction();

            SaveToSQL saveToSQL = new SaveToSQL(session, parser.getStationIndex());
            saveToSQL.saver();

            session.getTransaction().commit();
        } catch (Exception ex) {
            logger.error(ex.toString());
        } finally {
            session.close();
            sessionFactory.close();
        }
    }
}