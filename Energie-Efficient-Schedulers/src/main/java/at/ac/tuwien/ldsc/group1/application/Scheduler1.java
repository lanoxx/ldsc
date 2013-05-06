package at.ac.tuwien.ldsc.group1.application;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import at.ac.tuwien.ldsc.group1.domain.CloudOverallInfo;
import at.ac.tuwien.ldsc.group1.domain.CloudStateInfo;
import at.ac.tuwien.ldsc.group1.domain.Event;
import at.ac.tuwien.ldsc.group1.domain.EventType;
import at.ac.tuwien.ldsc.group1.domain.components.Application;
import at.ac.tuwien.ldsc.group1.domain.components.Component;
import at.ac.tuwien.ldsc.group1.domain.components.Machine;
import at.ac.tuwien.ldsc.group1.domain.components.PhysicalMachine;
import at.ac.tuwien.ldsc.group1.domain.components.PhysicalMachineImpl;
import at.ac.tuwien.ldsc.group1.domain.components.VirtualMachine;
import at.ac.tuwien.ldsc.group1.domain.components.VirtualMachineImpl;
import at.ac.tuwien.ldsc.group1.domain.exceptions.ResourceUnavailableException;
import at.ac.tuwien.ldsc.group1.domain.exceptions.SchedulingNotPossibleException;

public class Scheduler1 implements Scheduler {
	
	int maxPMs;
	long internalTime = 0L;
	long lastinternalTime = 0L;
	
    List<Application> applications = new ArrayList<>();
    List<PhysicalMachine> physicalMachines;
    Integer VMramBase;
    Integer VMhddBase;
    Integer VMcpuInMhzBase;
    CsvWriter writer;
    Event lastEvent = null;
    double lastTotalCosumption = 0;

    CloudStateInfo lastState = null;
    final CloudOverallInfo overallInfo = new CloudOverallInfo();
    Set<Event> events;
    
    public Scheduler1(CsvWriter writer) {
    	ResourceBundle res = ResourceBundle.getBundle("virtualMachine");
    	VMramBase = Integer.parseInt(res.getString("ramBase"));
		VMhddBase = Integer.parseInt(res.getString("sizeBase"));
		VMcpuInMhzBase = Integer.parseInt(res.getString("cpuBase")); 
		this.writer = writer; 
    	
	}

	@Override
    public void schedule(Event event) throws SchedulingNotPossibleException {
        if(event.getEventType() == EventType.START) {
            try {
            	this.addApplication(event.getApplication());
            }catch (ResourceUnavailableException e) {
				e.printErrorMsg();
				throw new SchedulingNotPossibleException();
			}
            
        } else {
            this.removeApplication(event.getApplication());
        }
        
        long previousTimeStamp = 0L;
        if(lastEvent != null){
        	previousTimeStamp = lastEvent.getEventTime();
        }
        if(event.getEventTime() - previousTimeStamp > 0){
        	lastinternalTime = internalTime;
        	internalTime = internalTime + (event.getEventTime() - previousTimeStamp);
        }else{
        	//leave internal time as it is. //the entire time scale will be shifted
        }
        this.writeLog();
        this.lastEvent = event;
    }
	
	@Override
	public void callScheduling(Set<Event> events) {
		this.events = events;
		for(Event e: events){
			if(!e.isToBeSkipped()) handleEvent(e);
		}
	}
	
	private void handleEvent(Event event) {
		
		try {
			this.schedule(event);
			applications.add(event.getApplication());
		} catch (SchedulingNotPossibleException e) {
			
			//getnextStopEvent
			Event stopEvent = getNextStopEvent(event);
			//set additional scheduler time
//			scheduler.addToInternalTime(stopEvent.getEventTime() - event.getEventTime());
			//schedule stop
			this.handleEvent(stopEvent);
			//remove this stop event from the event list
			stopEvent.setToBeSkipped(true);
			//schedule original event
			this.handleEvent(event);
			
		}
	}

    @Override
    public void addApplication(Application application) throws ResourceUnavailableException, SchedulingNotPossibleException {
        //1. make a decision on which virtual machine this application will run
    	   	
    	//A.) Create VM
    	//B.) Fill PM with VM until they are full
    	//	B/1.) Start new PM if needed
    	//	B/2.) Optimize PM selection
    	Integer neededRam = application.getRam() + this.VMramBase;
    	Integer neededHddSize = application.getHddSize() + this.VMhddBase;
    	Integer neededCpuInMHz = application.getCpuInMhz() + this.VMcpuInMhzBase;
    	
    	PhysicalMachine pm = selectOptimalPM(neededRam,neededHddSize,neededCpuInMHz);
    	VirtualMachine vm = new VirtualMachineImpl(pm);

    	vm.start(); 		 //TODO what is start stand for? Can we do there the resource allocation?
    	overallInfo.setTotalVMs(overallInfo.getTotalVMs()+1);
    	//allocate resources
    	try {
    		vm.addComponent(application);		//allocate resources inside this method
		} catch (ResourceUnavailableException e) {
			
			e.printResourceAllocationErrorLog(pm,vm,neededCpuInMHz,neededHddSize,neededRam);
			
		}
    	
    	

        //Finally: Log current cloud utilization details to output file 2
    }

