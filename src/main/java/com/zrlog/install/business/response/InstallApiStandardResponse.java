package com.zrlog.install.business.response;

class InstallApiStandardResponse {

    private int error;
    private String message = "";

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}