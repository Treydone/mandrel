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
package io.mandrel.data.source;

import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class CsvSource extends Source {

	private static final long serialVersionUID = -343795756996004307L;

	private List<String> files;

	public void register(EntryListener listener) {
		files.forEach(file -> {
			try {
				VFS.getManager().resolveFile(file).getContent().getInputStream();
			} catch (FileSystemException e) {
				log.debug("Can not resolve file {}", file, e);
			}
		});
	}

	public boolean check() {
		return files.stream().allMatch(file -> {
			try {
				return VFS.getManager().resolveFile(file).exists();
			} catch (FileSystemException e) {
				log.debug("Can not resolve file {}", file, e);
				return false;
			}
		});
	}
}
