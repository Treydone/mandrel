package io.mandrel.data.filters;

import io.mandrel.http.WebPage;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

public interface BooleanFilters {

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class TrueFilter extends WebPageFilter {

		private static final long serialVersionUID = 2874603296721730595L;

		public boolean isValid(WebPage webPage) {
			return true;
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class FalseFilter extends WebPageFilter {

		private static final long serialVersionUID = -1371552939630443549L;

		public boolean isValid(WebPage webPage) {
			return false;
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class NotFilter extends WebPageFilter {

		private static final long serialVersionUID = -2429186996142024643L;

		private WebPageFilter filter;

		public boolean isValid(WebPage webPage) {
			return !filter.isValid(webPage);
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class OrFilter extends WebPageFilter {

		private static final long serialVersionUID = 7721027082341003067L;

		private List<WebPageFilter> filters;

		public boolean isValid(WebPage webPage) {
			return filters.stream().anyMatch(f -> f.isValid(webPage));
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class AndFilter extends WebPageFilter {

		private static final long serialVersionUID = -7125723269925872394L;

		private List<WebPageFilter> filters;

		public boolean isValid(WebPage webPage) {
			return filters.stream().allMatch(f -> f.isValid(webPage));
		}
	}
}
