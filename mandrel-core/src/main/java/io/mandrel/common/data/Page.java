package io.mandrel.common.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Basic {@code Page} implementation.
 * 
 * @param <T>
 *            the type of which the page consists.
 * @author Oliver Gierke
 */
@ThriftStruct
public class Page<T> {

	private final long total;
	private final PageRequest request;
	private final List<T> content = new ArrayList<T>();

	/**
	 * Constructor of {@code Page}.
	 * 
	 * @param content
	 *            the content of this page, must not be {@literal null}.
	 * @param pageable
	 *            the paging information, can be {@literal null}.
	 * @param total
	 *            the total amount of items available. The total might be adapted considering the length of the content
	 *            given, if it is going to be the content of the last page. This is in place to mitigate inconsistencies
	 */
	@ThriftConstructor
	public Page(@ThriftField(3) List<T> content, @ThriftField(2) PageRequest pageable, @ThriftField(1) long total) {

		Assert.notNull(content, "Content must not be null!");

		this.content.addAll(content);
		this.request = pageable;
		this.total = !content.isEmpty() && pageable != null && pageable.getOffset() + pageable.getPageSize() > total ? pageable.getOffset() + content.size()
				: total;
	}

	@ThriftField(1)
	public long getTotal() {
		return total;
	}

	@ThriftField(2)
	public PageRequest getRequest() {
		return request;
	}

	@ThriftField(3)
	public List<T> getContent() {
		return Collections.unmodifiableList(content);
	}

	/**
	 * Creates a new {@link Page} with the given content. This will result in the created {@link Page} being identical
	 * to the entire {@link List}.
	 * 
	 * @param content
	 *            must not be {@literal null}.
	 */
	public Page(List<T> content) {
		this(content, null, null == content ? 0 : content.size());
	}

	public int getTotalPages() {
		return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
	}

	public boolean hasNext() {
		return getNumber() + 1 < getTotalPages();
	}

	public boolean isLast() {
		return !hasNext();
	}

	public int getNumber() {
		return request == null ? 0 : request.getPageNumber();
	}

	public int getSize() {
		return request == null ? 0 : request.getPageSize();
	}

	public int getNumberOfElements() {
		return content.size();
	}

	public boolean hasPrevious() {
		return getNumber() > 0;
	}

	public boolean isFirst() {
		return !hasPrevious();
	}

	public PageRequest nextPageable() {
		return hasNext() ? request.next() : null;
	}

	public PageRequest previousPageable() {

		if (hasPrevious()) {
			return request.previousOrFirst();
		}

		return null;
	}

	public boolean hasContent() {
		return !content.isEmpty();
	}

	public String toString() {

		String contentType = "UNKNOWN";
		List<T> content = getContent();

		if (content.size() > 0) {
			contentType = content.get(0).getClass().getName();
		}

		return String.format("Page %s of %d containing %s instances", getNumber(), getTotalPages(), contentType);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Page<?>)) {
			return false;
		}

		Page<?> that = (Page<?>) obj;

		return this.total == that.total && super.equals(obj);
	}

	@Override
	public int hashCode() {

		int result = 17;

		result += 31 * (int) (total ^ total >>> 32);
		result += 31 * super.hashCode();

		return result;
	}
}
