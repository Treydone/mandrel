package io.mandrel.endpoints;

import io.mandrel.document.Document;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Item extends Document {

	private static final long serialVersionUID = 6406152284787009438L;

	@JsonProperty("DT_RowId")
	private String rowId;

	@JsonProperty("DT_RowClass")
	private String rowClass;

	public static Item of(Document data) {
		Item item = new Item();
		item.putAll(data);
		return item;
	}
}
