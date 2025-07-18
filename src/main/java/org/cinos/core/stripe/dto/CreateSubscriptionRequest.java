package org.cinos.core.stripe.dto;

public class CreateSubscriptionRequest {
    private String planId;
    private boolean trial;
    private String successUrl;
    private String cancelUrl;

    public CreateSubscriptionRequest() {}

    public CreateSubscriptionRequest(String planId) {
        this.planId = planId;
    }

    public CreateSubscriptionRequest(String planId, String successUrl, String cancelUrl) {
        this.planId = planId;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public boolean isTrial() {
        return trial;
    }

    public void setTrial(boolean trial) {
        this.trial = trial;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
}