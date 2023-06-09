package com.marcnuri.yakd.quickstarts.dashboard.watch;

public record WatchEvent<T>(WatchEvent.Type type, T object) {
  public enum Type {
    ADDED, MODIFIED, DELETED, ERROR
  }
}
