package io.mandrel.data.source;

import scala.Serializable;

public interface EntryListener extends Serializable {

	void onItem(String item);
}
