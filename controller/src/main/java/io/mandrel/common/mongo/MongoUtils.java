/*
 * Licensed to Mandrel under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Mandrel licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mandrel.common.mongo;

import lombok.extern.slf4j.Slf4j;

import org.bson.Document;

import com.google.common.collect.Lists;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;

@Slf4j
public abstract class MongoUtils {

	public static void checkCapped(MongoDatabase database, String collectionName, int size, int maxDocuments, boolean delete) {
		if (Lists.newArrayList(database.listCollectionNames()).contains(collectionName)) {
			log.debug("'{}' collection already exists...", collectionName);

			// Check if already capped
			Document command = new Document("collStats", collectionName);
			boolean isCapped = database.runCommand(command, ReadPreference.primary()).getBoolean("capped").booleanValue();

			if (!isCapped) {
				if (delete) {
					database.getCollection(collectionName).drop();
					database.createCollection(collectionName, new CreateCollectionOptions().capped(true).maxDocuments(maxDocuments).sizeInBytes(size));
				} else {
					log.info("'{}' is not capped, converting it...", collectionName);
					command = new Document("convertToCapped", collectionName).append("size", size).append("max", maxDocuments);
					database.runCommand(command, ReadPreference.primary());
				}
			} else {
				log.debug("'{}' collection already capped!", collectionName);
			}

		} else {
			database.createCollection(collectionName, new CreateCollectionOptions().capped(true).maxDocuments(maxDocuments).sizeInBytes(size));
		}
	}
}
