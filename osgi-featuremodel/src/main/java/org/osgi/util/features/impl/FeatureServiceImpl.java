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
package org.osgi.util.features.impl;

import org.osgi.util.features.ID;
import org.osgi.util.features.BuilderFactory;
import org.osgi.util.features.Bundle;
import org.osgi.util.features.BundleBuilder;
import org.osgi.util.features.Configuration;
import org.osgi.util.features.ConfigurationBuilder;
import org.osgi.util.features.Extension;
import org.osgi.util.features.ExtensionBuilder;
import org.osgi.util.features.Feature;
import org.osgi.util.features.FeatureBuilder;
import org.osgi.util.features.FeatureService;
import org.osgi.util.features.MergeContext;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

class FeatureServiceImpl implements FeatureService {
    private final BuilderFactoryImpl builderFactory = new BuilderFactoryImpl();

    @Override
    public BuilderFactory getBuilderFactory() {
        return builderFactory;
    }

    @Override
    public Feature readFeature(Reader jsonReader) throws IOException {
        JsonObject json = Json.createReader(jsonReader).readObject();

        String id = json.getString("id");
        FeatureBuilder builder = builderFactory.newFeatureBuilder(ID.fromMavenID(id));

        builder.setTitle(json.getString("title", null));
        builder.setDescription(json.getString("description", null));
        builder.setVendor(json.getString("vendor", null));
        builder.setLicense(json.getString("license", null));
        builder.setLocation(json.getString("location", null));

        builder.setComplete(json.getBoolean("complete", false));
        builder.setFinal(json.getBoolean("final", false));

        builder.addBundles(getBundles(json));
        builder.addConfigurations(getConfigurations(json));
        builder.addExtensions(getExtensions(json));

        return builder.build();
    }

    private Bundle[] getBundles(JsonObject json) {
        JsonArray ja = json.getJsonArray("bundles");
        if (ja == null)
            return new Bundle[] {};

        List<Bundle> bundles = new ArrayList<>();

        for (JsonValue val : ja) {
            if (val.getValueType() == JsonValue.ValueType.OBJECT) {
                JsonObject jo = val.asJsonObject();
                String bid = jo.getString("id");
                BundleBuilder builder = builderFactory.newBundleBuilder(ID.fromMavenID(bid));

                for (Map.Entry<String, JsonValue> entry : jo.entrySet()) {
                    if (entry.getKey().equals("id"))
                        continue;

                    JsonValue value = entry.getValue();

                    Object v;
                    switch (value.getValueType()) {
                    case NUMBER:
                        v = ((JsonNumber) value).longValueExact();
                        break;
                    case STRING:
                        v = ((JsonString) value).getString();
                        break;
                    default:
                        v = value.toString();
                    }
                    builder.addMetadata(entry.getKey(), v);
                }
                bundles.add(builder.build());
            }
        }

        return bundles.toArray(new Bundle[0]);
    }

    private Configuration[] getConfigurations(JsonObject json) {
        JsonObject jo = json.getJsonObject("configurations");
        if (jo == null)
            return new Configuration[] {};

        List<Configuration> configs = new ArrayList<>();

        for (Map.Entry<String, JsonValue> entry : jo.entrySet()) {

            String p = entry.getKey();
            String factoryPid = null;
            int idx = p.indexOf('~');
            if (idx > 0) {
                factoryPid = p.substring(0, idx);
                p = p.substring(idx + 1);
            }

            ConfigurationBuilder builder;
            if (factoryPid == null) {
                builder = builderFactory.newConfigurationBuilder(p);
            } else {
                builder = builderFactory.newConfigurationBuilder(factoryPid, p);
            }

            JsonObject values = entry.getValue().asJsonObject();
            for (Map.Entry<String, JsonValue> value : values.entrySet()) {
                JsonValue val = value.getValue();

                Object v;
                switch (val.getValueType()) {
                case TRUE:
                    v = true;
                    break;
                case FALSE:
                    v = false;
                    break;
                case NUMBER:
                    v = ((JsonNumber) val).longValueExact();
                    break;
                case STRING:
                    v = ((JsonString) val).getString();
                    break;
                default:
                    v = val.toString();

                    // TODO object types, arrays, and requested type conversions
                }
                builder.addValue(value.getKey(), v);
            }
            configs.add(builder.build());
        }

        return configs.toArray(new Configuration[] {});
    }

