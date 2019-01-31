package com.example.platform.mt.impl;

import com.example.platform.mt.MultiTenantWorkScheduler;
import com.example.platform.mt.vo.TenantAwareTask;
import com.example.platform.mt.vo.TenantConfig;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FairQueueingWorkScheduler extends MultiTenantWorkScheduler {
    //fair queue will be ordered in a fair way. i.e., it will contain only one
    //taskDescription per tenant.
    private BlockingQueue<TenantAwareTask> fairQueue;

    private Map<String, AtomicBoolean> tenantHasWork;

    public FairQueueingWorkScheduler() {
        super();
    }

    @Override
    public void initialize(Map<String, BlockingQueue<TenantAwareTask>> tenantWorkQueues) {
        super.initialize(tenantWorkQueues);
        tenantHasWork = new ConcurrentHashMap<>(tenantWorkQueues.size()*2);
        for (String tenantId : tenantWorkQueues.keySet()) {
            tenantHasWork.put(tenantId, new AtomicBoolean(false));
        }
        //We could have bounded it by noOfTenants, but leaving as unbounded
        //in order to support provisioning and de-provisioning of tenants.
        fairQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void add(TenantAwareTask task) throws InterruptedException {
        String tenantId = task.getTenantId();
        boolean tenantHadNoWork = tenantHasWork.get(tenantId).compareAndSet(false, true);
        //if value was swapped, means tenant had no work prior this, so add this taskDescription to fairQueue.
        if (tenantHadNoWork) {
            fairQueue.put(task);
            return;
        }
        tenantWorkQueues.get(tenantId).put(task);
    }

    @Override
    public TenantAwareTask remove() throws InterruptedException {
        TenantAwareTask removed = fairQueue.take();
        //System.out.println(fairQueue);
        //System.out.println(removed.getTenantId());
        String tenantId = removed.getTenantId();
        TenantAwareTask nextTask = tenantWorkQueues.get(tenantId).poll();
        if (nextTask != null) {
            fairQueue.put(nextTask);
        } else {
            tenantHasWork.get(tenantId).set(false);
        }
        return removed;
    }

    @Override
    public void provisionTenant(TenantConfig tenantConfig) {
        tenantHasWork.put(tenantConfig.getTenantId(), new AtomicBoolean(false));
    }

    @Override
    public void deProvisionTenant(String tenantId) {
        tenantHasWork.remove(tenantId);
    }

    @Override
    public int getEffectiveCapacity(int workCapacity) {
        return workCapacity-1;
    }
}
