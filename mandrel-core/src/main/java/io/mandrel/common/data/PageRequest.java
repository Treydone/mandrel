package io.mandrel.common.data;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class PageRequest {

	private final int pageNumber;
	private final int pageSize;

	/**
	 * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing 0 for {@code page} will return
	 * the first page.
	 * 
	 * @param page
	 *            must not be less than zero.
	 * @param size
	 *            must not be less than one.
	 */
	@ThriftConstructor
	public PageRequest(@ThriftField(value = 1, name = "pageNumber") int page, @ThriftField(value = 2, name = "pageSize") int size) {

		if (page < 0) {
			throw new IllegalArgumentException("Page index must not be less than zero!");
		}

		if (size < 1) {
			throw new IllegalArgumentException("Page size must not be less than one!");
		}

		this.pageNumber = page;
		this.pageSize = size;
	}

	@ThriftField(2)
	public int getPageSize() {
		return pageSize;
	}

	@ThriftField(1)
	public int getPageNumber() {
		return pageNumber;
	}

	public int getOffset() {
		return pageNumber * pageSize;
	}

	public boolean hasPrevious() {
		return pageNumber > 0;
	}

	public PageRequest previousOrFirst() {
		return hasPrevious() ? previous() : first();
	}

	public PageRequest next() {
		return new PageRequest(getPageNumber() + 1, getPageSize());
	}

	public PageRequest previous() {
		return getPageNumber() == 0 ? this : new PageRequest(getPageNumber() - 1, getPageSize());
	}

	public PageRequest first() {
		return new PageRequest(0, getPageSize());
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;

		result = prime * result + pageNumber;
		result = prime * result + pageSize;

		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		PageRequest other = (PageRequest) obj;
		return this.pageNumber == other.pageNumber && this.pageSize == other.pageSize;
	}
}
