package com.tencent.timi.annualparty.controller.response;

public class PickedResponse {
    private boolean isPickedUp;

    private String message;

    public boolean isPickedUp() {
        return isPickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        isPickedUp = pickedUp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
