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
package org.apache.sling.scripting.graphql.it;

import javax.inject.Inject;

import org.apache.sling.resource.presence.ResourcePresence;
import org.apache.sling.scripting.gql.api.DataFetcherFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.factoryConfiguration;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class GraphQLServletIT extends GraphQLScriptingTestSupport {

    @Inject
    @Filter(value = "(path=/apps/graphql/test/one/json.gql)")
    private ResourcePresence resourcePresence;

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] configuration() {
        return new Option[]{
            baseConfiguration(),
            factoryConfiguration("org.apache.sling.resource.presence.internal.ResourcePresenter")
                .put("path", "/apps/graphql/test/one/json.gql")
                .asOption(),
        };
    }

    @Test
    public void testJsonContent() throws Exception {
        PipeDataFetcherFactory pipeDataFetcherFactory = new PipeDataFetcherFactory();
        ServiceRegistration<DataFetcherFactory> dataFetcherFactoryRegistration =
                bundleContext.registerService(DataFetcherFactory.class, pipeDataFetcherFactory, null);

        try {
            final String path = "/graphql/one";
            final String json = getContent(path + ".gql");
            // TODO we should really parse this..or run detailed tests in unit tests, and just the basics here
            final String expected =
                    "{\"currentResource\":{\"path\":\"/content/graphql/one\",\"resourceType\":\"graphql/test/one\"}}";
            assertEquals(expected, json);

        } finally {
            dataFetcherFactoryRegistration.unregister();
        }
    }
}
