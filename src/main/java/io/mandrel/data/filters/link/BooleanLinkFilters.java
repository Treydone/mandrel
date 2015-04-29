package io.mandrel.data.filters.link;

import io.mandrel.data.spider.Link;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

public interface BooleanLinkFilters {

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class TrueFilter extends LinkFilter {

		private static final long serialVersionUID = 2874603296721730595L;

		public boolean isValid(Link link) {
			return true;
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class FalseFilter extends LinkFilter {

		private static final long serialVersionUID = -1371552939630443549L;

		public boolean isValid(Link link) {
			return false;
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class NotFilter extends LinkFilter {

		private static final long serialVersionUID = -2429186996142024643L;

		private LinkFilter filter;

		public boolean isValid(Link link) {
			return !filter.isValid(link);
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class OrFilter extends LinkFilter {

		private static final long serialVersionUID = 7721027082341003067L;

		private List<LinkFilter> filters;

		public boolean isValid(Link link) {
			return filters.stream().anyMatch(f -> f.isValid(link));
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public class AndFilter extends LinkFilter {

		private static final long serialVersionUID = -7125723269925872394L;

		private List<LinkFilter> filters;

		public boolean isValid(Link link) {
			return filters.stream().allMatch(f -> f.isValid(link));
		}
	}
}
