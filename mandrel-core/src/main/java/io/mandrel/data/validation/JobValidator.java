/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.data.validation;

import io.mandrel.common.data.Job;
import io.mandrel.common.data.JobDefinition;
import io.mandrel.common.data.StoresDefinition;
import io.mandrel.data.content.DataExtractor;
import io.mandrel.data.content.DefaultDataExtractor;
import io.mandrel.data.source.Source;
import io.mandrel.data.source.Source.SourceDefinition;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class JobValidator implements Validator {

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", "Can not be null");

		JobDefinition job = (JobDefinition) target;

		// Stores
		StoresDefinition stores = job.getStores();

		if (stores.getMetadataStore() == null) {
			errors.rejectValue("stores.pageMetadataStore", "stores.pageMetadataStore.not.null", null, "Can not be null.");
		}

		// Sources
		if (job.getSources() != null) {
			int i = 0;
			for (SourceDefinition<? extends Source> source : job.getSources()) {
				if (source.name() == null) {
					errors.rejectValue("sources[" + i + "].name", "sources.name.not.null", null, "Can not be null.");
				}

				i++;
			}
		}

		// Client
		if (job.getClient() == null) {
			errors.rejectValue("client", "client.not.null", null, "Can not be null.");
			// if (job.getClient().getRequester() == null) {
			// errors.rejectValue("client.requester",
			// "client.requester.not.null", null, "Can not be null.");
			// }
		}

		// Extractors
		if (job.getExtractors() != null && job.getExtractors().getData() != null) {
			int i = 0;
			for (DataExtractor ex : job.getExtractors().getData()) {
				if (ex.getName() == null) {
					errors.rejectValue("extractors.pages[" + i + "].name", "extractors.name.not.null", null, "Can not be null.");
				}

				if (ex.getDocumentStore() == null) {
					errors.rejectValue("extractors.pages[" + i + "].documentStore", "extractors.fields.not.null", null, "Can not be null.");
				}

				// TODO init
				// if (!ex.getDocumentStore().check()) {
				// errors.rejectValue("extractors.pages[" + i +
				// "].documentStore", "extractors.datastore.failed", null,
				// "Check " + ex.getName() + " failed.");
				// }

				if (ex instanceof DefaultDataExtractor) {
					DefaultDataExtractor dex = (DefaultDataExtractor) ex;
					if (dex.getFields() == null) {
						errors.rejectValue("extractors.pages[" + i + "].fields", "extractors.fields.not.null", null, "Can not be null.");
					}

					if (dex.getMultiple() != null) {
						if (dex.getFields().stream().filter(f -> f.isUseMultiple())
								.anyMatch(f -> !dex.getMultiple().getType().equals(f.getExtractor().getType()))) {
							errors.rejectValue("extractors.pages[" + i + "].fields", "extractors.fields.not.same.type.as.multiple", null,
									"Is not the same type as the multiple.");
						}
					}
				}
				i++;
			}
			;
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Job.class.isAssignableFrom(clazz);
	}
}