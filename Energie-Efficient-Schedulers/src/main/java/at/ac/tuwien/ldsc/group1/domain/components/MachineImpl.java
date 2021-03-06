package at.ac.tuwien.ldsc.group1.domain.components;

import at.ac.tuwien.ldsc.group1.domain.exceptions.ResourceUnavailableException;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class MachineImpl implements Machine, Composite {
    /**
     * A default component has a power consumption of 0.3 Watt per MHz,
     * concrete implementations of Component are allowed to
     */
    private static final double DEFAULT_POWER_PER_MHZ = 0.3;
    public final double CURRENT_POWER_PER_MHZ = POWER_PER_MHZ;
    public static double POWER_PER_MHZ = DEFAULT_POWER_PER_MHZ;
    public static final int MS_PER_SECOND = 1000;

    //Base resources must be set when the machine is instantiated
    //they represent the requirements for the OS.
    private int hddBase;
    private int ramBase;
    private int cpuInMhzBase;

    //The maximum resources that are available to this machine
    //they are based on the resources that the underlying hardware
    //can provide
    protected int hddMax;
    protected int cpuInMhzMax;
    protected int ramMax;
    static final int initialConsumption;
    static final double consumptionPerMHz;
    
	Integer overprovidedCpuInMhz = 0;
    Integer overprovidedRam = 0;
    
    /**
     * A list of components that are managed by this machine
     */
    private List<Component> components = new ArrayList<>();
    protected Machine parent = null;

    static{ 
		ResourceBundle res = ResourceBundle.getBundle("physicalMachine");
		initialConsumption = Integer.parseInt(res.getString("initialConsumption"));
		consumptionPerMHz = Double.parseDouble(res.getString("consumptionPerMHz"));
	}
    
    //Constructors
    public MachineImpl(
            int ramBase, int hddBase, int cpuInMhzBase,
            int ramMax, int hddMax, int cpuInMhzMax)
    {
        this.ramBase = ramBase;
        this.hddBase = hddBase;
        this.cpuInMhzBase = cpuInMhzBase;
        this.ramMax = ramMax;
        this.hddMax = hddMax;
        this.cpuInMhzMax = cpuInMhzMax;
    }
    
    public MachineImpl (
            int ramBase, int hddBase, int cpuInMhzBase,
            Machine parent) throws ResourceUnavailableException
    {
        this.ramBase = ramBase;
        this.hddBase = hddBase;
        this.cpuInMhzBase = cpuInMhzBase;
        this.parent = parent;
        parent.addComponent(this);
    }

    //Methods
    @Override
    public int getRam() {
        int ram = ramBase;
        for(Component c : components) {
            ram += c.getRam();
        }
        return ram;
    }

    @Override
    public int getHddSize() {
        int hddSize = hddBase;
        for(Component c : components) {
            hddSize += c.getHddSize();
        }
        return hddSize;
    }

    @Override
    public int getCpuInMhz() {
        int cpuInMhz = cpuInMhzBase;
        for(Component c : components) {
            cpuInMhz += c.getCpuInMhz();
        }
        return cpuInMhz;
    }

    @Override
    public double getPowerConsumption() {
        double powerConsumption = 0;
        if (parent != null) {
        	powerConsumption += parent.getPowerConsumption();
        } else {
    		powerConsumption += initialConsumption + (this.getCpuInMhz() * consumptionPerMHz);
        }
        return powerConsumption;
    }

    @Override
    public void start() {
        //TODO: Use log4j
        System.out.println("Started a machine");
    }

    @Override
    public void stop() {
        //TODO: Use log4j
        System.out.println("Stopped a machine");
    }

    @Override
    public void addComponent(Component component) throws ResourceUnavailableException {
        components.add(component);
    }

    @Override
    public void removeComponent(Component component) {
        components.remove(component);
    }

    @Override
    public List<Component> getComponents() {
        return components;
    }

    public int getHddBase() {
        return hddBase;
    }

    public int getRamBase() {
        return ramBase;
    }

    public int getCpuInMhzBase() {
        return cpuInMhzBase;
    }

    @Override
    public int getRamAvailable() {
        if(parent != null) {
            //return parent.getRamMax() - parent.getRamAvailable();
            return parent.getRamAvailable();
        } else {
            return ramMax - getRam();
        }
    }

    @Override
    public int getCpuAvailable() {
        if (parent != null) {
            // When Machine is child, get value from parent PM
            return parent.getCpuAvailable();
        } else {
            // When Machine is a parent PM, return available CPU for all VMs
            return cpuInMhzMax - getCpuInMhz();
        }
    }

    @Override
    public int getHddAvailable() {
        if (parent != null) {
            // When Machine is child, get value from parent PM
            return parent.getHddAvailable();
        } else {
            // When Machine is a parent PM, return available HDD for all VMs
            return hddMax - getHddSize();
        }
    }

    @Override
    public Machine getParent() {
        return parent;
    }
    
    protected void setParent(Machine parent) {
        this.parent = parent;
    }
    
    public Integer getOverprovidedCpuInMhz() {
        return overprovidedCpuInMhz;
    }

    public void setOverprovidedCpuInMhz(Integer overprovidedCpuInMhz) {
        // we swap the values
        this.overprovidedCpuInMhz = cpuInMhzMax;
        cpuInMhzMax= overprovidedCpuInMhz ;
    }
    
    public void revertOverprovidedCpuInMhz() {
        // we swap the values
        cpuInMhzMax = this.overprovidedCpuInMhz ;
    }

    public Integer getOverprovidedRam() {
        return overprovidedRam;
    }

    public void setOverprovidedRam(Integer overprovidedRam) {
        this.overprovidedRam = ramMax;
        ramMax = overprovidedRam;
    }
    
    public void revertOverprovidedRam() {
        ramMax = this.overprovidedRam;
    }
    
   }
