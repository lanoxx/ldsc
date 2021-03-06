package at.ac.tuwien.ldsc.group1.domain;

/**
 * This is a DTO that contains all the data from the scheduler that is required by the
 * CsvWriter to log the cloud utilization.
 */
public class CloudOverallInfo {

    String scheduler = "N/A";
    String scenario = "N/A";
    int totalPMs = 0, totalVMs = 0, totalInSourced = 0, totalOutSourced = 0;
    long totalDuration = 0L;
    double totalPowerConsumption = 0;


    public CloudOverallInfo() {
        super();
    }

    public String getScheduler() {
        return scheduler;
    }

    public String getScenario() {
        return scenario;
    }

    public int getTotalPMs() {
        return totalPMs;
    }

    public int getTotalVMs() {
        return totalVMs;
    }

    public int getTotalInSourced() {
        return totalInSourced;
    }

    public int getTotalOutSourced() {
        return totalOutSourced;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public void setTotalPMs(int totalPMs) {
        this.totalPMs = totalPMs;
    }

    public void setTotalVMs(int totalVMs) {
        this.totalVMs = totalVMs;
    }

    public void setTotalInSourced(int totalInSourced) {
        this.totalInSourced = totalInSourced;
    }

    public void setTotalOutSourced(int totalOutSourced) {
        this.totalOutSourced = totalOutSourced;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public void setTotalPowerConsumption(double totalPowerConsumption) {
        this.totalPowerConsumption = totalPowerConsumption;
    }

    @Override
    public String toString() {
        return this.getScheduler() + ";" + this.getScenario() + ";" + this.getTotalPMs() + ";" + this.getTotalVMs() + ";" +
                +this.getTotalDuration() + ";" + this.getTotalPowerConsumption() + ";" + this.getTotalInSourced() + ";" + this.getTotalOutSourced();
    }

}
