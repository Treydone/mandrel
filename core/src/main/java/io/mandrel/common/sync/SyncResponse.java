package io.mandrel.common.sync;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@Accessors(chain = true)
@ThriftStruct
public class SyncResponse {

	@Getter(onMethod = @__(@ThriftField(1)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Long> created;
	@Getter(onMethod = @__(@ThriftField(2)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Long> updated;
	@Getter(onMethod = @__(@ThriftField(3)))
	@Setter(onMethod = @__(@ThriftField))
	private List<Long> deleted;

	public boolean anyAction() {
		return this.getCreated() != null && this.getCreated().size() > 1 && this.getUpdated() != null && this.getUpdated().size() > 1
				&& this.getDeleted() != null && this.getDeleted().size() > 1;
	}
}
