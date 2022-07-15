package com.alpdroid.huGen10;

public class AlpdroidStatus {

  private int errorCode = 0;
  private long dbId = -1;

  public AlpdroidStatus(int errorCode) {
    this.errorCode = errorCode;
  }

  public AlpdroidStatus(int errorCode, long dbId) {
    this(errorCode);
    this.dbId = dbId;
  }

  public boolean isScrobbled() {
    return errorCode == -1;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setScrobbled(boolean scrobbled) {
    errorCode = 0;

    if (scrobbled) {
      errorCode = -1;
    }
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public long getDbId() {
    return dbId;
  }

  public void setDbId(long dbId) {
    this.dbId = dbId;
  }

  public void setFrom(AlpdroidStatus status) {
    this.errorCode = status.errorCode;
    this.dbId = status.dbId;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof AlpdroidStatus)) {
      return false;
    }

    AlpdroidStatus status = (AlpdroidStatus) object;
    return status == this || status.getErrorCode() == errorCode && status.getDbId() == dbId;
  }

  @Override
  public String toString() {
    String value;

    if (isScrobbled()) {
      value = "Scrobbled";
    } else if (errorCode == 0) {
      value = "Pending";
    } else {
      value = "Error " + errorCode;
    }

    return String.format("Status(%s)", value);
  }
}
