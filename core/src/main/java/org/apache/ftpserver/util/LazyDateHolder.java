package org.apache.ftpserver.util;

import java.util.Date;

/**
 * A thread-safe holder for a timestamp that lazily creates a corresponding {@link Date} object.
 * <p>
 * This class is designed to optimize scenarios where the timestamp is frequently updated
 * (such as during file transfers), but the {@code Date} object is rarely accessed.
 * A new {@code Date} instance is created only when the timestamp value changes,
 * minimizing unnecessary object allocations and reducing garbage collection pressure.
 * </p>
 *
 * <p>
 * All methods that access or modify the cached {@code Date} are properly synchronized
 * to ensure atomic visibility of the timestamp and associated {@code Date}.
 * </p>
 */
public final class LazyDateHolder {

    private long timeMillis;

    private Date cachedDate;

    /**
     * Creates a new {@code LazyDateHolder} initialized with the specified time in milliseconds.
     *
     * @param initialTimeMillis
     *         the initial timestamp value in milliseconds since the epoch
     */
    public LazyDateHolder(long initialTimeMillis) {
        this.timeMillis = initialTimeMillis;
        this.cachedDate = new Date(initialTimeMillis);
    }

    /**
     * Creates a new {@code LazyDateHolder} initialized with the current system time.
     */
    public LazyDateHolder() {
        this(System.currentTimeMillis());
    }

    /**
     * Updates the timestamp to the specified value in milliseconds since the epoch.
     * <p>
     * If the new timestamp differs from the current one, the cached {@code Date} object is refreshed.
     * </p>
     *
     * @param newTimeMillis
     *         the new timestamp value in milliseconds
     */
    public synchronized void update(long newTimeMillis) {
        if (this.timeMillis != newTimeMillis) {
            this.timeMillis = newTimeMillis;
            this.cachedDate = new Date(newTimeMillis);
        }
    }

    /**
     * Updates the timestamp to the current system time.
     * <p>
     * If the new timestamp differs from the current one, the cached {@code Date} object is refreshed.
     * </p>
     */
    public void update() {
        update(System.currentTimeMillis());
    }

    /**
     * Returns the cached {@code Date} instance representing the current timestamp.
     * <p>
     * This {@code Date} object is updated lazily and only changes when the timestamp value changes.
     * </p>
     *
     * @return the cached {@code Date} corresponding to the last update time
     */
    public synchronized Date getDate() {
        return cachedDate;
    }

    /**
     * Returns the current timestamp value in milliseconds since the epoch.
     *
     * @return the current timestamp in milliseconds
     */
    public long getTimeMillis() {
        return timeMillis;
    }
}
