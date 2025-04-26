package org.apache.ftpserver.util;

import junit.framework.TestCase;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for {@link LazyDateHolder}.
 */
public class LazyDateHolderTest extends TestCase {

    public void testInitialTimeFromConstructor() {
        long now = System.currentTimeMillis();
        LazyDateHolder holder = new LazyDateHolder(now);
        assertEquals("Initial timeMillis should match provided value", now, holder.getTimeMillis());
        assertEquals("Initial Date should match provided value", now, holder.getDate().getTime());
    }

    public void testInitialTimeFromNoArgConstructor() {
        LazyDateHolder holder = new LazyDateHolder();
        long now = System.currentTimeMillis();
        long diff = Math.abs(holder.getTimeMillis() - now);
        assertTrue("Initial timeMillis should be close to current time", diff < 1000);
    }

    public void testUpdateWithSameTimeDoesNotChangeDate() {
        long now = System.currentTimeMillis();
        LazyDateHolder holder = new LazyDateHolder(now);
        Date firstDate = holder.getDate();

        holder.update(now); // update with same millis
        Date secondDate = holder.getDate();

        assertSame("Date instance should not change if timeMillis is the same", firstDate, secondDate);
    }

    public void testUpdateWithDifferentTimeChangesDate() {
        long now = System.currentTimeMillis();
        LazyDateHolder holder = new LazyDateHolder(now);
        Date firstDate = holder.getDate();

        holder.update(now + 1000); // advance by 1 second
        Date secondDate = holder.getDate();

        assertNotSame("Date instance should change when timeMillis changes", firstDate, secondDate);
        assertEquals("Date should reflect updated timeMillis", now + 1000, secondDate.getTime());
    }

    public void testUpdateWithoutArgumentUsesCurrentTime() throws InterruptedException {
        LazyDateHolder holder = new LazyDateHolder();
        Date firstDate = holder.getDate();
        Thread.sleep(5); // ensure time passes
        holder.update(); // auto update
        Date secondDate = holder.getDate();

        assertTrue("Date after auto update should be newer", secondDate.getTime() > firstDate.getTime());
    }

    public void testConcurrentUpdateAndGet() throws InterruptedException {
        final LazyDateHolder holder = new LazyDateHolder();
        final AtomicReference<Exception> failure = new AtomicReference<>(null);
        final CountDownLatch latch = new CountDownLatch(2);

        Runnable updater = new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 1000; i++) {
                        holder.update();
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    failure.set(e);
                } finally {
                    latch.countDown();
                }
            }
        };

        Runnable getter = new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 1000; i++) {
                        Date date = holder.getDate();
                        assertNotNull("Date should never be null during concurrent access", date);
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    failure.set(e);
                } finally {
                    latch.countDown();
                }
            }
        };

        Thread updateThread = new Thread(updater);
        Thread getThread = new Thread(getter);
        updateThread.start();
        getThread.start();

        latch.await(); // wait for both threads to finish

        if (failure.get() != null) {
            fail("Exception occurred during concurrent test: " + failure.get().getMessage());
        }
    }
}
