package at.ac.tuwien.ldsc.group1.application;

import at.ac.tuwien.ldsc.group1.domain.CloudOverallInfo;
import at.ac.tuwien.ldsc.group1.domain.CloudStateInfo;
import at.ac.tuwien.ldsc.group1.domain.WriterType;
import at.ac.tuwien.ldsc.group1.ui.interfaces.GuiLogger;
import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class CsvWriter {
    private BufferedWriter bufferedWriter;
    private GuiLogger guiLogger = null;
    private long lastTimeStamp = 0L;
    private boolean extrapolationActive;

    /**
     * @param baseName The base name of the file (the filename will in the style <pre>baseName-yyyy-MM-dd.csv</pre>
     *                 <br/>Where yyyy-MM-dd is the current date
     */
    public CsvWriter(String baseName, WriterType type) {
        DateTime date = new DateTime();
        String dateString = date.toString("yyyy-MM-dd");
        try {
            File file = new File(baseName + "-" + dateString + ".csv");

            // if file doesn't exist, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bufferedWriter = new BufferedWriter(fw);

            switch (type) {
                case SCENARIO:
                    writeScenarioHeader();
                    break;
                case OVERVIEW:
                    writeOverviewHeader();
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeScenarioHeader() {
        try {
            bufferedWriter.write("Timestamp; TotalRAM; TotalCPU; TotalSize; RunningPMs; RunningVMs; TotalPowerConsumption; InSourced; OutSourced");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeOverviewHeader() {
        try {
            bufferedWriter.write("Scheduler;Scenario;TotalPMs;TotalVMs;TotalDuration;TotalPowerConsumption;TotalInSourced;TotalOutSourced");
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLine(CloudStateInfo cloudInfo) {
        try {
            if(this.guiLogger != null) {
                guiLogger.writeGuiLog(cloudInfo);
            }
            System.out.println("[Writing to LOG:] "+cloudInfo.toString());
            // This will extrapolate the timestamp gap in the output file by repeating
            // the same output for the missing files.
            if(extrapolationActive) {
                for(long i = lastTimeStamp; i < cloudInfo.getTimestamp(); i++) {
                    bufferedWriter.write(String.format(cloudInfo.getExtrapolatableString(), i));
                    bufferedWriter.newLine();
                }
            }
            lastTimeStamp = cloudInfo.getTimestamp();
            bufferedWriter.write(cloudInfo.toString());
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLine(CloudOverallInfo cloudInfo) {
        try {
            bufferedWriter.write(cloudInfo.toString());
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setGuiLogger(GuiLogger guiLogger){
        this.guiLogger  = guiLogger;
    }

    public void activateExtrapolation(boolean activate) {
        this.extrapolationActive = activate;
    }
}