    private Extension[] getExtensions(JsonObject json) {
        JsonObject jo = json.getJsonObject("extensions");
        if (jo == null)
            return new Extension[] {};

        List<Extension> extensions = new ArrayList<>();

        for (Map.Entry<String,JsonValue> entry : jo.entrySet()) {
            JsonObject exData = entry.getValue().asJsonObject();
            Extension.Type type;
            if (exData.containsKey("text")) {
                type = Extension.Type.TEXT;
            } else if (exData.containsKey("artifacts")) {
                type = Extension.Type.ARTIFACTS;
            } else if (exData.containsKey("json")) {
                type = Extension.Type.JSON;
            } else {
                throw new IllegalStateException("Invalid extension: " + entry);
            }
            String k = exData.getString("kind", "optional");
            Extension.Kind kind = Extension.Kind.valueOf(k.toUpperCase());

            ExtensionBuilder builder = builderFactory.newExtensionBuilder(entry.getKey(), type, kind);

            switch (type) {
            case TEXT:
                builder.addText(exData.getString("text"));
                break;
            case ARTIFACTS:
                JsonArray ja2 = exData.getJsonArray("artifacts");
                for (JsonValue jv : ja2) {
                    if (jv.getValueType() == JsonValue.ValueType.STRING) {
                        String id = ((JsonString) jv).getString();
                        builder.addArtifact(ID.fromMavenID(id));
                    }
                }
                break;
            case JSON:
                builder.setJSON(exData.getJsonObject("json").toString());
                break;
            }
            extensions.add(builder.build());
        }

        return extensions.toArray(new Extension[] {});
    }

    @Override
    public void writeFeature(Feature feature, Writer jsonWriter) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public Feature mergeFeatures(ID targetID, Feature f1, Feature f2, MergeContext ctx) {
        FeatureBuilder fb = builderFactory.newFeatureBuilder(targetID);

        copyAttrs(f1, fb);
        copyAttrs(f2, fb);

        fb.addBundles(mergeBundles(f1, f2, ctx));
        fb.addConfigurations(mergeConfigs(f1, f2, ctx));
        fb.addExtensions(mergeExtensions(f1, f2, ctx));

        return fb.build();
    }

    private Bundle[] mergeBundles(Feature f1, Feature f2, MergeContext ctx) {
        List<Bundle> bundles = new ArrayList<>(f1.getBundles());
        List<Bundle> addedBundles = new ArrayList<>();

        for (Bundle b : f2.getBundles()) {
            ID bID = b.getID();
            boolean found = false;
            for (Iterator<Bundle> it = bundles.iterator(); it.hasNext(); ) {
                Bundle orgb = it.next();
                ID orgID = orgb.getID();

                if (bID.getGroupId().equals(orgID.getGroupId()) &&
                        bID.getArtifactId().equals(orgID.getArtifactId())) {
                    found = true;
                    List<Bundle> res = new ArrayList<>(ctx.handleBundleConflict(f1, b, f2, orgb));
                    if (res.contains(orgb)) {
                        res.remove(orgb);
                    } else {
                        it.remove();
                    }
                    addedBundles.addAll(res);
                }
            }
            if (!found) {
                addedBundles.add(b);
            }
        }
        bundles.addAll(addedBundles);
        return bundles.toArray(new Bundle[] {});
    }

    private Configuration[] mergeConfigs(Feature f1, Feature f2, MergeContext ctx) {
        Map<String,Configuration> configs = new HashMap<>(f1.getConfigurations());
        Map<String,Configuration> addConfigs = new HashMap<>();

        for (Map.Entry<String,Configuration> cfgEntry : f2.getConfigurations().entrySet()) {
            String pid = cfgEntry.getKey();
            Configuration newCfg = cfgEntry.getValue();
            Configuration orgCfg = configs.get(pid);
            if (orgCfg != null) {
                Configuration resCfg = ctx.handleConfigurationConflict(f1, orgCfg, f2, newCfg);
                if (!resCfg.equals(orgCfg)) {
                    configs.remove(pid);
                    addConfigs.put(pid, resCfg);
                }
            } else {
                addConfigs.put(pid, newCfg);
            }
        }

        configs.putAll(addConfigs);
        return configs.values().toArray(new Configuration[] {});
    }

    private Extension[] mergeExtensions(Feature f1, Feature f2, MergeContext ctx) {
        Map<String,Extension> extensions = new HashMap<>(f1.getExtensions());
        Map<String,Extension> addExtensions = new HashMap<>();

        for (Map.Entry<String,Extension> exEntry : f2.getExtensions().entrySet()) {
            String key = exEntry.getKey();
            Extension newEx = exEntry.getValue();
            Extension orgEx = extensions.get(key);
            if (orgEx != null) {
                Extension resEx = ctx.handleExtensionConflict(f1, orgEx, f2, newEx);
                if (!resEx.equals(orgEx)) {
                    extensions.remove(key);
                    addExtensions.put(key, resEx);
                }
            } else {
                addExtensions.put(key, newEx);
            }
        }

        extensions.putAll(addExtensions);
        return extensions.values().toArray(new Extension[] {});
    }

    private void copyAttrs(Feature f, FeatureBuilder fb) {
        if (f.getTitle() != null)
            fb.setTitle(f.getTitle());

        if (f.getDescription() != null)
            fb.setDescription(f.getDescription());

        if (f.getVendor() != null)
            fb.setVendor(f.getVendor());

        if (f.getLicense() != null)
            fb.setLicense(f.getLicense());

        if (f.getLocation() != null)
            fb.setLocation(f.getLocation());
    }
}