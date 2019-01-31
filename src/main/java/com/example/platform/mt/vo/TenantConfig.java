package com.example.platform.mt.vo;

import java.util.Objects;

public class TenantConfig {
    private String tenantId;
    private String tenantName;
    private int weight;
    private int workCapacity;

    public TenantConfig(String tenantId, String tenantName, int workCapacity) {
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.weight = 1;
        this.workCapacity = workCapacity;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, tenantName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TenantConfig))
            return false;
        TenantConfig other = (TenantConfig)obj;
        return other.tenantId.equals(this.tenantId) && other.tenantName.equals(this.tenantName);
    }

    @Override
    public String toString() {
        return tenantId +"-"+ tenantName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWorkCapacity() {
        return workCapacity;
    }
}
