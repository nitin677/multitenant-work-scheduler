package com.example.platform.mt.vo;

import java.util.Objects;

/**
 * Tenant aware task which wraps the actual task and stores tenant id.
 */
public class TenantAwareTask implements Runnable {
    private String tenantId;
    private AbstractTask task;

    public String getTenantId() {
        return tenantId;
    }

    public AbstractTask getTask() {
        return task;
    }

    public TenantAwareTask(String tenantId, AbstractTask task) {
        this.task = task;
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "Tenant: "+tenantId+", "+task.getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TenantAwareTask))
            return false;
        TenantAwareTask other = (TenantAwareTask)obj;
        return other.tenantId == this.tenantId && other.task.equals(this.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, task);
    }

    @Override
    public void run() {
        task.run();
    }
}
