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

import java.util.List;

/**
 * <p>
 * A reference to a document entry identified by a key and whose value is a
 * collection. Allows access and modification of document entry.
 * <p>
 * This type is suitable when the entry's value consists in a collection of
 * elements. In contrast, a {@link DocEntry} instance is used for an entry that contains a
 * single value.
 *
 * <p>
 * The behaviour of setters, getters, etc. in this class is very similar to
 * {@link DocEntry}. Refer to its documentation for more information.
 * <h3>Other considerations</h3>
 * <p> This class exposes values as collections for
 * simplicity. Internally, values are stored as arrays, as expected by
 * webMethods.
 * <p>
 * This class manipulates only the first entry identified by the key. This is
 * the norm in most uses cases. In rare situations where multiple entries for a
 * key may exist and processing is required for all those entries, use of
 * {@link ScatteredEntry} would be more appropriate.
 *
 * @param <E>
 *            collection element type to treat the entry value as
 */
public interface CollectionDocEntry<E> {

	/** -------- Accessors ------------------------------------------ */

	/**
	 * Returns the key associated to the entry being referenced
	 * 
	 * @return key associated to the entry
	 */
	String getKey();

	/**
	 * Returns whether the key has been assigned to an entry the document. This
	 * method is used to find out whether the document contains an entry with
	 * that key.
	 * 
	 * @return true if the key has been assigned; false, otherwise
	 */
	boolean isAssigned();

	/**
	 * Returns the <em>value</em> component of the key/value entry in the
	 * document.
	 * <p>
	 * Use this method when you expect an entry with the key <em>to exist</em>.
	 * <p>
	 * If an entry with the key doesn't exist, an exception is thrown.
	 *
	 * @return entry value
	 * @throws InexistentEntryException
	 *             if there's no entry associated with the key
	 * 
	 */
	List<E> getVal() throws InexistentEntryException;

	/**
	 * Returns the <em>value</em> component of the key/value entry in the
	 * document.
	 * <p>
	 * Use this method when you expect an entry with the key <em>to exist</em>
	 * and for the value to <em>non-null</em> and <em>non-empty</em>.
	 * <p>
	 * If an entry with the key doesn't exist or if it contains a null value, an
	 * exception is thrown.
	 * 
	 * @return entry value
	 * @throws InexistentEntryException
	 *             if there's no entry associated with the key
	 * @throws UnexpectedEntryValueException
	 *             if the entry contains a null value or is empty
	 * @see #getVal()
	 */
	List<E> getNonEmptyVal() throws InexistentEntryException, UnexpectedEntryValueException;

	// TODO keep both 'orNull' and 'orEmpy'?

	/**
	 * Returns the <em>value</em> component of the key/value entry in the
	 * document.
	 * <p>
	 * Use this method when you don't know if the entry exists and you want an
	 * empty list to be returned if it doesn't.
	 * 
	 * @return entry value
	 * 
	 * @see #getVal()
	 */
	List<E> getValOrEmpty();

	/**
	 * Returns the <em>value</em> component of the key/value entry in the
	 * document.
	 * <p>
	 * Use this method when you don't know if the entry exists and you want
	 * {@code defaultValue} to be returned if it doesn't.
	 * 
	 * @param defaultValue
	 *            value to return if entry doesn't exist
	 * 
	 * @return entry value
	 * 
	 * @see #getVal()
	 */
	List<E> getValOrDefault(List<? extends E> defaultValue);

	/** -------- Mutators ------------------------------------------ */

	/**
	 * Sets the provided {@code value} as the entry's value.
	 * <p>
	 * If an entry identified with the key doesn't exist, a value is created.
	 * Otherwise, the entry's value is replaced with the provided {@code value}.
	 * 
	 * @param value
	 *            the new value to set for the entry
	 */
	void put(Iterable<? extends E> value);

	/**
	 * Sets the provided {@code value} as the entry's value
	 * 
	 * @param value
	 *            the new value to set for the entry
	 * 
	 * @see #put(Iterable)
	 */
	void put(E[] value);

	/**
	 * Converts the provided value and sets the converted value as the entry's
	 * value.
	 * <p>
	 * Use this method in cases where the value type is different to the type
	 * you want to be stored in the entry. For example, if you wanted an
	 * {@code Integer} value of 5 to be stored as a {@code String} in the entry,
	 * you could use the following code:
	 * 
	 * <pre>
	 * doc.entryOfString("total").putConverted(5);
	 * </pre>
	 * <p>
	 * If the provided {@code value} can't be converted to the entry's type, an
	 * exception is thrown.
	 * 
	 * @param value
	 *            the new value to set for the entry
	 * 
	 * @see #put(Iterable)
	 */
	void putConverted(Iterable<?> value);

	/**
	 * Deletes they entry identified by the elementís key. 
	 * 
	 * <p>Note that type used for the entry (e.g. String) is not taken into account
	 * when removing the entry. It's only done by key.
	 * 
	 * <p>If multiple elements exist in the IData
	 * with the given key, only the first occurrence of the key is deleted.
	 * 
	 */
	void remove();
}
