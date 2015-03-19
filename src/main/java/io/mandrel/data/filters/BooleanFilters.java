package io.mandrel.data.filters;

import io.mandrel.http.WebPage;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

public interface BooleanFilters {

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class TrueFilter extends WebPageFilter {

		public boolean isValid(WebPage webPage) {
			return true;
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class FalseFilter extends WebPageFilter {

		public boolean isValid(WebPage webPage) {
			return false;
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class NotFilter extends WebPageFilter {

		private WebPageFilter filter;

		public boolean isValid(WebPage webPage) {
			return !filter.isValid(webPage);
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class OrFilter extends WebPageFilter {

		private List<WebPageFilter> filters;

		public boolean isValid(WebPage webPage) {
			return filters.stream().anyMatch(f -> f.isValid(webPage));
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class AndFilter extends WebPageFilter {

		private List<WebPageFilter> filters;

		public boolean isValid(WebPage webPage) {
			return filters.stream().allMatch(f -> f.isValid(webPage));
		}
	}
}
