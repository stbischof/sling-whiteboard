/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.metrics.osgi.impl;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleStartTimeCalculator implements SynchronousBundleListener, Dumpable {

    private Map<Long, StartTime> bundleToStartTime = new HashMap<>();
    private Clock clock = Clock.systemUTC();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void bundleChanged(BundleEvent event) {
        // TODO - ignore framework, ourselves
        
        Bundle bundle = event.getBundle();
        
        System.out.println("Processing Event Type " + event.getType() + " for bundle " + event.getBundle().getBundleId() + "/" + event.getBundle().getSymbolicName());
        
        synchronized (bundleToStartTime) {

            switch (event.getType()) {
                case BundleEvent.STARTING:
                    bundleToStartTime.put(bundle.getBundleId(), new StartTime(bundle.getSymbolicName(), clock.millis()));
                    break;
    
                case BundleEvent.STARTED:
                    StartTime startTime = bundleToStartTime.get(bundle.getBundleId());
                    if ( startTime == null ) {
                        System.out.println("[WARN] No previous data for started bundle " + bundle.getBundleId() + "/" + bundle.getSymbolicName());
                        return;
                    }
                    startTime.started(clock.millis());
                    break;
                
                default: // nothing to do here
                    break;
            }
        }
    }

    @Override
    public void dumpInfo() {
        synchronized (bundleToStartTime) {
            bundleToStartTime.values().stream()
                .forEach( e -> logger.info("Bundle {} has started up in {} milliseconds", e.getBundleSymbolicName() , e.getDuration()));
        }
        
    }
    
    class StartTime {
        private final String bundleSymbolicName;
        private long startingTimestamp;
        private long startedTimestamp;

        public StartTime(String bundleSymbolicName, long startingTimestamp) {
            this.bundleSymbolicName = bundleSymbolicName;
            this.startingTimestamp = startingTimestamp;
        }

        public long getDuration() {
            return startedTimestamp - startingTimestamp;
        }
        
        public String getBundleSymbolicName() {
            return bundleSymbolicName;
        }

        public void started(long startedTimestamp) {
            this.startedTimestamp = startedTimestamp;
        }
    }
}
