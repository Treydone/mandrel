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
package io.mandrel.data.content.selector;

import io.mandrel.metadata.FetchMetadata;

import java.util.Arrays;
import java.util.List;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.google.common.base.Throwables;

public class TikaSelector extends BodySelector<String> {

	@Override
	public String name() {
		return "tika";
	}

	@Override
	public Instance<String> init(FetchMetadata data, byte[] bytes, boolean isSegment) {
		return new Instance<String>() {
			@Override
			public <T> List<T> select(String value, DataConverter<String, T> converter) {
				TikaConfig tikaConfig = TikaConfig.getDefaultConfig();

				org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
				AutoDetectParser parser = new AutoDetectParser(tikaConfig);
				ContentHandler handler = new BodyContentHandler();
				TikaInputStream stream = TikaInputStream.get(bytes, metadata);
				try {
					parser.parse(stream, handler, metadata, new ParseContext());
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
				return Arrays.asList(converter.convert(handler.toString()));
			}
		};
	}
}
