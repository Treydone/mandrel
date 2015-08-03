package io.mandrel.common.querydsl;

import io.mandrel.common.MandrelParseException;
import io.mandrel.data.filters.link.BooleanLinkFilters.AndFilter;
import io.mandrel.data.filters.link.BooleanLinkFilters.OrFilter;
import io.mandrel.data.filters.link.LinkFilter;
import io.mandrel.data.filters.link.SanitizeParamsFilter;
import io.mandrel.data.filters.link.SkipAncorFilter;
import io.mandrel.data.filters.link.StartWithFilter;
import io.mandrel.data.filters.link.UrlPatternFilter;

import java.util.Arrays;

import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;
import org.apache.commons.beanutils.PropertyUtils;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Var;

import com.google.common.base.Throwables;

public abstract class DslParser {

	public static LinkFilter parseLinkFilter(String input) {

		PropertyUtils.addBeanIntrospector(new FluentPropertyBeanIntrospector());
		LinkFilterDslParser parser = Parboiled.createParser(LinkFilterDslParser.class);

		ParsingResult<LinkFilter> result = new RecoveringParseRunner<LinkFilter>(parser.Root()).run(input);

		if (result.hasErrors()) {
			throw new MandrelParseException("\nParse Errors:\n" + ErrorUtils.printParseErrors(result));
		}

		return result.parseTreeRoot.getValue();
	}

	/**
	 * skip_ancor and (allowed_for_domains("www.xxx.com", "www.yyy.com") or
	 * start_with("/uri/"))
	 */
	@BuildParseTree
	public static class LinkFilterDslParser extends BaseParser<LinkFilter> {

		public Rule Root() {
			return Sequence(Expression(), EOI);
		}

		public Rule Expression() {
			return Sequence(Term(),
					ZeroOrMore(Sequence(WhiteSpace(), "or", WhiteSpace(), Term(), push(new OrFilter().setFilters(Arrays.asList(pop(), pop()))))));
		}

		public Rule Term() {
			return Sequence(Factor(),
					ZeroOrMore(Sequence(WhiteSpace(), "and", WhiteSpace(), Factor(), push(new AndFilter().setFilters(Arrays.asList(pop(), pop()))))));
		}

		public Rule Factor() {
			return FirstOf(Filter(), Parens(Filter()), Parens(Expression()));
		}

		public Rule Filter() {
			Var<LinkFilter> filter = new Var<>();
			return FirstOf(Sequence(WhiteSpace(), "start_with", filter.set(new StartWithFilter()), Options(filter), push(filter.get())),
					Sequence(WhiteSpace(), "pattern", filter.set(new UrlPatternFilter()), Options(filter), push(filter.get())),
					Sequence(WhiteSpace(), "skip_ancor", filter.set(new SkipAncorFilter()), push(filter.get())),
					Sequence(WhiteSpace(), "sanitize_params", filter.set(new SanitizeParamsFilter()), Options(filter), push(filter.get())));
		}

		public Rule Options(Var<LinkFilter> filter) {
			return Optional(Parens(Properties(filter)));
		}

		public Rule Parens(Rule rule) {
			return Sequence('(', rule, ')');
		}

		public Rule Properties(Var<LinkFilter> filter) {
			Var<Object> value = new Var<Object>();
			return FirstOf(Sequence(SimpleValueOrList(value), setValue(filter, "value", value.get())),
					Sequence(Property(filter), ZeroOrMore(Ch(','), Property(filter))));
		}

		public Rule Property(Var<LinkFilter> filter) {
			Var<String> key = new Var<String>();
			Var<Object> value = new Var<Object>();
			Rule sequence = Sequence(WhiteSpace(), Chars(), key.set(match()), WhiteSpace(), ValueSep(), SimpleValueOrList(value),
					setValue(filter, key.get(), value.get()));
			return sequence;
		}

		public Rule SimpleValueOrList(Var<Object> value) {
			return FirstOf(QuotedValue(value), List(value));
		}

		public Rule QuotedValue(Var<Object> value) {
			return Sequence(WhiteSpace(), PropertySep(), Value(), value.set(match()), PropertySep(), WhiteSpace());
		}

		public Rule List(Var<Object> value) {
			ListVar items = new ListVar();
			return Sequence(WhiteSpace(), '[', QuotedValue(items), ZeroOrMore(',', QuotedValue(items)), ']', WhiteSpace(), value.set(items.get()));
		}

		public Rule QuotedValue(ListVar value) {
			return Sequence(WhiteSpace(), PropertySep(), Value(), value.add(match()), PropertySep(), WhiteSpace());
		}

		public Rule Value() {
			return OneOrMore(TestNot(PropertySep()), ANY);
		}

		public Rule ValueSep() {
			return Ch(':');
		}

		public Rule PropertySep() {
			return Ch('\'');
		}

		public boolean setValue(Var<LinkFilter> filter, String key, Object value) {
			try {
				PropertyUtils.setSimpleProperty(filter.get(), key, value);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
			return true;
		}

		public Rule Chars() {
			return OneOrMore(FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), "_", "$"));
		}

		public Rule WhiteSpace() {
			return ZeroOrMore(AnyOf(" \t\f"));
		}
	}
}
