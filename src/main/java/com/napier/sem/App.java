package com.napier.sem;

/**
 * Demo app for Code Review 2.
 * Prints a mock report now. JDBC code is scaffolded for Code Review 3.
 */
public class App
{
    public static void main(String[] args) {
        // --- Demo output (mock) ---
        System.out.println("=== World Population Reporting System ===");
        System.out.println("System initialized successfully.");
        System.out.println("Sample report (mock data):");
        System.out.println("Country\t\tPopulation");
        System.out.println("China\t\t1,411,778,724");
        System.out.println("India\t\t1,380,004,385");
        System.out.println("USA\t\t331,002,651");
        System.out.println("Report generated successfully.");

        // --- JDBC scaffold for CR3 (leave commented for now) ---
        // String url = "jdbc:mysql://localhost:3306/world";
        // String user = "root";
        // String pass = "password";
        // String sql  = "SELECT Name, Population FROM country ORDER BY Population DESC LIMIT 3";
        // try (Connection c = DriverManager.getConnection(url, user, pass);
        //      Statement st = c.createStatement();
        //      ResultSet rs = st.executeQuery(sql)) {
        //     System.out.println("\nTop 3 countries by population (live DB):");
        //     while (rs.next()) {
        //         System.out.printf("%-20s %,d%n", rs.getString("Name"), rs.getInt("Population"));
        //     }
        // } catch (SQLException e) {
        //     System.err.println("DB connection failed: " + e.getMessage());
        // }
    }
}