/**
 * Copyright 2017 Innodev
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package au.com.innodev.wmboost.data;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.wm.data.IDataCursor;

import au.com.innodev.wmboost.data.internal.Preconditions;
import au.com.innodev.wmboost.data.internal.TextUtil;

/**
 * Base implementation of an document entry.
 *
 * @param <A>
 *            Accessor type
 * @param <M>
 *            Mutator type
 */
class BaseEntry<A, M> {

	private final DocumentImpl document;
	private final String key;
	private final NormaliseOption normaliseOption;
	
	public BaseEntry(DocumentImpl document, String key, NormaliseOption normaliseOption) {
		this.key = Preconditions.checkHasLength(key, "Invalid key was provided (null or empty string)");
		this.document = Preconditions.checkNotNull(document);
		this.normaliseOption = Preconditions.checkNotNull(normaliseOption);
	}

	protected final IDataCursorResource newCursorResource() {
		return document.newCursorResource();
	}

	public String getKey() {
		return key;
	}

	protected final <T> T getConvertedValue(Object value, TypeDescriptor destTypeSpec) {
		return getConvertedValue(value, destTypeSpec, TypeDescriptor.forObject(value));
	}

	private ConversionService getConversionService() {
		return document.getInternalConversionService();
	}

	protected final <T> T getConvertedValue(Object value, TypeDescriptor destTypeSpec, TypeDescriptor sourceTypeDescriptor) {
		ConversionService conversionService = getConversionService();

		Object convertedValue;

		// Single value
		if (destTypeSpec.isCollection() || destTypeSpec.isArray()) {
			try {
				convertedValue = conversionService.convert(value, sourceTypeDescriptor, destTypeSpec);
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Unable to convert value to '" + destTypeSpec
						+ "' while retrieving document field '" + key + "'", e);
			}
		} else {
			try {
				convertedValue = conversionService.convert(value, sourceTypeDescriptor, destTypeSpec);
			} catch (RuntimeException e) {
				StringBuilder message = new StringBuilder();
				message.append("Unable to convert value to type '");
				message.append(destTypeSpec);
				message.append("' while retrieving document field '");
				message.append(key);
				message.append("'. Actual value was [");
				message.append(TextUtil.abbreviateObj(value, 100));
				message.append("]");
				if (value != null) {
					message.append(" of type '");
					message.append(value.getClass());
					message.append("'");
				}
				throw new IllegalArgumentException(message.toString(), e);
			}
		}

		@SuppressWarnings("unchecked")
		T casted = (T) convertedValue;
		return casted;
	}

	protected final A convertAndNormaliseValForGet(Object value, TypeDescriptor accessorType) {
		A convertedValue = getConvertedValue(value, accessorType);
		A normalised;

		if (normaliseOption.isDontNormalise()) {
			normalised = convertedValue;
		} else {
			normalised = EntryUtil.normaliseValueForGet(convertedValue, getConversionService());
		}

		return normalised;
	}
	
	protected final Object convertAndNormaliseValForPut(Object value, TypeDescriptor mutatorType) {
		Object normalised;
		
		if (mutatorType != null) {
			normalised = getConvertedValue(value, mutatorType);
		}
		else if (! normaliseOption.isDontNormalise()) {			
			normalised = EntryUtil.normaliseValueForPut(value, getConversionService());
		}
		else {
			normalised = value;
		}
		
		return normalised;
	}


	protected final void deleteCurrentEntry(IDataCursor cursor) {
		// Note: delete's return value indicates whether delete succeeded AND cursor not at end of document after invocation.
		// This means we can't rely on this value to see if deletion worked
		cursor.delete();
	}
	
	protected final DocumentImpl getDocument() {
		return document;
	}

}
