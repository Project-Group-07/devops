package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CR3: подключение к world, простые отчеты + хелперы. */
public class App {
    private Connection con;

    /** Подключение к MySQL. location вида "localhost:33060". */
    public void connect(String location, int delayMs) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }
        for (int i = 0; i < 10; i++) {
            System.out.println("Connecting to database...");
            try {
                Thread.sleep(delayMs);
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + location + "/world?allowPublicKeyRetrieval=true&useSSL=false",
                        "root", "example");
                System.out.println("Successfully connected");
                break;
            } catch (Exception e) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(e.getMessage());
            }
        }
    }

    public void disconnect() {
        if (con != null) try { con.close(); } catch (Exception ignored) {}
    }

    /** Население мира (SUM по country.Population). */
    public long getWorldPopulation() {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(Population) AS pop FROM country")) {
            return rs.next() ? rs.getLong("pop") : 0L;
        } catch (Exception e) {
            System.out.println("getWorldPopulation failed: " + e.getMessage());
            return 0L;
        }
    }

    /** Все страны мира по населению (DESC). */
    public List<Country> getCountriesWorldByPopulationDesc() {
        List<Country> list = new ArrayList<>();
        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population, ct.Name AS CapitalName
                FROM country c LEFT JOIN city ct ON c.Capital = ct.ID
                ORDER BY c.Population DESC
                """;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Country c = new Country();
                c.code = rs.getString("Code");
                c.name = rs.getString("Name");
                c.continent = rs.getString("Continent");
                c.region = rs.getString("Region");
                c.population = rs.getLong("Population");
                c.capitalName = rs.getString("CapitalName");
                list.add(c);
            }
        } catch (Exception e) {
            System.out.println("getCountriesWorldByPopulationDesc failed: " + e.getMessage());
        }
        return list;
    }

    /** Топ-N стран по континенту (DESC). */
    public List<Country> getTopNCountriesInContinent(String continent, int n) {
        List<Country> list = new ArrayList<>();
        if (!isValidTopN(n) || continent == null || continent.isBlank()) return list;

        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population, ct.Name AS CapitalName
                FROM country c LEFT JOIN city ct ON c.Capital = ct.ID
                WHERE c.Continent = ?
                ORDER BY c.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Country c = new Country();
                    c.code = rs.getString("Code");
                    c.name = rs.getString("Name");
                    c.continent = rs.getString("Continent");
                    c.region = rs.getString("Region");
                    c.population = rs.getLong("Population");
                    c.capitalName = rs.getString("CapitalName");
                    list.add(c);
                }
            }
        } catch (Exception e) {
            System.out.println("getTopNCountriesInContinent failed: " + e.getMessage());
        }
        return list;
    }

    /** Guard для Top-N. */
    public boolean isValidTopN(int n) { return n > 0; }

    /** Формат строки отчета о стране (для юнит-тестов/вывода). */
    public String formatCountryRow(Country c) {
        if (c == null) return "";
        return String.format("%-6s | %-32s | %-13s | %-22s | %12d | %-20s",
                nz(c.code), nz(c.name), nz(c.continent), nz(c.region), c.population, nz(c.capitalName));
    }
    private String nz(String s) { return s == null ? "" : s; }

    public static void main(String[] args) {
        App a = new App();
        if (args.length < 2) a.connect("localhost:33060", 30000);
        else a.connect(args[0], Integer.parseInt(args[1]));
        System.out.println("World population = " + a.getWorldPopulation());
        a.disconnect();
    }
}
