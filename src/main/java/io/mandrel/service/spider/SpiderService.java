package io.mandrel.service.spider;

import io.mandrel.service.spider.Spider.State;
import io.mandrel.service.spider.Spider.Stores;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ReplicatedMap;

@Component
public class SpiderService {

	private final HazelcastInstance instance;

	private Validator spiderValidator = new SpiderValidator();

	@Inject
	public SpiderService(HazelcastInstance instance) {
		this.instance = instance;
	}

	public Errors validate(Spider spider) {
		Errors errors = new BeanPropertyBindingResult(spider, "spider");
		spiderValidator.validate(spider, errors);
		return errors;
	}

	/**
	 * Store the spider and distribute it across the cluster.
	 * 
	 * @param spider
	 * @return
	 */
	public Spider add(Spider spider) {

		Errors errors = validate(spider);

		if (errors.hasErrors()) {
			// TODO
			throw new RuntimeException("Errors!");
		}

		long id = instance.getIdGenerator("spiders").newId();
		spider.setId(id);
		spiders().put(id, spider);
		return spider;
	}

	public Optional<Spider> get(long id) {
		Spider value = (Spider) spiders().get(id);
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public Stream<Spider> list() {
		return spiders().values().stream().map(el -> (Spider) el);
	}

	public void start(Spider spider) {

		// TODO
		Map<String, Object> properties = null;

		spider.getSources().stream().forEach(source -> {
			source.init(properties);
		});

		spider.getStores().getPageMetadataStore().init(properties);
		spider.getStores().getPageStore().init(properties);

		spider.getExtractors().stream().forEach(ex -> ex.getDataStore().init(ex));

		spider.setState(State.STARTED);
		spiders().put(spider.getId(), spider);

	}

	private ReplicatedMap<Object, Object> spiders() {
		return instance.getReplicatedMap("spiders");
	}

	private final class SpiderValidator implements Validator {
		@Override
		public void validate(Object target, Errors errors) {

			// ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName",
			// "field.required");
			// ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password",
			// "field.required");

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
					if (!source.check()) {
						errors.rejectValue("sources", "sources.failed", null, "Check " + source.getName() + " failed.");
					}
				});
			}

			// Client
			// TODO
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return Spider.class.isAssignableFrom(clazz);
		}
	}
}
