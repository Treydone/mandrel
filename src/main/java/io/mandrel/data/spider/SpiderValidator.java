package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Stores;
import io.mandrel.data.content.WebPageExtractor;
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

		if (stores.getPageMetadataStore() == null) {
			errors.rejectValue("stores.pageMetadataStore", "stores.pageMetadataStore.not.null", null, "Can not be null.");
		} else {
			if (!stores.getPageMetadataStore().check()) {
				errors.rejectValue("stores.pageMetadataStore", "stores.pageMetadataStore.failed", null, "PageMetadataStore failed check.");
			}
		}

		if (stores.getPageStore() != null && !stores.getPageMetadataStore().check()) {
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
		// TODO

		// Extractors
		if (spider.getExtractors() != null && spider.getExtractors().getPages() != null) {
			int i = 0;
			for (WebPageExtractor ex : spider.getExtractors().getPages()) {
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