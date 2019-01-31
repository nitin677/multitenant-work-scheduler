package com.example.platform.mt.impl;

import com.example.platform.mt.MultiTenantWorkScheduler;
import com.example.platform.mt.vo.TenantAwareTask;
import com.example.platform.mt.vo.TenantConfig;

public class WeightedFairQueueingWorkScheduler extends MultiTenantWorkScheduler {
    @Override
    public void add(TenantAwareTask task) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TenantAwareTask remove() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void provisionTenant(TenantConfig tenantConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deProvisionTenant(String tenantId) {
        throw new UnsupportedOperationException();
    }
}
