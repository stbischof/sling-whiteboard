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

import org.osgi.service.feature.FeatureArtifact;
import org.osgi.service.feature.ID;

import java.util.Objects;

class ArtifactImpl implements FeatureArtifact {
    private final ID id;

    ArtifactImpl(ID id) {
        this.id = id;
    }

    @Override
    public ID getID() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ArtifactImpl))
            return false;
        ArtifactImpl other = (ArtifactImpl) obj;
        return Objects.equals(id, other.id);
    }
}
