package org.example;

import java.util.HashMap;
import java.util.Locale;

// The SLAService class encapsulates the logic to handle Service Level Agreement (SLA) based on effect and priority.
class SlaTimeCalc {
    private final HashMap<String, Output> responseTable = new HashMap<>();
    String priority;
    String effect;
    int responseTime;
    int resolutionTime;

    public SlaTimeCalc(String priority, String effect) {
        //set variables
        //set priority and effect. changes to lower case
        this.priority = priority.toLowerCase();
        this.effect = effect.toLowerCase();
        //populate table
        PopulateTable();
        //set responseTime and resolutionTime
        responseTime = responseTable.get(priority + " " + effect).getResponseTime();
        resolutionTime = responseTable.get(priority + " " + effect).getResolutionTime();
    }

    public void PopulateTable() {
        //add info to map
        //populate table
        responseTable.put(("low low"), new Output(3, 5));
        responseTable.put(("medium low"), new Output(2, 5));
        responseTable.put(("high low"), new Output(1, 5));
        responseTable.put(("low medium"), new Output(3, 3));
        responseTable.put(("medium medium"), new Output(3, 3));
        responseTable.put(("high medium"), new Output(1, 3));
        responseTable.put(("low high"), new Output(3, 1));
        responseTable.put(("medium high"), new Output(2, 1));
        responseTable.put(("high high"), new Output(1, 1));
    }
//get
    public int getResponseTime() {
        return responseTime;
    }

    public int getResolutionTime() {
        return resolutionTime;
    }
//output class for table
    static class Output {
        //vars
        private final int responseTime;
        private final int resolutionTime;

        // Constructor to initialize the output with response time and resolution time.
        Output(int responseTime, int resolutionTime) {
            this.responseTime = responseTime;
            this.resolutionTime = resolutionTime;
        }

        public int getResponseTime() {
            return responseTime;

        }

        public int getResolutionTime() {
            return resolutionTime;
        }
    }
}



