package io.mandrel.common.source;

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

	private List<String> files;

	public void register(EntryListener listener) {
		files.forEach(file -> {
			try {
				VFS.getManager().resolveFile(file).getContent()
						.getInputStream();
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
