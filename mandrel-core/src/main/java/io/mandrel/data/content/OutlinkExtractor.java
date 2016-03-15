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
package io.mandrel.data.content;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OutlinkExtractor extends NamedDataExtractorFormatter {

	private static final long serialVersionUID = -4094495903167374714L;

	private String name;
	private Extractor extractor;
	private Formatter formatter;

	public OutlinkExtractor(String name) {
		this.extractor = new Extractor();
		this.extractor.setSource(SourceType.BODY);
		this.extractor.setType("xpath");
		this.extractor.setValue("//a/@href/text()");
		this.name = name;
	}

	public OutlinkExtractor() {
	}
}
