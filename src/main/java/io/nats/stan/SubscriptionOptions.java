/*******************************************************************************
 * Copyright (c) 2015-2016 Apcera Inc. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the MIT License (MIT) which accompanies this
 * distribution, and is available at http://opensource.org/licenses/MIT
 *******************************************************************************/

package io.nats.stan;

import io.nats.stan.protobuf.StartPosition;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SubscriptionOptions {
    // DurableName, if set will survive client restarts.
    String durableName;
    // Controls the number of messages the cluster will have inflight without an ACK.
    int maxInFlight;
    // Controls the time the cluster will wait for an ACK for a given message.
    Duration ackWait;
    // StartPosition enum from proto.
    StartPosition startAt;
    // Optional start sequence number.
    long startSequence;
    // Optional start time in nanoseconds since the UNIX epoch.
    long startTime;
    // Option to do Manual Acks
    boolean manualAcks;

    Date startTimeAsDate;

    private SubscriptionOptions(Builder builder) {
        this.durableName = builder.durableName;
        this.maxInFlight = builder.maxInFlight;
        this.ackWait = builder.ackWait;
        this.startAt = builder.startAt;
        this.startSequence = builder.startSequence;
        this.startTime = builder.startTime;
        this.manualAcks = builder.manualAcks;
    }

    /**
     * Returns the name of the durable subscriber.
     * 
     * @return the name of the durable subscriber
     */
    public String getDurableName() {
        return durableName;
    }

    /**
     * Returns the maximum number of messages the cluster will send without an ACK.
     * 
     * @return the maximum number of messages the cluster will send without an ACK
     */
    public int getMaxInFlight() {
        return maxInFlight;
    }

    /**
     * Returns the timeout for waiting for an ACK from the cluster's point of view for delivered
     * messages.
     * 
     * @return the timeout for waiting for an ACK from the cluster's point of view for delivered
     *         messages
     */
    public Duration getAckWait() {
        return ackWait;
    }

    /**
     * Returns the desired start position for the message stream.
     * 
     * @return the desired start position for the message stream
     */
    public StartPosition getStartAt() {
        return startAt;
    }

    /**
     * Returns the desired start sequence position.
     * 
     * @return the desired start sequence position
     */
    public long getStartSequence() {
        return startSequence;
    }

    /**
     * Returns the desired start time position.
     * 
     * @return the desired start time position
     */
    public Date getStartTime() {
        if (startTimeAsDate == null) {
            startTimeAsDate = new Date(getStartTime(TimeUnit.MILLISECONDS));
        }
        return startTimeAsDate;
    }

    /**
     * Returns the desired start time position in the requested units.
     * 
     * @param unit the unit of time
     * @return the desired start time position
     */
    public long getStartTime(TimeUnit unit) {
        return unit.convert(startTime, TimeUnit.NANOSECONDS);
    }

    /**
     * Returns whether or not messages for this subscription must be acknowledged individually by
     * calling {@link Message#ack()}.
     * 
     * @return whether or not manual acks are required for this subscription.
     * 
     */
    public boolean isManualAcks() {
        return manualAcks;
    }

    public static final class Builder {
        String durableName;
        int maxInFlight = SubscriptionImpl.DEFAULT_MAX_IN_FLIGHT;
        Duration ackWait = Duration.ofMillis(SubscriptionImpl.DEFAULT_ACK_WAIT);
        StartPosition startAt;
        long startSequence;
        long startTime;
        boolean manualAcks;
        Date startTimeAsDate;

        /**
         * Sets the durable subscriber name for the subscription.
         * 
         * @param durableName the name of the durable subscriber
         * @return this
         */
        public Builder setDurableName(String durableName) {
            this.durableName = durableName;
            return this;
        }

        /**
         * Sets the maximum number of in-flight (unacknowledged) messages for the subscription.
         * 
         * @param maxInFlight the maximum number of in-flight messages
         * @return this
         */
        public Builder setMaxInFlight(int maxInFlight) {
            this.maxInFlight = maxInFlight;
            return this;
        }

        /**
         * Sets the amount of time the subscription will wait for ACKs from the cluster.
         * 
         * @param ackWait the amount of time the subscription will wait for an ACK from the cluster
         * @return this
         */
        public Builder setAckWait(Duration ackWait) {
            this.ackWait = ackWait;
            return this;
        }

        /**
         * Sets the amount of time the subscription will wait for ACKs from the cluster.
         * 
         * @param ackWait the amount of time the subscription will wait for an ACK from the cluster
         * @param unit the time unit
         * @return this
         */
        public Builder setAckWait(long ackWait, TimeUnit unit) {
            this.ackWait = Duration.ofMillis(unit.toMillis(ackWait));
            return this;
        }

        /**
         * Sets whether or not messages must be acknowledge individually by calling
         * {@link Message#ack()}.
         * 
         * @param manualAcks whether or not messages must be manually acknowledged
         * @return this
         */
        public Builder setManualAcks(boolean manualAcks) {
            this.manualAcks = manualAcks;
            return this;
        }

        /**
         * Specifies the sequence number from which to start receiving messages.
         * 
         * @param seq the sequence number from which to start receiving messages
         * @return this
         */
        public Builder startAtSequence(long seq) {
            this.startAt = StartPosition.SequenceStart;
            this.startSequence = seq;
            return this;
        }

        /**
         * Specifies the desired start time position using {@code java.util.Date}.
         * 
         * @param start the desired start time position expressed as a {@code java.util.Date}
         * @return this
         */
        public Builder startAtTime(Date start) {
            this.startAt = StartPosition.TimeDeltaStart;
            this.startTime = TimeUnit.MILLISECONDS.toNanos(start.getTime());
            return this;
        }

        /**
         * Specifies the desired delta start time position in the desired unit.
         * 
         * @param ago the historical time delta (from now) from which to start receiving messages
         * @param unit the time unit
         * @return this
         */
        public Builder startAtTimeDelta(long ago, TimeUnit unit) {
            this.startAt = StartPosition.TimeDeltaStart;
            this.startTime =
                    TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() - unit.toMillis(ago));
            return this;
        }

        /**
         * Specifies that message delivery should start with the last (most recent) message stored
         * for this subject.
         * 
         * @return this
         */
        public Builder startWithLastReceived() {
            this.startAt = StartPosition.LastReceived;
            return this;
        }

        /**
         * Specifies that message delivery should begin at the oldest available message for this
         * subject.
         * 
         * @return this
         */
        public Builder deliverAllAvailable() {
            this.startAt = StartPosition.First;
            return this;
        }

        /**
         * Creates a {@link SubscriptionOptions} instance based on the current configuration.
         * 
         * @return the created {@link SubscriptionOptions} instance
         */
        public SubscriptionOptions build() {
            return new SubscriptionOptions(this);
        }
    }
}

