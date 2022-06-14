package com.github.apetrelli.scafa.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.apetrelli.scafa.proto.util.AsciiString;

public class ParsedResource {

	private final String resource;

	private final Map<String, List<String>> parameters = new LinkedHashMap<>();

	public ParsedResource(AsciiString unparsedResourceAscii) {
		String unparsedResource = unparsedResourceAscii.toString();
		int position = unparsedResource.indexOf("?");
		if (position >= 0) {
			String escapedResource = unparsedResource.substring(0, position);
			resource = URLDecoder.decode(escapedResource, StandardCharsets.UTF_8);
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
					values.add(URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
				} else {
					values.add("");
				}
			}
		} else {
			resource = URLDecoder.decode(unparsedResource, StandardCharsets.UTF_8);
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
		return URLEncoder.encode(resource, StandardCharsets.UTF_8) + (parameters.isEmpty() ? "" : "?") + encodeParameters();
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
				String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
				builder.append(encodedKey);
				builder.append("=");
				String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
				builder.append(encodedValue);
			}
		}
		return null;
	}
}
