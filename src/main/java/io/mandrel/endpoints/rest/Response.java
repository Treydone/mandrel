package io.mandrel.endpoints.rest;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "A response", description = "An object wrapping the response")
@Data
@Accessors(fluent = true, chain = true)
public class Response<T> {

	@ApiModelProperty("The duration of the request by be processed")
	@JsonProperty("took")
	private int took;

	@ApiModelProperty("The underlying data of the response")
	@JsonProperty("data")
	private T data;

	public static <T> Response<T> from(T data) {
		return new Response<T>().data(data);
	}
}
