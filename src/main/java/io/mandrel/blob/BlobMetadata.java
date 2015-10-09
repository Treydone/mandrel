package io.mandrel.blob;

import io.mandrel.io.ContentMetadata;
import io.mandrel.metadata.FetchMetadata;

import java.net.URI;
import java.util.Date;

import javax.annotation.Nullable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
public class BlobMetadata {

	/**
	 * URI used to access this resource
	 */
	private URI uri;

	/**
	 * Name of this resource. Names are dictated by the user. For files, this
	 * may be the filename, ex. file.txt
	 * 
	 */
	private String name;

	@Nullable
	private String container;

	private ContentMetadata contentMetadata;

	/**
	 * Any key-value pairs associated with the fetching of the resource.
	 */
	private FetchMetadata fetchMetadata;

	/**
	 * Creation date of the resource, possibly null.
	 */
	private Date creationDate;

	/**
	 * Last modification time of the resource
	 */
	private Date lastModified;

	/** Size of the resource, possibly null. */
	private Long size;
}
