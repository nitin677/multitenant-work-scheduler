package com.example.platform.mt;

import com.example.platform.mt.vo.TenantAwareTask;
import com.example.platform.mt.vo.TenantConfig;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Abstract class which represents a Work scheduler for multi-tenant servers/platforms.
 */
public abstract class MultiTenantWorkScheduler {
    protected Map<String, BlockingQueue<TenantAwareTask>> tenantWorkQueues;

    /**
     * Initializes the work scheduler.
     * @param tenantWorkQueues tenant specific work queues.
     */
    public void initialize(Map<String, BlockingQueue<TenantAwareTask>> tenantWorkQueues) {
        this.tenantWorkQueues = tenantWorkQueues;
    }

    /**
     * Adds or schedules the specified taskDescription into the appropriate tenant work queue,
     * waiting if necessary for space to become available.
     * @param task Tenant Task to be added or scheduled.
     * @throws InterruptedException if interrupted while waiting
     */
    public abstract void add(TenantAwareTask task) throws InterruptedException;

    /**
     * Retrieves the prioritized taskDescription from among multiple tenant queues,
     * based on the scheduling algorithm, waiting if necessary until taskDescription becomes available.
     * @return Prioritized taskDescription, based on the scheduling algorithm.
     * @throws InterruptedException if interrupted while waiting
     */
    public abstract TenantAwareTask remove() throws InterruptedException;

    /**
     * Provisions a new tenant to the work scheduler.
     * @param tenantConfig Tenant config for the new tenant.
     */
    public abstract void provisionTenant(TenantConfig tenantConfig);

    /**
     * De-provisions the tenant with specified tenant id.
     * @param tenantId tenant id of the tenant to be de-provisioned.
     */
    public abstract void deProvisionTenant(String tenantId);

    /**
     * Returns the effective capacity of each tenant queue, when the current
     * scheduling algorithm is employed.
     * @param workCapacity original work capacity passed by client.
     * @return effective work capacity when the current scheduling algorithm is employed.
     */
    public int getEffectiveCapacity(int workCapacity) {
        return workCapacity;
    }
}
