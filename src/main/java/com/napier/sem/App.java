package com.napier.sem;

/**
 * Application entry point.
 * For Code Review 2 this only prints mock data.
 * Real database queries will be implemented in Code Review 3.
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

        // --- JDBC scaffold for Code Review 3 ---
// (Leave this section commented out for now. It shows how the real
// database connection and query will be implemented in CR3.)

// Connection details for MySQL database
// String url = "jdbc:mysql://localhost:3306/world";   // Database location (schema = world)
// String user = "root";                               // Database username
// String pass = "password";                           // Database password

// Example SQL query: select top 3 countries by population
// String sql  = "SELECT Name, Population FROM country ORDER BY Population DESC LIMIT 3";

// Try-with-resources block automatically closes the connection after use
// try (Connection c = DriverManager.getConnection(url, user, pass);
//      Statement st = c.createStatement();
//      ResultSet rs = st.executeQuery(sql)) {

//     // Print a live database report (to be used in Code Review 3)
//     System.out.println("\nTop 3 countries by population (live DB):");

//     // Iterate through the query results
//     while (rs.next()) {
//         // Print formatted output: Country name and population with commas
//         System.out.printf("%-20s %,d%n", rs.getString("Name"), rs.getInt("Population"));
//     }

// } catch (SQLException e) {
//     // Print database connection or query error
//     System.err.println("DB connection failed: " + e.getMessage());
// }

    }
}
