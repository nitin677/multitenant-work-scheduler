package com.example.platform.mt.exception;

public class UnknownTenantException extends Exception {
    public UnknownTenantException(String tenantId) {
        super("Unknown Tenant with tenant id: "+tenantId);
    }
}
