/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.feature.osgi.impl;

import org.osgi.service.feature.FeatureBundle;
import org.osgi.service.feature.FeatureBundleBuilder;
import org.osgi.service.feature.ID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class BundleBuilderImpl implements FeatureBundleBuilder {
    private final ID id;

    private final Map<String,Object> metadata = new HashMap<>();

    BundleBuilderImpl(ID id) {
        this.id = id;
    }

    @Override
    public FeatureBundleBuilder addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    @Override
    public FeatureBundleBuilder addMetadata(Map<String,Object> md) {
        this.metadata.putAll(md);
        return this;
    }

    @Override
    public FeatureBundle build() {
        return new BundleImpl(id, metadata);
    }

    private static class BundleImpl extends ArtifactImpl implements FeatureBundle {
        private final Map<String, Object> metadata;

        private BundleImpl(ID id, Map<String, Object> metadata) {
            super(id);

            this.metadata = Collections.unmodifiableMap(metadata);
        }

        @Override
        public Map<String, Object> getMetadata() {
            return metadata;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(metadata);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (!(obj instanceof BundleImpl))
                return false;
            BundleImpl other = (BundleImpl) obj;
            return Objects.equals(metadata, other.metadata);
        }

        @Override
        public String toString() {
            return "BundleImpl [getID()=" + getID() + "]";
        }
    }
}
