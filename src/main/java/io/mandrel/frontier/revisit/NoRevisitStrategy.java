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
package io.mandrel.frontier.revisit;

import io.mandrel.blob.BlobMetadata;
import io.mandrel.common.unit.TimeValue;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = false)
public class NoRevisitStrategy extends RevisitStrategy implements Serializable {

	private static final long serialVersionUID = -6064010303003504348L;

	@JsonProperty("rescheduled_after")
	private TimeValue rescheduledAfter = TimeValue.parseTimeValue("1d");

	@Override
	public boolean isScheduledForRevisit(BlobMetadata metadata) {
		return false;
	}
}
