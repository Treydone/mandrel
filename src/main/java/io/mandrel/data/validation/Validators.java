package io.mandrel.data.validation;

import io.mandrel.common.data.Spider;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

public class Validators {

	private final static Validator spider = new SpiderValidator();

	public static BindingResult validate(Spider theSpider) {
		BindingResult errors = new BeanPropertyBindingResult(theSpider, "spider");
		spider.validate(theSpider, errors);
		return errors;
	}

	public static Validator spider() {
		return spider;
	}
}
