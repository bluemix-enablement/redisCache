package com.ibm.cloudoe.ecaas.samples;

/**
 * Define the elastic caching POJO, mainly in order to program operation, such
 * as convenient output json object.
 */
public class ECache {
	private String key;
	private String value;

	public ECache() {
	}

	public ECache(Object obj, Object obj2) {
		if (obj instanceof String && obj2 instanceof String) {
			this.key = (String) obj;
			this.value = (String) obj2;
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		// convenient output json format
		return String
				.format("{\"key\": \"%s\", \"value\": \"%s\"}", StringUtil.htmlEncode(key), StringUtil.htmlEncode(value));
	}

}