	@Override
    public void removeApplication(Application application) {
        //1. find the virtual machine on which this application runs
        //   and remove it.
		VirtualMachine hostVM = null;
		for (PhysicalMachine pm : physicalMachines) {
			for(Component VMcomp : pm.getComponents()){
				VirtualMachine vm = (VirtualMachine) VMcomp;
				for(Component appComp : vm.getComponents()){
					if(appComp.equals(application)) {
						hostVM = vm;
						break;
					}
				}
			}
		}
		if(hostVM != null)	{
			hostVM.removeComponent(application);	 // free resources inside this method
		}else{
			System.out.println("How come app is running on no virtual machine?");
		}
		
		

    	//C.) Kill VM if not needed anymore (No App running on it)
		VirtualMachine vmToBeStopped = null;
		for (PhysicalMachine pm : physicalMachines) {
			for(Component VMcomp : pm.getComponents()){
				VirtualMachine vm = (VirtualMachine) VMcomp;
				if(vm.getComponents() == null || vm.getComponents().isEmpty()){
					vmToBeStopped = vm;
				}
			}
		}
		
		if(vmToBeStopped != null){
			vmToBeStopped.stop();
			vmToBeStopped.getParent().removeComponent(vmToBeStopped);	//TODO I Think it should be happening inside the stop()
			
			//D.) Kill PM if not needed anymore (No VM running on it) 
			//Do not run this part if no VM were stopped before
			PhysicalMachine pmToBeStopped = null;
			for (PhysicalMachine pm : physicalMachines) {
				if(pm.getComponents() == null || pm.getComponents().isEmpty()){
					pmToBeStopped = pm;
				}
			}
			
			if(pmToBeStopped != null){
				pmToBeStopped.stop();
				this.physicalMachines.remove(pmToBeStopped);
			}
			
		}else{
			//do nothing
		}
    	
        //Finally: Log current clould utilization details to output file 2
    }
	
	private PhysicalMachine selectOptimalPM(Integer neededRam, Integer neededHddSize, Integer neededCpuInMHz) throws SchedulingNotPossibleException {
		
		if(this.physicalMachines == null){
			this.physicalMachines = new ArrayList<PhysicalMachine>();
			PhysicalMachine pm = createNewPM();
			this.physicalMachines.add(pm);
			pm.start(); //TODO start method is empty --> Count Initial Power Consumption there?
			overallInfo.setTotalPMs(overallInfo.getTotalPMs()+1);
			return pm;
		}else{
			//iterate over PMList give back first possible
			//TODO find more clever solution
			for (PhysicalMachine pm : this.physicalMachines) {
				if(	pm.getCpuAvailable() >= neededCpuInMHz &&
					pm.getRamAvailable() >= neededRam &&
					pm.getHddAvailable() >= neededHddSize){
				
					return pm;
				}
			}
			
			//list iterated and no pm could give back -> start new pm
			PhysicalMachine pm = createNewPM();
			this.physicalMachines.add(pm);
			pm.start();
			overallInfo.setTotalPMs(overallInfo.getTotalPMs()+1);
			return pm;
			
		}

	}

	
	private PhysicalMachine createNewPM() throws SchedulingNotPossibleException {
		if(this.physicalMachines.size() < maxPMs){
			return new PhysicalMachineImpl();
		}else{
			throw new SchedulingNotPossibleException();
		}
	}

	private void writeLog() {
		int timestamp;
		int totalRAM = 0;
		int totalCPU = 0;
		int totalSize = 0;
		int runningPMs;
		int runningVMs = 0;
		double totalPowerConsumption = 0;
		int inSourced = 0;		//TODO
		int outSourced = 0;		//TODO
		
		timestamp = (int) internalTime;
		runningPMs = this.physicalMachines.size();
		for(Machine pm : this.physicalMachines){
			totalRAM += pm.getRamAvailable();
			totalCPU += pm.getCpuAvailable();
			totalSize += pm.getHddAvailable();
			runningVMs += pm.getComponents().size();
			//this consumption is the overall powerconsuption of the cloud in the moment
			totalPowerConsumption += pm.getPowerConsumption();
		}
		
		CloudStateInfo info = new CloudStateInfo(timestamp, totalRAM, totalCPU, totalSize, runningPMs, runningVMs, totalPowerConsumption, inSourced, outSourced);
		this.updatePowerConsumption(lastTotalCosumption);
		lastTotalCosumption = totalPowerConsumption;
		this.writer.writeCsv(info);
	}
	
	
	
	private void updatePowerConsumption(double lastTotalCosumption) {
		//total consumption after the previous event * time interval between last and new event in seconds
		this.overallInfo.setTotalPowerConsumption(lastTotalCosumption*(lastinternalTime/1000));
		
	}
	

	@Override
	public void finalize(){
		
		this.writer.close();
	}
	
	@Override
	public CloudOverallInfo getOverAllInfo(){
		overallInfo.setScheduler(this.getClass().getName());
		overallInfo.setTotalDuration(internalTime);
		return this.overallInfo;
	}

	@Override
	public void setMaxNumberOfPhysicalMachines(int nr) {
		this.maxPMs = nr;
		
	}

	private Event getNextStopEvent(Event event) {
		for(Event e : events){
			if(e.getEventTime() > event.getEventTime() && e.getEventType().equals(EventType.STOP) && applications.contains(e.getApplication()) && !e.isToBeSkipped()){
				return e;
			}
		}
		System.out.println("Cloud is full and no App can be stopped.");
		return null;
	}

}
