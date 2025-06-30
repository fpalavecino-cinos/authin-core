package org.cinos.core.stripe.dto;

public class CreateSubscriptionRequest {
    private String planId;
    private boolean trial;

    public CreateSubscriptionRequest() {}

    public CreateSubscriptionRequest(String planId) {
        this.planId = planId;
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
}