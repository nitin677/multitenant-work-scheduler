package com.example.platform.mt.impl;

import com.example.platform.mt.MultiTenantWorkQueueManager;
import com.example.platform.mt.vo.TenantAwareTask;

public class MTWorkerThread extends Thread {

    MultiTenantWorkQueueManager queueManager;
    int threadId;

    public MTWorkerThread(MultiTenantWorkQueueManager queueManager, int threadId) {
        this.queueManager = queueManager;
        this.threadId = threadId;
        setName("Multi-tenant worker thread "+threadId);
        System.out.println("Initialized "+getName());
    }

    @Override
    public void run() {
        TenantAwareTask t = null;
        try {
            while ((t = queueManager.takeWork()) != null) {
                //System.out.println("Processing "+t.getTenantId()+"-"+t.getTask().getDescription());
                //this is just to check a testcase that when a new tenant is provisioned and
                //when it submits a task, it doesn't have to wait behind all the previously
                //submitted tasks from other tenants.
                if (t.getTenantId().equals("tenantId:"+60)) {
                    System.out.println("Processed at " + System.currentTimeMillis());
                    System.out.println("Check the difference between submitted time and processed time " +
                        "should not be more than a few ms. This confirms that when a new tenant is provisioned" +
                        " and when it submits a task, it doesn't have to wait behind all the previously " +
                        "submitted tasks from other tenants.");
                }
                //run the actual task
                t.run();
            }
        } catch (InterruptedException e) {
            System.out.println(getName()+" was interrupted while fetching task "+e);
        }
    }
}

