package at.ac.tuwien.ldsc.group1.application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import at.ac.tuwien.ldsc.group1.domain.Event;
import at.ac.tuwien.ldsc.group1.domain.EventType;
import at.ac.tuwien.ldsc.group1.domain.components.Application;
import at.ac.tuwien.ldsc.group1.domain.exceptions.SchedulingNotPossibleException;

/**
 * This is our Energy-Efficient Cloud Elasticity Manager (E2CEM).
 *
 * It will initialise a scheduler and then control when an application is
 * added (based on the timestamp) and when it needs to be removed (based on the
 * duration).
 */
public class E2CElasticityManager {
    Schedulable scheduler;
    CsvParser csvParser;
    CsvWriter csvWriter;
    Set<Event> events;
    List<Application> runningApps = new ArrayList<Application>();

    public E2CElasticityManager(CsvParser parser, CsvWriter writer, Schedulable scheduler) {

    	this.csvParser = parser;
    	this.csvWriter = writer;
    	this.scheduler = scheduler;

    }

    public void startSimulation() {
        //1. Get list of application from parser

		List<Application> appList = csvParser.parse();

        //2. Build interval list that transforms the list of applications from the parser
        //   into events

		events = new TreeSet<Event>();
		for(Application app : appList){

			//For Each Application there is
			//One Event when the app STARTS
			//One Event when the app STOPS
			//
			//Store Events ordered by their eventTime

			long startTime = app.getTimeStamp();
			long stopTime = app.getTimeStamp()+app.getDuration();

			Event startEvent = new Event(startTime, EventType.START, app);
			Event stopEvent = new Event(stopTime, EventType.STOP, app);


			events.add(startEvent);
			events.add(stopEvent);

		}

        for(Event event : events) {
            //3. Feed it into the scheduler
        	//EVENTS ARE ORDERED
        	if(!event.isToBeSkipped())  callScheduling(event);
        }

        //close streams
        scheduler.finalize();

        // Finally log summary information of cloud to output file 1
        //String info = scheduler.getSummaryInfo();
    }

	private void callScheduling(Event event) {
		
		try {
			scheduler.schedule(event);
			runningApps.add(event.getApplication());
		} catch (SchedulingNotPossibleException e) {
			
			//getnextStopEvent
			Event stopEvent = getNextStopEvent(event);
			//set additional scheduler time
//			scheduler.addToInternalTime(stopEvent.getEventTime() - event.getEventTime());
			//schedule stop
			this.callScheduling(stopEvent);
			//remove this stop event from the event list
			
//			events.remove(stopEvent);
			stopEvent.setToBeSkipped(true);
			//schedule original event
			this.callScheduling(event);
			
		}
		
		
	}

	private Event getNextStopEvent(Event event) {
		for(Event e : events){
			if(e.getEventTime() > event.getEventTime() && e.getEventType().equals(EventType.STOP) && runningApps.contains(e.getApplication()) && !e.isToBeSkipped()){
				return e;
			}
		}
		System.out.println("Cloud is full and no App can be stopped.");
		return null;
	}
}


