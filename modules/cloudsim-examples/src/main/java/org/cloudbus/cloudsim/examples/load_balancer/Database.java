package org.cloudbus.cloudsim.examples.load_balancer;

import java.util.HashMap;
import java.util.Map;

public class Database {

    // A simple map to hold dummy data
    private Map<String, String> data;

    // Constructor to initialize the database with some dummy data
    public Database() {
        data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        data.put("key3", "value3");
        // ... add more data as needed
    }

    // Method to simulate a database query
    public String query(String key) {
        // Simulate some delay in database access (e.g., 10 milliseconds)
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return the data associated with the provided key
        return data.get(key);
    }

}
