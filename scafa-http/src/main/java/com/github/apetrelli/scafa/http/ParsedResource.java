package com.github.apetrelli.scafa.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParsedResource {

	private String resource;

	private Map<String, List<String>> parameters = new LinkedHashMap<>();

	public ParsedResource(String unparsedResource) {
		int position = unparsedResource.indexOf("?");
		if (position >= 0) {
			resource = unparsedResource.substring(0, position);
			String[] pairs = unparsedResource.substring(position + 1).split("&");
			for (int i = 0; i < pairs.length; i++) {
				String[] pair = pairs[i].split("=");
				String key;
				try {
					key = URLDecoder.decode(pair[0], "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					key = pair[0];
				}
				List<String> values = parameters.get(key);
				if (values == null) {
					values = new ArrayList<>();
					parameters.put(key, values);
				}
				if (pair.length == 2) {
					try {
						values.add(URLDecoder.decode(pair[1], "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						values.add(pair[1]);
					}
				} else {
					values.add("");
				}
			}
		} else {
			resource = unparsedResource;
		}
	}

	public String getResource() {
		return resource;
	}

	public List<String> getValues(String parameter) {
		return parameters.get(parameter);
	}

	public String getValue(String parameter) {
		List<String> values = getValues(parameter);
		if (values != null && !values.isEmpty()) {
			return values.get(0);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return resource + (parameters.isEmpty() ? "" : "?") + encodeParameters();
	}

	private String encodeParameters() {
		StringBuilder builder = new StringBuilder();
		boolean firstDone = false;
		for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
			for (String value : entry.getValue()) {
				if (firstDone) {
					builder.append("&");
				} else {
					firstDone = true;
				}
				String encodedKey;
				try {
					encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					encodedKey = entry.getKey();
				}
				builder.append(encodedKey);
				builder.append("=");
				String encodedValue;
				try {
					encodedValue = URLEncoder.encode(value, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					encodedValue = value;
				}
				builder.append(encodedValue);
			}
		}
		return null;
	}
}
