/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.sitemap;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.sitemap.SitemapException;
import org.apache.sling.sitemap.builder.Sitemap;
import org.apache.sling.sitemap.generator.ResourceTreeSitemapGenerator;
import org.apache.sling.sitemap.generator.SitemapGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class TestResourceTreeSitemapGenerator extends ResourceTreeSitemapGenerator {

    @Override
    public @NotNull Set<String> getNames(@NotNull Resource sitemapRoot) {
        return Collections.singleton(SitemapGenerator.DEFAULT_SITEMAP);
    }

    @Override
    protected void addResource(String name, Sitemap sitemap, Resource resource) throws SitemapException {
        sitemap.addUrl(resource.getPath());
    }
}
