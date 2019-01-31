package com.example.platform.mt;

import com.example.platform.mt.exception.UnknownTenantException;
import com.example.platform.mt.impl.FairQueueingWorkScheduler;
import com.example.platform.mt.impl.WeightedFairQueueingWorkScheduler;
import com.example.platform.mt.vo.SchedulingStrategy;
import com.example.platform.mt.vo.TenantAwareTask;
import com.example.platform.mt.vo.TenantConfig;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A multi-tenant Work queue manager implementation which serves multiple tenants.
 * It allows multiple tenants to submit their works/tasks. And when worker threads (responsible
 * for processing the tasks) take the tasks from this queue manager, it gives out the tasks
 * prioritized based on the employed {@link SchedulingStrategy}.
 */
public class MultiTenantWorkQueueManager {
    private final Map<String, BlockingQueue<TenantAwareTask>> tenantWorkQueues;
    private MultiTenantWorkScheduler scheduler;
    private AtomicInteger noOfTenants;
    List<TenantConfig> tenants;
    private AtomicInteger tasksProcessed = new AtomicInteger(0);

    /**
     * Initializes the multi-tenant work queue manager.
     * @param tenantConfigs List of tenant configurations for the tenants served by this queue manager.
     * @param strategy Work scheduling strategy, supported strategies - {@link SchedulingStrategy}
     */
    public MultiTenantWorkQueueManager(List<TenantConfig> tenantConfigs, SchedulingStrategy strategy) {
        tenants = tenantConfigs;
        tenantWorkQueues = new ConcurrentHashMap<>(tenants.size()*2);
        scheduler = MultiTenantWorkSchedulerFactory.getScheduler(strategy);
        for (TenantConfig tenantCfg : tenants) {
            tenantWorkQueues.put(tenantCfg.getTenantId(), new LinkedBlockingQueue(scheduler.getEffectiveCapacity(tenantCfg.getWorkCapacity())));
        }
        this.noOfTenants = new AtomicInteger(tenants.size());
        scheduler.initialize(tenantWorkQueues);
    }

    /**
     * Provision a new tenant to the multi-tenant work queue manager.
     * @param tenantConfig Tenant configuration for the new tenant.
     */
    public void provisionTenant(TenantConfig tenantConfig) {
        tenantWorkQueues.put(tenantConfig.getTenantId(),
            new LinkedBlockingQueue<>(tenantConfig.getWorkCapacity()));
        scheduler.provisionTenant(tenantConfig);
        noOfTenants.incrementAndGet();
    }

    /**
     * De-provision a tenant from the multi-tenant work queue manager.
     * @param tenantId tenant id of the tenant to be removed.
     */
    public void deProvisionTenant(String tenantId) {
        tenantWorkQueues.remove(tenantId);
        scheduler.deProvisionTenant(tenantId);
        noOfTenants.decrementAndGet();
    }

    /**
     * Submits the specified taskDescription into the appropriate tenant work queue,
     * waiting if necessary for space to become available.
     * @param task Tenant taskDescription to be submitted to the multi-tenant work queue manager.
     * @throws InterruptedException if interrupted while waiting
     */
    public void submitWork(TenantAwareTask task) throws InterruptedException, UnknownTenantException {
        if (!isTenantProvisioned(task.getTenantId()))
            throw new UnknownTenantException(task.getTenantId());
        scheduler.add(task);
    }

    private boolean isTenantProvisioned(String tenantId) {
        return tenantWorkQueues.containsKey(tenantId);
    }

    /**
     * Retrieves the prioritized taskDescription from among multiple tenant queues,
     * waiting if necessary until a taskDescription becomes available.
     * @return Prioritized taskDescription based on the applicable {@link SchedulingStrategy}
     * @throws InterruptedException if interrupted while waiting
     */
    public TenantAwareTask takeWork() throws InterruptedException {
        TenantAwareTask task = scheduler.remove();
        tasksProcessed.getAndIncrement();
        return task;
    }

    /**
     * Returns the current number of tenants served by the multi-tenant work queue.
     * @return the current number of tenants served by the multi-tenant work queue.
     */
    public int getNoOfTenants() {
        return noOfTenants.get();
    }

    public int getProcessedTasksCount() {
        return tasksProcessed.get();
    }

    private static class MultiTenantWorkSchedulerFactory {
        public static MultiTenantWorkScheduler getScheduler(SchedulingStrategy strategy) {
            if (SchedulingStrategy.FAIR_QUEUEING.equals(strategy))
                return new FairQueueingWorkScheduler();
            else if (SchedulingStrategy.WEIGHTED_FAIR_QUEUEING.equals(strategy))
                return new WeightedFairQueueingWorkScheduler();
            return new FairQueueingWorkScheduler();
        }
    }
}
