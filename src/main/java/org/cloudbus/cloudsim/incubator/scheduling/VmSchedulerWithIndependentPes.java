package org.cloudbus.cloudsim.incubator.scheduling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;

/**
 * A VmScheduler which associates each VM with one or several PEs. IT allocates
 * resources for a VM only from its specified PEs. For the purpose we have a
 * seperate {@link VmScheduler} for each Pe.
 * 
 * <br>
 * <br>
 * TODO - ideally this class should extend {@link VmScheduler}, but this way, it
 * would "abandon" some of its member variables and thus will subtype
 * improperly. Consider introducing a new interface IVmScheduler common to both
 * {@link VmScheduler} and this class.
 * 
 * @author nikolay.grozev
 * 
 */
public abstract class VmSchedulerWithIndependentPes<P extends Pe, V extends Vm> {

    private final LinkedHashMap<P, VmScheduler> peIdsToSchedulers = new LinkedHashMap<>();

    public VmSchedulerWithIndependentPes(final List<P> pelist) {
	for (P pe : pelist) {
	    peIdsToSchedulers.put(pe, createSchedulerFroPe(pe));
	}
    }

    public List<Double> getAllocatedMipsForVm(final V vm) {
	List<Double> result = new ArrayList<>();
	for (Map.Entry<P, VmScheduler> entry : peIdsToSchedulers.entrySet()) {
	    P pe = entry.getKey();
	    VmScheduler scheduler = entry.getValue();
	    if (doesVmUse(vm, pe)) {
		List<Double> alloc = scheduler.getAllocatedMipsForVm(vm);
		result.add(alloc.get(0));
	    } else {
		result.add(0.0);
	    }
	}

	return result;
    }

    public boolean allocatePesForVm(final V vm, final List<Double> mipsShare) {
	boolean result = true;
	int i = 0;
	for (Map.Entry<P, VmScheduler> entry : peIdsToSchedulers.entrySet()) {
	    P pe = entry.getKey();
	    VmScheduler scheduler = entry.getValue();
	    if (doesVmUse(vm, pe)) {
		// Call the scheduler of the i-th Pe with the i-th value from
		// the mipsShare
		result &= scheduler.allocatePesForVm(vm, mipsShare.subList(i, i + 1));
	    }
	    i++;
	}
	return result;
    }

    public void deallocatePesForVm(final V vm) {
	for (Map.Entry<P, VmScheduler> entry : peIdsToSchedulers.entrySet()) {
	    P pe = entry.getKey();
	    VmScheduler scheduler = entry.getValue();
	    if (doesVmUse(vm, pe)) {
		scheduler.deallocatePesForVm(vm);
	    }
	}
    }

    /**
     * A factory method used to create a scheduler for each Pe.
     * 
     * @param pe
     *            - the pe to create a scheduler for.
     * @return - a VM scheduler that manages the parameter Pe.
     */
    protected abstract VmScheduler createSchedulerFroPe(final P pe);

    /**
     * A predicate method, returning f a VM uses a Pe.
     * 
     * @param vm
     *            - the vm to check for,
     * @param pe
     *            - the pe to check for.
     * @return - if the vm uses the Pe.
     */
    protected abstract boolean doesVmUse(final V vm, final Pe pe);

}