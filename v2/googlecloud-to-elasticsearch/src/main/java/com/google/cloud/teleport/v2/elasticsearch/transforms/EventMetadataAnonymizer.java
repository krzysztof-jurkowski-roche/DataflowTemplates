/*
 * Copyright (C) 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.v2.elasticsearch.transforms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.teleport.v2.elasticsearch.options.PubSubToElasticsearchOptions;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import java.io.Serializable;

/**
 * EventMetadataBuilder is used to insert metadata required by Elasticsearch. The metadata helps
 * Elasticsearch to visualize events on the dashboards, also uniform message format is needed for
 * data analytics.
 *
 * <p>Please refer to <b><a href=
 * "https://github.com/GoogleCloudPlatform/DataflowTemplates/blob/master/v2/googlecloud-to-elasticsearch/src/test/resources/EventMetadataBuilder/inputGCPAuditlogMessageEnriched.json">
 * inputGCPAuditlogMessageEnriched.json</a></b> to see an example of enriched message.
 */
public class EventMetadataAnonymizer implements Serializable {
    @JsonIgnore
    private final String inputMessage;
    @JsonIgnore
    private JsonNode enrichedMessage;
    @JsonIgnore
    final ObjectMapper objectMapper = new ObjectMapper();

    private EventMetadataAnonymizer(String inputMessage, PubSubToElasticsearchOptions pubSubToElasticsearchOptions) {
        this.inputMessage = inputMessage;
    }

    public static EventMetadataAnonymizer build(
            String inputMessage, PubSubToElasticsearchOptions pubSubToElasticsearchOptions) {
        return new EventMetadataAnonymizer(inputMessage, pubSubToElasticsearchOptions);
    }

    public static EventMetadataAnonymizer build(String inputMessage, DlpServiceClient dlpServiceClient, DeidentifyContentRequest.Builder requestBuilder, PubSubToElasticsearchOptions pubSubToElasticsearchOptions) {
        return new EventMetadataAnonymizer(inputMessage, pubSubToElasticsearchOptions);
    }

    private void anonymize() {
        try {
            enrichedMessage = objectMapper.readTree(inputMessage);
            ((ObjectNode) enrichedMessage).put("custom", "greetings from anonymizer");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Exception occurred while processing input message: " + inputMessage, e);
        }
    }

    public String getAnonymizedMessageAsString() {
        if (enrichedMessage == null) {
            this.anonymize();
        }

        try {
            return objectMapper.writeValueAsString(enrichedMessage);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Exception occurred while building enriched message: " + enrichedMessage, e);
        }
    }

    public JsonNode getAnonymizedMessageAsJsonNode() {
        if (enrichedMessage == null) {
            this.anonymize();
        }

        return enrichedMessage;
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(enrichedMessage);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Exception occurred while writing EventMetadataBuilder as String.", e);
        }
    }

    static class EventMetadata {
        @JsonProperty("@timestamp")
        private String timestamp;

        @JsonProperty("message")
        private String message;

        @JsonIgnore
        private String inputMessage;
        @JsonIgnore
        private JsonNode enrichedMessage;
        @JsonIgnore
        final ObjectMapper objectMapper = new ObjectMapper();
    }
}
