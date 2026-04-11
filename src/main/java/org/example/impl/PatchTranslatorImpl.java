/*
 * Copyright 2016-2017 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.Configure;

/**
 * @author Baoyi Chen
 */
public class PatchTranslatorImpl extends AbstractTranslator {
	
	public PatchTranslatorImpl(Configure configure) {
		super(configure);
	}
	
	@Override
	public void start() {
		
	}
	
	@Override
	public void stop() {
		
	}
	
	@Override
	public int hintSize() {
		return 0;
	}
	
	@Override
	public String translate(String text, Map<String, String> options) {
		if (options != null && !options.containsKey(text)) {
			options.put(text, text);
		}
		return text;
	}
	
	@Override
	public String proofread(Map<String, String> map, Map<String, String> options) {
		return null;
	}
	
	@Override
	public List<Map.Entry<String, String>> translates(List<Map.Entry<String, String>> texts, Map<String, String> options) {
		List<Map.Entry<String, String>> r = new ArrayList<>(texts.size());
		for(Map.Entry<String, String> entry : texts) {
			if (options != null && !options.containsKey(entry.getValue())) {
				options.put(entry.getValue(), entry.getValue());
			}
			r.add(Map.entry(entry.getKey(), entry.getValue()));
		}
		return r;
	}
}
