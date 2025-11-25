package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Application for population reports using the MySQL `world` database.
 */
public class App {

    /** JDBC connection. */
    private Connection con;

    // -------------------------------------------------------------------------
    //  CONNECTION
    // -------------------------------------------------------------------------

    /**
     * Connect to MySQL.
     *
     * @param location host:port of the DB, e.g. "localhost:33060" or "db:3306"
     * @param delayMs  delay between connection attempts in milliseconds
     */
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
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + location + "/world?allowPublicKeyRetrieval=true&useSSL=false",
                        "root",
                        "example");
                System.out.println("Successfully connected");
                break;
            } catch (Exception e) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Disconnect from MySQL.
     */
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (Exception ignored) { }
        }
    }

    // -------------------------------------------------------------------------
    //  WORLD / SIMPLE AGGREGATES
    // -------------------------------------------------------------------------

    /**
     * Returns population of the world (sum of all country populations).
     */
    public long getWorldPopulation() {
        String sql = "SELECT SUM(Population) AS pop FROM country";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong("pop");
            }
        } catch (SQLException e) {
            System.out.println("getWorldPopulation failed: " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Population of a given continent.
     */
    public long getContinentPopulation(String continent) {
        if (continent == null || continent.isBlank()) return 0L;
        String sql = "SELECT SUM(Population) AS pop FROM country WHERE Continent = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("pop");
                }
            }
        } catch (SQLException e) {
            System.out.println("getContinentPopulation failed: " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Population of a given region.
     */
    public long getRegionPopulation(String region) {
        if (region == null || region.isBlank()) return 0L;
        String sql = "SELECT SUM(Population) AS pop FROM country WHERE Region = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("pop");
                }
            }
        } catch (SQLException e) {
            System.out.println("getRegionPopulation failed: " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Population of a country (by name).
     */
    public long getCountryPopulation(String countryName) {
        if (countryName == null || countryName.isBlank()) return 0L;
        String sql = "SELECT Population FROM country WHERE Name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, countryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("Population");
                }
            }
        } catch (SQLException e) {
            System.out.println("getCountryPopulation failed: " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Population of a district (sum of all cities in that district).
     */
    public long getDistrictPopulation(String district) {
        if (district == null || district.isBlank()) return 0L;
        String sql = "SELECT SUM(Population) AS pop FROM city WHERE District = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, district);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("pop");
                }
            }
        } catch (SQLException e) {
            System.out.println("getDistrictPopulation failed: " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Population of a city (if there are multiple cities with the same name, they are summed).
     */
    public long getCityPopulation(String cityName) {
        if (cityName == null || cityName.isBlank()) return 0L;
        String sql = "SELECT SUM(Population) AS pop FROM city WHERE Name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cityName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("pop");
                }
            }
        } catch (SQLException e) {
            System.out.println("getCityPopulation failed: " + e.getMessage());
        }
        return 0L;
    }

    // -------------------------------------------------------------------------
    //  COUNTRY REPORTS
    // -------------------------------------------------------------------------

    /**
     * Helper: builds a Country from current ResultSet row.
     */
    private Country mapCountry(ResultSet rs) throws SQLException {
        Country c = new Country();
        c.code = rs.getString("Code");
        c.name = rs.getString("Name");
        c.continent = rs.getString("Continent");
        c.region = rs.getString("Region");
        c.population = rs.getLong("Population");
        c.capitalName = rs.getString("CapitalName");
        return c;
    }

    /**
     * All countries in the world organised by largest population to smallest.
     */
    public List<Country> getCountriesWorldByPopulationDesc() {
        List<Country> list = new ArrayList<>();
        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population,
                       ct.Name AS CapitalName
                FROM country c
                LEFT JOIN city ct ON c.Capital = ct.ID
                ORDER BY c.Population DESC
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapCountry(rs));
            }
        } catch (SQLException e) {
            System.out.println("getCountriesWorldByPopulationDesc failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * All countries in a continent organised by largest population to smallest.
     */
    public List<Country> getCountriesByContinent(String continent) {
        List<Country> list = new ArrayList<>();
        if (continent == null || continent.isBlank()) return list;

        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population,
                       ct.Name AS CapitalName
                FROM country c
                LEFT JOIN city ct ON c.Capital = ct.ID
                WHERE c.Continent = ?
                ORDER BY c.Population DESC
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCountry(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getCountriesByContinent failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * All countries in a region organised by largest population to smallest.
     */
    public List<Country> getCountriesByRegion(String region) {
        List<Country> list = new ArrayList<>();
        if (region == null || region.isBlank()) return list;

        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population,
                       ct.Name AS CapitalName
                FROM country c
                LEFT JOIN city ct ON c.Capital = ct.ID
                WHERE c.Region = ?
                ORDER BY c.Population DESC
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCountry(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getCountriesByRegion failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Top N populated countries in the world.
     */
    public List<Country> getTopNCountriesWorld(int n) {
        List<Country> list = new ArrayList<>();
        if (!isValidTopN(n)) return list;

        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population,
                       ct.Name AS CapitalName
                FROM country c
                LEFT JOIN city ct ON c.Capital = ct.ID
                ORDER BY c.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCountry(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getTopNCountriesWorld failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Top N populated countries in a continent.
     */
    public List<Country> getTopNCountriesInContinent(String continent, int n) {
        List<Country> list = new ArrayList<>();
        if (!isValidTopN(n) || continent == null || continent.isBlank()) return list;

        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population,
                       ct.Name AS CapitalName
                FROM country c
                LEFT JOIN city ct ON c.Capital = ct.ID
                WHERE c.Continent = ?
                ORDER BY c.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCountry(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getTopNCountriesInContinent failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Top N populated countries in a region.
     */
    public List<Country> getTopNCountriesInRegion(String region, int n) {
        List<Country> list = new ArrayList<>();
        if (!isValidTopN(n) || region == null || region.isBlank()) return list;

        String sql = """
                SELECT c.Code, c.Name, c.Continent, c.Region, c.Population,
                       ct.Name AS CapitalName
                FROM country c
                LEFT JOIN city ct ON c.Capital = ct.ID
                WHERE c.Region = ?
                ORDER BY c.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCountry(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getTopNCountriesInRegion failed: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    //  CITY REPORTS
    // -------------------------------------------------------------------------

    private City mapCity(ResultSet rs) throws SQLException {
        City c = new City();
        c.name = rs.getString("Name");
        c.country = rs.getString("Country");
        c.district = rs.getString("District");
        c.population = rs.getInt("Population");
        return c;
    }

    /**
     * All the cities in the world organised by largest population to smallest.
     */
    public List<City> getCitiesWorldByPopulationDesc() {
        List<City> list = new ArrayList<>();
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                ORDER BY ci.Population DESC
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getCitiesWorldByPopulationDesc failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * All the cities in a continent organised by largest population to smallest.
     */
    public List<City> getCitiesByContinent(String continent) {
        List<City> list = new ArrayList<>();
        if (continent == null || continent.isBlank()) return list;

        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                WHERE co.Continent = ?
                ORDER BY ci.Population DESC
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCity(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getCitiesByContinent failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * All the cities in a region organised by largest population to smallest.
     */
    public List<City> getCitiesByRegion(String region) {
        List<City> list = new ArrayList<>();
        if (region == null || region.isBlank()) return list;

        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                WHERE co.Region = ?
                ORDER BY ci.Population DESC
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCity(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getCitiesByRegion failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * All the cities in a country organised by largest population to smallest.
     */
    public List<City> getCitiesByCountry(String countryName) {
        List<City> list = new ArrayList<>();
        if (countryName == null || countryName.isBlank()) return list;

        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                WHERE co.Name = ?
                ORDER BY ci.Population DESC
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, countryName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCity(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getCitiesByCountry failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * All the cities in a district organised by largest population to smallest.
     */
    public List<City> getCitiesByDistrict(String district) {
        List<City> list = new ArrayList<>();
        if (district == null || district.isBlank()) return list;

        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                WHERE ci.District = ?
                ORDER BY ci.Population DESC
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, district);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCity(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getCitiesByDistrict failed: " + e.getMessage());
        }
        return list;
    }

    // Top N city reports follow the same style:

    public List<City> getTopNCitiesWorld(int n) {
        List<City> list = new ArrayList<>();
        if (!isValidTopN(n)) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                ORDER BY ci.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTopNCitiesWorld failed: " + e.getMessage());
        }
        return list;
    }

    public List<City> getTopNCitiesByContinent(String continent, int n) {
        List<City> list = new ArrayList<>();
        if (!isValidTopN(n) || continent == null || continent.isBlank()) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                WHERE co.Continent = ?
                ORDER BY ci.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTopNCitiesByContinent failed: " + e.getMessage());
        }
        return list;
    }

    public List<City> getTopNCitiesByRegion(String region, int n) {
        List<City> list = new ArrayList<>();
        if (!isValidTopN(n) || region == null || region.isBlank()) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                WHERE co.Region = ?
                ORDER BY ci.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTopNCitiesByRegion failed: " + e.getMessage());
        }
        return list;
    }

    public List<City> getTopNCitiesByCountry(String countryName, int n) {
        List<City> list = new ArrayList<>();
        if (!isValidTopN(n) || countryName == null || countryName.isBlank()) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                WHERE co.Name = ?
                ORDER BY ci.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, countryName);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTopNCitiesByCountry failed: " + e.getMessage());
        }
        return list;
    }

    public List<City> getTopNCitiesByDistrict(String district, int n) {
        List<City> list = new ArrayList<>();
        if (!isValidTopN(n) || district == null || district.isBlank()) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.District, ci.Population
                FROM city ci
                JOIN country co ON ci.CountryCode = co.Code
                WHERE ci.District = ?
                ORDER BY ci.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, district);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTopNCitiesByDistrict failed: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    //  CAPITAL CITY REPORTS
    // -------------------------------------------------------------------------

    private CapitalCity mapCapitalCity(ResultSet rs) throws SQLException {
        CapitalCity c = new CapitalCity();
        c.name = rs.getString("Name");
        c.country = rs.getString("Country");
        c.population = rs.getInt("Population");
        return c;
    }

    public List<CapitalCity> getCapitalCitiesWorldByPopulationDesc() {
        List<CapitalCity> list = new ArrayList<>();
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.Population
                FROM city ci
                JOIN country co ON co.Capital = ci.ID
                ORDER BY ci.Population DESC
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapCapitalCity(rs));
        } catch (SQLException e) {
            System.out.println("getCapitalCitiesWorldByPopulationDesc failed: " + e.getMessage());
        }
        return list;
    }

    public List<CapitalCity> getCapitalCitiesByContinent(String continent) {
        List<CapitalCity> list = new ArrayList<>();
        if (continent == null || continent.isBlank()) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.Population
                FROM city ci
                JOIN country co ON co.Capital = ci.ID
                WHERE co.Continent = ?
                ORDER BY ci.Population DESC
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCapitalCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getCapitalCitiesByContinent failed: " + e.getMessage());
        }
        return list;
    }

    public List<CapitalCity> getCapitalCitiesByRegion(String region) {
        List<CapitalCity> list = new ArrayList<>();
        if (region == null || region.isBlank()) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.Population
                FROM city ci
                JOIN country co ON co.Capital = ci.ID
                WHERE co.Region = ?
                ORDER BY ci.Population DESC
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCapitalCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getCapitalCitiesByRegion failed: " + e.getMessage());
        }
        return list;
    }

    public List<CapitalCity> getTopNCapitalCitiesWorld(int n) {
        List<CapitalCity> list = new ArrayList<>();
        if (!isValidTopN(n)) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.Population
                FROM city ci
                JOIN country co ON co.Capital = ci.ID
                ORDER BY ci.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCapitalCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTopNCapitalCitiesWorld failed: " + e.getMessage());
        }
        return list;
    }

    public List<CapitalCity> getTopNCapitalCitiesByContinent(String continent, int n) {
        List<CapitalCity> list = new ArrayList<>();
        if (!isValidTopN(n) || continent == null || continent.isBlank()) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.Population
                FROM city ci
                JOIN country co ON co.Capital = ci.ID
                WHERE co.Continent = ?
                ORDER BY ci.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCapitalCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTopNCapitalCitiesByContinent failed: " + e.getMessage());
        }
        return list;
    }

    public List<CapitalCity> getTopNCapitalCitiesByRegion(String region, int n) {
        List<CapitalCity> list = new ArrayList<>();
        if (!isValidTopN(n) || region == null || region.isBlank()) return list;
        String sql = """
                SELECT ci.Name, co.Name AS Country, ci.Population
                FROM city ci
                JOIN country co ON co.Capital = ci.ID
                WHERE co.Region = ?
                ORDER BY ci.Population DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCapitalCity(rs));
            }
        } catch (SQLException e) {
            System.out.println("getTopNCapitalCitiesByRegion failed: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    //  POPULATION REPORTS (CONTINENT / REGION / COUNTRY)
    // -------------------------------------------------------------------------

    private PopulationReport mapPopulationRow(ResultSet rs) throws SQLException {
        PopulationReport r = new PopulationReport();
        r.name = rs.getString("Name");
        r.totalPopulation = rs.getLong("TotalPop");
        r.cityPopulation = rs.getLong("CityPop");
        r.nonCityPopulation = r.totalPopulation - r.cityPopulation;
        if (r.totalPopulation > 0) {
            r.cityPercentage = (double) r.cityPopulation * 100.0 / r.totalPopulation;
            r.nonCityPercentage = (double) r.nonCityPopulation * 100.0 / r.totalPopulation;
        } else {
            r.cityPercentage = 0.0;
            r.nonCityPercentage = 0.0;
        }
        return r;
    }

    /**
     * Population report for each continent.
     */
    public List<PopulationReport> getContinentPopulationReports() {
        List<PopulationReport> list = new ArrayList<>();
        String sql = """
                SELECT c.Continent AS Name,
                       SUM(c.Population) AS TotalPop,
                       SUM(ci.Population) AS CityPop
                FROM country c
                LEFT JOIN city ci ON c.Code = ci.CountryCode
                GROUP BY c.Continent
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapPopulationRow(rs));
        } catch (SQLException e) {
            System.out.println("getContinentPopulationReports failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Population report for each region.
     */
    public List<PopulationReport> getRegionPopulationReports() {
        List<PopulationReport> list = new ArrayList<>();
        String sql = """
                SELECT c.Region AS Name,
                       SUM(c.Population) AS TotalPop,
                       SUM(ci.Population) AS CityPop
                FROM country c
                LEFT JOIN city ci ON c.Code = ci.CountryCode
                GROUP BY c.Region
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapPopulationRow(rs));
        } catch (SQLException e) {
            System.out.println("getRegionPopulationReports failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Population report for each country.
     */
    public List<PopulationReport> getCountryPopulationReports() {
        List<PopulationReport> list = new ArrayList<>();
        String sql = """
                SELECT c.Name AS Name,
                       SUM(c.Population) AS TotalPop,
                       SUM(ci.Population) AS CityPop
                FROM country c
                LEFT JOIN city ci ON c.Code = ci.CountryCode
                GROUP BY c.Code, c.Name
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapPopulationRow(rs));
        } catch (SQLException e) {
            System.out.println("getCountryPopulationReports failed: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    //  LANGUAGE REPORTS
    // -------------------------------------------------------------------------

    /**
     * Returns language statistics (Chinese, English, Hindi, Spanish, Arabic).
     */
    public List<LanguageReport> getLanguageReports() {
        List<LanguageReport> list = new ArrayList<>();
        long worldPop = getWorldPopulation();
        if (worldPop == 0) return list;

        String sql = """
                SELECT cl.Language,
                       SUM(c.Population * cl.Percentage / 100) AS Speakers
                FROM countrylanguage cl
                JOIN country c ON cl.CountryCode = c.Code
                WHERE cl.Language IN ('Chinese','English','Hindi','Spanish','Arabic')
                GROUP BY cl.Language
                ORDER BY Speakers DESC
                """;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                LanguageReport r = new LanguageReport();
                r.language = rs.getString("Language");
                r.speakers = rs.getLong("Speakers");
                r.percentage = (double) r.speakers * 100.0 / worldPop;
                list.add(r);
            }
        } catch (SQLException e) {
            System.out.println("getLanguageReports failed: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    //  HELPERS: VALIDATION + FORMATTING
    // -------------------------------------------------------------------------

    /** Guard for Top-N queries. */
    public boolean isValidTopN(int n) {
        return n > 0;
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    /** Format country row for console output. */
    public String formatCountryRow(Country c) {
        if (c == null) return "";
        return String.format("%-6s | %-32s | %-11s | %-22s | %12d | %-20s",
                nz(c.code), nz(c.name), nz(c.continent), nz(c.region), c.population, nz(c.capitalName));
    }

    /** Format city row for console output. */
    public String formatCityRow(City c) {
        if (c == null) return "";
        return String.format("%-32s | %-32s | %-20s | %12d",
                nz(c.name), nz(c.country), nz(c.district), c.population);
    }

    /** Format capital city row for console output. */
    public String formatCapitalCityRow(CapitalCity c) {
        if (c == null) return "";
        return String.format("%-32s | %-32s | %12d",
                nz(c.name), nz(c.country), c.population);
    }

    /** Format population report row for console output. */
    public String formatPopulationReportRow(PopulationReport r) {
        if (r == null) return "";
        return String.format("%-30s | %12d | %12d (%.2f%%) | %12d (%.2f%%)",
                nz(r.name),
                r.totalPopulation,
                r.cityPopulation, r.cityPercentage,
                r.nonCityPopulation, r.nonCityPercentage);
    }

    /** Format language report row for console output. */
    public String formatLanguageReportRow(LanguageReport r) {
        if (r == null) return "";
        return String.format("%-10s | %12d | %6.2f%%",
                nz(r.language), r.speakers, r.percentage);
    }

    // -------------------------------------------------------------------------
    //  MAIN
    // -------------------------------------------------------------------------

    /**
     * Entry point.
     * Works both locally and inside Docker.
     */
    public static void main(String[] args) {
        App app = new App();

        // 1) Read DB connection from environment (for Docker)
        String envHost = System.getenv("DB_HOST");       // e.g. "db:3306"
        String envDelay = System.getenv("DB_DELAY_MS");  // e.g. "5000"

        // 2) Determine host:port
        String location;
        if (envHost != null && !envHost.isBlank()) {
            location = envHost;
        } else if (args.length >= 1) {
            location = args[0];
        } else {
            location = "localhost:33060";  // default for local Docker MySQL
        }

        // 3) Determine delay (ms)
        int delayMs = 30000;  // default = 30 seconds
        if (envDelay != null && !envDelay.isBlank()) {
            try {
                delayMs = Integer.parseInt(envDelay);
            } catch (NumberFormatException ignored) { }
        } else if (args.length >= 2) {
            try {
                delayMs = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) { }
        }

        // Connect
        app.connect(location, delayMs);

        // Simple demo output (you can change this if you want)
        System.out.println("World population = " + app.getWorldPopulation());

        List<Country> world = app.getCountriesWorldByPopulationDesc();
        System.out.println("\nTop 10 countries by population (world):");
        for (int i = 0; i < Math.min(10, world.size()); i++) {
            System.out.println(app.formatCountryRow(world.get(i)));
        }

        List<Country> europeTop = app.getTopNCountriesInContinent("Europe", 5);
        System.out.println("\nTop 5 countries in Europe:");
        for (Country c : europeTop) {
            System.out.println(app.formatCountryRow(c));
        }

        app.disconnect();
    }
}
