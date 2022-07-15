package com.alpdroid.huGen10;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Alpdroid {

  public abstract Track track();

  public abstract int timestamp();

  public abstract AlpdroidStatus status();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_Alpdroid.Builder().status(new AlpdroidStatus(0));
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder track(Track track);

    public abstract Builder timestamp(int timestamp);

    public abstract Builder status(AlpdroidStatus status);

    public abstract Alpdroid build();
  }
}
