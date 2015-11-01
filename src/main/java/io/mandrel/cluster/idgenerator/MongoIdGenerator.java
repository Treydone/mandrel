package io.mandrel.cluster.idgenerator;

import org.bson.types.ObjectId;

public class MongoIdGenerator implements IdGenerator {

	@Override
	public long generateId(String name) {
		return new ObjectId().toString().hashCode();
	}
}
