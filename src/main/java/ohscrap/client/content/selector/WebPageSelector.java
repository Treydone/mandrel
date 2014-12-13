package ohscrap.client.content.selector;

public interface WebPageSelector {

	String getName();

	Object select(byte[] value);
}
