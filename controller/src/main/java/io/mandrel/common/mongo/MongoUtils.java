package io.mandrel.common.mongo;

import lombok.extern.slf4j.Slf4j;

import org.bson.Document;

import com.google.common.collect.Lists;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;

@Slf4j
public abstract class MongoUtils {

	public static void checkCapped(MongoDatabase database, String collectionName, int size, int maxDocuments) {
		if (Lists.newArrayList(database.listCollectionNames()).contains(collectionName)) {
			log.debug("'{}' collection already exists...", collectionName);

			// Check if already capped
			Document command = new Document("collStats", collectionName);
			boolean isCapped = database.runCommand(command, ReadPreference.primary()).getBoolean("capped").booleanValue();

			if (!isCapped) {
				log.info("'{}' is not capped, converting it...", collectionName);
				command = new Document("convertToCapped", collectionName).append("size", size).append("max", maxDocuments);
				database.runCommand(command, ReadPreference.primary());
			} else {
				log.debug("'{}' collection already capped!", collectionName);
			}

		} else {
			database.createCollection(collectionName, new CreateCollectionOptions().capped(true).maxDocuments(maxDocuments).sizeInBytes(size));
		}
	}
}
