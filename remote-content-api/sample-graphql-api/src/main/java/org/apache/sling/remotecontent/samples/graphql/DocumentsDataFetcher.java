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

package org.apache.sling.remotecontent.samples.graphql;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.graphql.api.SlingDataFetcher;
import org.apache.sling.graphql.api.SlingDataFetcherEnvironment;
import org.apache.sling.graphql.api.pagination.Cursor;
import org.apache.sling.graphql.helpers.GenericConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;

@Component(service = SlingDataFetcher.class, property = {"name=samples/documents"})
public class DocumentsDataFetcher extends DocumentDataFetcher {

    @Override
    public @Nullable Object get(@NotNull SlingDataFetcherEnvironment e) throws Exception {
        final int limit = e.getArgument("limit", 5);
        final String after = e.getArgument("after", null);

        // Use a suffix as we might not keep these built-in language in the long term
        final String langSuffix = "2020";

        String lang = e.getArgument("lang", "xpath" + langSuffix);
        if(!lang.endsWith(langSuffix)) {
            throw new RuntimeException("Query langage must end with suffix " + langSuffix);
        }
        lang = lang.replaceAll(langSuffix + "$", "");
        final String query = e.getArgument("query");

        final Iterator<Resource> resultIterator = e.getCurrentResource().getResourceResolver().findResources(query, lang);
        final Function<Map<String, Object>, String> cursorStringProvider = data -> (String)data.get("path");
        final Function<Resource, Map<String, Object>> converter = this::toDocument;
        final Iterator<Map<String, Object>> it = new ConvertingIterator<>(resultIterator, converter);
        return new GenericConnection.Builder<>(it, cursorStringProvider)
            .withStartAfter(Cursor.fromEncodedString(after))
            .withLimit(limit)
            .build();
    }
    
}
