package at.ac.tuwien.ldsc.group1.application;

import java.util.Set;

import at.ac.tuwien.ldsc.group1.domain.CloudOverallInfo;
import at.ac.tuwien.ldsc.group1.domain.Event;
import at.ac.tuwien.ldsc.group1.domain.components.Application;

public class Scheduler3 implements Scheduler {

    @Override
    public void schedule(Event event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addApplication(Application application) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeApplication(Application application) {
        // TODO Auto-generated method stub

    }
    
    @Override
   	public void finalize(){
   	}

	@Override
	public void setMaxNumberOfPhysicalMachines(int nr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void callScheduling(Set<Event> events) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CloudOverallInfo getOverAllInfo() {
		// TODO Auto-generated method stub
		return null;
	}
}
