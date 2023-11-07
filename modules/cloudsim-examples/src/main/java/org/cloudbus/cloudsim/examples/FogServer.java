package org.cloudbus.cloudsim.examples;

import java.util.Queue;
import java.util.LinkedList;

public class FogServer {

    private final Queue<VitalSigns> dataQueue;

    public FogServer() {
        dataQueue = new LinkedList<>();
    }

    // 1. Accept ECG data
    public void receiveData(VitalSigns vitalSigns) {
        dataQueue.add(vitalSigns);
    }

    // 2. Process/analyze the ECG data
    public void processData() {
        while (!dataQueue.isEmpty()) {
            VitalSigns data = dataQueue.poll();

            // ECG Analysis
            if (isAbnormalHeartRate(data.heartRate)) {
                alert("Abnormal heart rate detected: " + data.heartRate);
            }

            if (hasIrregularPattern(data.ecgPattern)) {
                alert("Irregular ECG pattern detected: " + data.ecgPattern);
            }

            // Blood Pressure Analysis
            if (isHighBloodPressure(data.systolicBP, data.diastolicBP)) {
                alert("High blood pressure detected!");
            }

            // Oxygen Saturation
            if (isLowOxygenLevel(data.oxygenSaturation)) {
                alert("Low oxygen saturation detected!");
            }
        }
    }

    private boolean isHighBloodPressure(int systolic, int diastolic) {
        return systolic > 180 || diastolic > 120;
    }

    private boolean isLowOxygenLevel(int oxygenSaturation) {
        return oxygenSaturation < 90;
    }

    private boolean isAbnormalHeartRate(int heartRate) {
        return heartRate < 50 || heartRate > 100;  // Simplified for example purposes
    }

    private boolean hasIrregularPattern(String ecgPattern) {
        // A placeholder; in real life, this would be a complex algorithm analyzing the ECG waveform
        return "ARRHYTHMIA".equals(ecgPattern);
    }

    private void alert(String message) {
        // Alerting mechanism, could be sending a notification, triggering an alarm, etc.
        System.out.println("ALERT: " + message);
    }

    public static void main(String[] args) {
        FogServer fogServer = new FogServer();

        // Simulate receiving ECG data and other vital signs
        fogServer.receiveData(new VitalSigns(45, "NORMAL", 110, 70, 92));  // Low heart rate
        fogServer.receiveData(new VitalSigns(110, "NORMAL", 130, 80, 99)); // High heart rate
        fogServer.receiveData(new VitalSigns(75, "ARRHYTHMIA", 120, 75, 88));  // Irregular pattern and low oxygen
        fogServer.receiveData(new VitalSigns(70, "NORMAL", 120, 80, 98));  // Normal data
        fogServer.receiveData(new VitalSigns(68, "TACHYCARDIA", 185, 121, 85));  // High blood pressure and low oxygen

        // Process and analyze data
        fogServer.processData();
    }


}

class ECGData {
    int heartRate;       // Beats per minute
    String ecgPattern;   // Simplified to a string; in reality, this would be a more complex data type

    public ECGData(int heartRate, String ecgPattern) {
        this.heartRate = heartRate;
        this.ecgPattern = ecgPattern;
    }
}

class VitalSigns extends ECGData {
    int systolicBP;  // Systolic Blood Pressure
    int diastolicBP; // Diastolic Blood Pressure
    int oxygenSaturation;  // Oxygen Saturation in %

    // Constructor
    public VitalSigns(int heartRate, String ecgPattern, int systolicBP, int diastolicBP, int oxygenSaturation) {
        super(heartRate, ecgPattern);
        this.systolicBP = systolicBP;
        this.diastolicBP = diastolicBP;
        this.oxygenSaturation = oxygenSaturation;
    }
}

