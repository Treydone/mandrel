package io.mandrel.data.spider;

import io.mandrel.common.data.Spider;
import io.mandrel.common.data.Stores;

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

		if (stores.getPageStore() == null) {
			errors.rejectValue("stores.pageStore", "stores.pageStore.not.null", null, "Can not be null.");
		} else {
			if (!stores.getPageMetadataStore().check()) {
				errors.rejectValue("stores.pageStore", "stores.pageStore.failed", null, "PageStore failed check.");
			}
		}

		// Sources
		if (spider.getSources() != null) {
			spider.getSources().stream().forEach(source -> {
				if (source.getName() == null) {
					errors.rejectValue("sources.name", "sources.name.not.null", null, "Can not be null.");
				}

				if (!source.check()) {
					errors.rejectValue("sources", "sources.failed", null, "Check " + source.getName() + " failed.");
				}
			});
		}

		// Client
		// TODO

		// Extractors
		if (spider.getExtractors().getPages() != null) {
			spider.getExtractors()
					.getPages()
					.stream()
					.forEach(
							ex -> {
								if (ex.getName() == null) {
									errors.rejectValue("extractors.name", "extractors.name.not.null", null, "Can not be null.");
								}

								if (!ex.getDataStore().check()) {
									errors.rejectValue("extractors.datastore", "extractors.datastore.failed", null, "Check " + ex.getName()
											+ " failed.");
								}

								if (ex.getFields() == null) {
									errors.rejectValue("extractors.fields", "extractors.fields.not.null", null, "Can not be null.");
								}

								if (ex.getMultiple() != null) {

									if (ex.getFields().stream().filter(f -> f.isUseMultiple())
											.anyMatch(f -> !ex.getMultiple().getType().equals(f.getExtractor().getType()))) {
										errors.rejectValue("extractors.fields", "extractors.fields.not.same.type.as.multiple", null,
												"Is not the same type as the multiple.");
									}

								}

							});
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Spider.class.isAssignableFrom(clazz);
	}
}