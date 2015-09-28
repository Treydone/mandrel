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
package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Stores;
import io.mandrel.data.content.MetadataExtractor;
import io.mandrel.data.source.Source;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class SpiderValidator implements Validator {

	@Override
	public void validate(Object target, Errors errors) {

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", "Can not be null");

		Spider spider = (Spider) target;

		// Stores
		Stores stores = spider.getStores();

		if (stores.getMetadataStore() == null) {
			errors.rejectValue("stores.pageMetadataStore", "stores.pageMetadataStore.not.null", null, "Can not be null.");
		} else {
			if (!stores.getMetadataStore().check()) {
				errors.rejectValue("stores.pageMetadataStore", "stores.pageMetadataStore.failed", null, "MetadataStore failed check.");
			}
		}

		if (!stores.getMetadataStore().check()) {
			errors.rejectValue("stores.pageStore", "stores.pageStore.failed", null, "PageStore failed check.");
		}

		// Sources
		if (spider.getSources() != null) {
			int i = 0;
			for (Source source : spider.getSources()) {
				if (source.getName() == null) {
					errors.rejectValue("sources[" + i + "].name", "sources.name.not.null", null, "Can not be null.");
				}

				if (!source.check()) {
					errors.rejectValue("sources[" + i + "]", "sources.failed", null, "Check " + source.getName() + " failed.");
				}
				i++;
			}
		}

		// Client
		if (spider.getClient() == null) {
			errors.rejectValue("client", "client.not.null", null, "Can not be null.");
			// if (spider.getClient().getRequester() == null) {
			// errors.rejectValue("client.requester",
			// "client.requester.not.null", null, "Can not be null.");
			// }
		}

		// Extractors
		if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
			int i = 0;
			for (MetadataExtractor ex : spider.getExtractors().getPages()) {
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

				if (ex.getFields() == null) {
					errors.rejectValue("extractors.pages[" + i + "].fields", "extractors.fields.not.null", null, "Can not be null.");
				}

				if (ex.getMultiple() != null) {
					if (ex.getFields().stream().filter(f -> f.isUseMultiple()).anyMatch(f -> !ex.getMultiple().getType().equals(f.getExtractor().getType()))) {
						errors.rejectValue("extractors.pages[" + i + "].fields", "extractors.fields.not.same.type.as.multiple", null,
								"Is not the same type as the multiple.");
					}
				}
				i++;
			}
			;
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Spider.class.isAssignableFrom(clazz);
	}
}