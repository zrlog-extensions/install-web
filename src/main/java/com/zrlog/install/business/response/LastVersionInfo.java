package com.zrlog.install.business.response;

public class LastVersionInfo {

    private String newVersion;
    private String downloadUrl;
    private Boolean latestVersion;
    private String changeLog;

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public Boolean getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(Boolean latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }
}
