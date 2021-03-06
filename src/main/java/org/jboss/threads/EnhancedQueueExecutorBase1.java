/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.threads;

import static org.jboss.threads.JBossExecutors.unsafe;

import org.wildfly.common.annotation.NotNull;

/**
 * EQE base class: tail section.
 */
abstract class EnhancedQueueExecutorBase1 extends EnhancedQueueExecutorBase0 {

    static final long tailOffset;

    static {
        try {
            tailOffset = unsafe.objectFieldOffset(EnhancedQueueExecutorBase1.class.getDeclaredField("tail"));
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    // =======================================================
    // Locks
    // =======================================================

    /**
     * Establish a combined head/tail lock.
     */
    static final boolean COMBINED_LOCK = readBooleanPropertyPrefixed("combined-lock", false);

    /**
     * Use a spin lock for the tail lock.
     */
    static final boolean TAIL_SPIN = ! COMBINED_LOCK && readBooleanPropertyPrefixed("tail-spin", false);

    /**
     * Attempt to lock frequently-contended operations on the list tail.  This defaults to {@code true} because
     * moderate contention among 8 CPUs can result in thousands of spin misses per execution.
     */
    static final boolean TAIL_LOCK = COMBINED_LOCK || readBooleanPropertyPrefixed("tail-lock", true);

    // =======================================================
    // Current state fields
    // =======================================================

    /**
     * The node <em>preceding</em> the tail node; this field is not {@code null}.  This
     * is the insertion point for tasks (and the removal point for waiting threads).
     */
    @NotNull
    @SuppressWarnings("unused") // used by field updater
    volatile EnhancedQueueExecutor.TaskNode tail;

    EnhancedQueueExecutorBase1() {}

    // =======================================================
    // Compare-and-set operations
    // =======================================================

    void compareAndSetTail(final EnhancedQueueExecutor.TaskNode expect, final EnhancedQueueExecutor.TaskNode update) {
        unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }
}
