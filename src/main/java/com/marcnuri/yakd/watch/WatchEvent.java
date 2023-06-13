package com.marcnuri.yakd.watch;

import io.fabric8.kubernetes.client.Watcher;

public record WatchEvent<T>(Watcher.Action type, T object) {
}
