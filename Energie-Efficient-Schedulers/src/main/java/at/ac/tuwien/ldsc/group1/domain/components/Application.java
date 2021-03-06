package at.ac.tuwien.ldsc.group1.domain.components;

/**
 * An interface for an application that runs in a virtual machine.
 * Based on the assignment an application is always created with fixed
 * size, ram and MHz requirements. Therefore this inteface is read-only.
 *
 * @author Sebastian Geiger
 */
public interface Application extends Component {
    /**
     * Returns the duration in [ms] that the application needs to run.
     */
    long getDuration();
    /**
     * Returns the timeStamp in [ms] when the application start is requested.
     */
    long getTimeStamp();

    boolean isOutSourced();

    void setIsOutSourced(boolean isOutSourced);

    void setIsInSourced(boolean b);

    boolean isInSourced();
}
