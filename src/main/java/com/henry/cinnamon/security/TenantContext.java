package com.henry.cinnamon.security;

public class TenantContext {


    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public TenantContext(){

    }

    public static void set(String tenantId){
        CURRENT_TENANT.set(tenantId);
    }

    public static String get(){
        String tenantId = CURRENT_TENANT.get();
        if(tenantId==null){
            throw new IllegalStateException("No tenant context found for current request");
        }
        return tenantId;
    }

    public static void clear(){
        CURRENT_TENANT.remove();
    }
}
