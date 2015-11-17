package io.mandrel.common.client;

import io.mandrel.common.data.Spider;

import java.util.List;

import lombok.Data;

@Data
public class SyncRequest {

	private List<Spider> spiders;
}
