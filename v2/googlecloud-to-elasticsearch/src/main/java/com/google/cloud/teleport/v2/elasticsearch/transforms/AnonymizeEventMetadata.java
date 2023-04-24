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

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.teleport.v2.elasticsearch.options.PubSubToElasticsearchOptions;
import com.google.privacy.dlp.v2.DeidentifyContentRequest;
import com.google.privacy.dlp.v2.DeidentifyContentRequest.Builder;
import com.google.privacy.dlp.v2.ProjectName;
import java.io.IOException;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * ProcessEventMetadata is used to enrich input message from Pub/Sub with metadata.
 */
public class AnonymizeEventMetadata extends PTransform<PCollection<String>, PCollection<String>> {

    private static final Logger LOG = LoggerFactory.getLogger(AnonymizeEventMetadata.class);

    @Override
    public PCollection<String> expand(PCollection<String> input) {
        return input.apply(ParDo.of(new AnonymizeEventMetadataFn()));
    }

    static class AnonymizeEventMetadataFn extends DoFn<String, String> {
        private DlpServiceClient dlpServiceClient;
        private Builder requestBuilder;

        private final String dlpProjectId;
        private final String deIdentifyTemplateName;

//        private ValueProvider<String> dlpProjectId;
//        private ValueProvider<String> deIdentifyTemplateName;
//        private ValueProvider<String> inspectTemplateName;


        public AnonymizeEventMetadataFn() {
            this.dlpProjectId = "gcp-ent-b-msasnttst-req0000248";
            this.dlpServiceClient = null;
            this.deIdentifyTemplateName = "projects/gcp-ent-b-msasnttst-req0000248/locations/global/deidentifyTemplates/dummy-deident";
        }

//        public AnonymizeEventMetadataFn(ValueProvider<String> dlpProjectId,
//                                        ValueProvider<String> deIdentifyTemplateName,
//                                        ValueProvider<String> inspectTemplateName) {
//            this.dlpProjectId = dlpProjectId;
//            this.dlpServiceClient = null;
//            this.deIdentifyTemplateName = deIdentifyTemplateName;
//            this.inspectTemplateName = inspectTemplateName;
//            this.inspectTemplateExist = false;
//        }

        @Setup
        public void setup() {
            try {
                this.requestBuilder =
                        DeidentifyContentRequest.newBuilder()
                                .setParent(ProjectName.of(dlpProjectId).toString())
                                .setDeidentifyTemplateName(deIdentifyTemplateName);
            } catch (Exception e) {
                LOG.error("Failed to create request builder", e.getMessage());
                throw new RuntimeException(e);
            }


            try {
                this.dlpServiceClient = DlpServiceClient.create();
            } catch (IOException e) {
                LOG.error("Failed to create DLP Service Client", e.getMessage());
                throw new RuntimeException(e);
            }
        }

        @ProcessElement
        public void processElement(ProcessContext context) {
            String input = context.element();
            PubSubToElasticsearchOptions options =
                    context.getPipelineOptions().as(PubSubToElasticsearchOptions.class);

            context.output(EventMetadataAnonymizer.build(input, dlpServiceClient, requestBuilder, options).getAnonymizedMessageAsString());
        }

        @Teardown
        public void closeClient() throws Exception {
            if (this.dlpServiceClient != null) {
                this.dlpServiceClient.close();
            }
        }
    }
}