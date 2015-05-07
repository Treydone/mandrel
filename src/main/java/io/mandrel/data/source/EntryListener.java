package io.mandrel.data.source;

import java.io.Serializable;

public interface EntryListener extends Serializable {

	void onItem(String item);
}
