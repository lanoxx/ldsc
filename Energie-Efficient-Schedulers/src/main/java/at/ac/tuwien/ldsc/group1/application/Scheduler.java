package at.ac.tuwien.ldsc.group1.application;

import at.ac.tuwien.ldsc.group1.domain.CloudOverallInfo;
import at.ac.tuwien.ldsc.group1.domain.Event;
import at.ac.tuwien.ldsc.group1.domain.components.Application;
import at.ac.tuwien.ldsc.group1.domain.exceptions.ResourceUnavailableException;
import at.ac.tuwien.ldsc.group1.domain.exceptions.SchedulingNotPossibleException;
import com.google.common.collect.TreeMultiset;

public interface Scheduler {
    void schedule(Event event);

    /**
     * Template method for use inside the scheduler
     * this function is called by the schedule method.
     * @throws SchedulingNotPossibleException 
     */
    void addApplication(Application application) throws ResourceUnavailableException, SchedulingNotPossibleException;

    /**
     * Template method for use inside the scheduler,
     * this function is called by the schedule method.
     */
    void removeApplication(Application application);

    /**
     *  To close the streams
     */
    void finalize();
    
    void setMaxNumberOfPhysicalMachines(int nr);

    void handleEvents(TreeMultiset<Event> events);

    public CloudOverallInfo getOverAllInfo();
}