package au.com.innodev.wmboost.data;

import static au.com.innodev.wmboost.data.TestUtil.newIDataWithValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.data.MBoolean;
import com.wm.data.MInteger;
import com.wm.data.MLong;

import au.com.innodev.wmboost.data.preset.DocumentFactories;
import au.com.innodev.wmboost.data.preset.Documents;
public class DocumentTest {

	private final DocumentFactory docFactory = DocumentFactories.getDefault();

	@Test
	public void testInsertionDeletion() {
		Document document = docFactory.create();

		assertTrue(document.isEmpty());
		assertEquals(document.getTotalEntries(), 0);
		assertTrue(CollectionUtils.isEqualCollection(Lists.newArrayList(), document.getKeys()));

		document.entry("val1").put("MyVal1");
		assertFalse(document.isEmpty());
		assertEquals(1, document.getTotalEntries());
		assertTrue(CollectionUtils.isEqualCollection(Lists.newArrayList("val1"), document.getKeys()));

		document.entry("val2").put("MyVal2");
		assertFalse(document.isEmpty());
		assertEquals(2, document.getTotalEntries());
		assertTrue(CollectionUtils.isEqualCollection(Lists.newArrayList("val1", "val2"), document.getKeys()));

		document.entry("val1").remove();
		assertFalse(document.isEmpty());
		assertEquals(1, document.getTotalEntries());
		assertTrue(CollectionUtils.isEqualCollection(Lists.newArrayList("val2"), document.getKeys()));

		document.entry("val2").remove();
		assertTrue(document.isEmpty());
		assertEquals(0, document.getTotalEntries());
		assertTrue(CollectionUtils.isEqualCollection(Lists.newArrayList(), document.getKeys()));
	}

	@Test
	public void testGetValue() {
		Integer originalValue = 5;
		Integer expected = 5;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1").getVal());
	}

	@Test
	public void testIntegerFromString() {
		String originalValue = "3";
		Integer expected = 3;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Integer.class).getVal());
	}

	@Test
	public void testIntegerFromLong() {
		Long originalValue = Long.valueOf(3);
		Integer expected = 3;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Integer.class).getVal());
	}

	@Test
	public void testIntegerFromInteger() {
		Integer originalValue = Integer.valueOf(3);
		Integer expected = 3;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Integer.class).getVal());
	}

	@Test
	public void testIntegerFromNull() {
		Integer originalValue = null;
		Integer expected = null;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Integer.class).getVal());
	}

	@Test
	public void testIntegerFromDecimal() {
		String originalValue = "3.14";

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		try {
			document.entry("value1", Integer.class).getVal();
			fail();
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().toLowerCase().contains("integer"));
			assertTrue(e.getMessage().contains("3.14"));
			assertTrue(e.getMessage().contains("value1"));
		}
	}

	@Test
	public void testBooleanFromTrue() {
		String originalValue = "true";
		Boolean expected = true;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Boolean.class).getVal());
	}

	@Test
	public void testBooleanFromFalse() {
		String originalValue = "false";
		Boolean expected = false;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Boolean.class).getVal());
	}

	@Test
	public void testBooleanFromYes() {
		String originalValue = "yes";
		Boolean expected = true;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Boolean.class).getVal());
	}

	@Test
	public void testBooleanFromInvalidString() {
		String originalValue = "notABool";

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		try {
			document.entry("value1", Boolean.class).getVal();
			fail();
		} catch (Exception e) {
			assertTrue(e.getMessage().toLowerCase().contains("boolean"));
			assertTrue(e.getMessage().contains("notABool"));
			assertTrue(e.getMessage().contains("value1"));
		}
	}

	@Test
	public void testBooleanFromEmptyString() {
		String originalValue = "";
		Boolean expectedValue = null;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expectedValue, document.entry("value1", Boolean.class).getVal());
	}

	@Test
	public void testSplitEntryOfIntegers() {
		List<Integer> expected = Lists.newArrayList(3,5);;
		IData idata = IDataFactory.create();
		idata.getCursor().insertAfter("myVal", "3");
		idata.getCursor().insertAfter("myVal", "5");

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.intsSplitEntry("myVal").getValOrEmpty());
	}
	
	@Test
	public void testSplitEntryOfBoolean() {
		List<Boolean> expected = Lists.newArrayList(true,false);
		IData idata = IDataFactory.create();
		idata.getCursor().insertAfter("myVal", "true");
		idata.getCursor().insertAfter("myVal", "false");

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.booleansSplitEntry("myVal").getValOrEmpty());
	}
	
	@Test
	public void testSplitEntryOfLongs() {
		List<Long> expected = Lists.newArrayList(3L,5L);;
		IData idata = IDataFactory.create();
		idata.getCursor().insertAfter("myVal", "3");
		idata.getCursor().insertAfter("myVal", "5");

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.longsSplitEntry("myVal").getValOrEmpty());
	}
	
	@Test
	public void testSplitEntryOfShorts() {
		Short s1 = 3;
		Short s2 = 5;
		List<Short> expected = Lists.newArrayList(s1,s2);
		IData idata = IDataFactory.create();
		idata.getCursor().insertAfter("myVal", "3");
		idata.getCursor().insertAfter("myVal", "5");

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.shortsSplitEntry("myVal").getValOrEmpty());
	}
	
	@Test
	public void testSplitEntryOfDoubles() {
		List<Double> expected = Lists.newArrayList(3.14,1.156);
		IData idata = IDataFactory.create();
		idata.getCursor().insertAfter("myVal", "3.14");
		idata.getCursor().insertAfter("myVal", "1.156");

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.doublesSplitEntry("myVal").getValOrEmpty());
	}
	
	@Test
	public void testSplitEntryOfFloats() {
		List<Float> expected = Lists.newArrayList(3.14f,1.156f);
		IData idata = IDataFactory.create();
		idata.getCursor().insertAfter("myVal", "3.14");
		idata.getCursor().insertAfter("myVal", "1.156");

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.floatsSplitEntry("myVal").getValOrEmpty());
	}
	
	@Test
	public void testSplitEntryOfBigDecimals() {
		List<BigDecimal> expected = Lists.newArrayList(new BigDecimal("3.14"), new BigDecimal("1.156"));
		IData idata = IDataFactory.create();
		idata.getCursor().insertAfter("myVal", "3.14");
		idata.getCursor().insertAfter("myVal", "1.156");

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.bigDecimalsSplitEntry("myVal").getValOrEmpty());
	}
	
	
	@Test
	public void testMBooleanToBoolean() {
		MBoolean originalValue = new MBoolean(true);
		Boolean expected = Boolean.TRUE;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Boolean.class).getVal());
	}

	@Test
	public void testEmptyString() {
		String originalValue = "";
		String expectedValue = "";
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expectedValue, document.stringEntry("value1").getVal());
	}

	@Test
	public void testAtomicLongToInteger() {
		AtomicLong originalValue = new AtomicLong(3);
		Integer expected = 3;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Integer.class).getVal());
	}
	
	@Test
	public void testBigIntegerToInteger() {
		Integer originalValue = 345;
		BigInteger expected = new BigInteger("345");
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", BigInteger.class).getVal());
	}

	@Test
	public void testMLongToIntger() {
		MLong originalValue = new MLong(345);
		Integer expected = 345;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Integer.class).getVal());
	}

	@Test
	public void testLongToMInteger() {
		Long originalValue = 67L;
		int expected = 67;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		MInteger value = document.entry("value1", MInteger.class).getVal();
		assertEquals(expected, value.intValue());
	}

	@Test
	public void testMLongToString() {
		Long originalValue = 678L;
		String expected = "678";

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.stringEntry("value1").getVal());
	}

	@Test
	public void testStringToMLong() {
		String originalValue = "91";
		long expected = 91;

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", MLong.class).getVal().longValue());
	}

	private static class Test1 {
		
	}
	
	@Test
	public void testStringFromCustomObject() {
		Test1 originalValue = new Test1();
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		try {
			document.stringEntry("value1").getVal();
			fail();
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().toLowerCase().contains("string"));
			assertTrue(e.getMessage().contains("value1"));
		}
	}

	@Test
	public void testStringFromInteger() {
		Integer originalValue = 3;
		String expected = "3";
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.stringEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfBoolean() {
		String originalValue = "true";
		Boolean expected = true;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.booleanEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfInteger() {
		String originalValue = "3";
		Integer expected = 3;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.intEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfLong() {
		String originalValue = "3";
		Long expected = 3L;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.longEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfShort() {
		String originalValue = "3";
		Short expected = 3;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.shortEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfFloat() {
		String originalValue = "3.14";
		Float expected = 3.14F;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.floatEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfDouble() {
		String originalValue = "3.14";
		Double expected = 3.14;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.doubleEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfBigDecimal() {
		String originalValue = "3.14";
		BigDecimal expected = BigDecimal.valueOf(3.14);
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.bigDecimalEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfCharacter() {
		String originalValue = "W";
		Character expected = 'W';
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Character.class).getVal());
	}	

	@Test
	public void testEntryOfEnum() {
		String originalValue = "CEILING";
		RoundingMode expected = RoundingMode.CEILING;
		
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", RoundingMode.class).getVal());
		
	}
	
	@Test
	public void testEntryOfCollection() {
		Object[] originalValues = {"Hello", 5};
		List<Object> expected = Lists.<Object>newArrayList("Hello",5);;
		IData idata = newIDataWithValue(originalValues);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.collectionEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfBooleans() {
		String[] originalValues = {"true", "false", "true"};
		List<Boolean> expected = Lists.newArrayList(true, false, true);;
		IData idata = newIDataWithValue(originalValues);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.booleansEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfIntegers() {
		String[] originalValues = {"3", "5"};
		List<Integer> expected = Lists.newArrayList(3,5);;
		IData idata = newIDataWithValue(originalValues);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.intsEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfLongs() {
		String[] originalValues = {"3", "5"};
		List<Long> expected = Lists.newArrayList(3L,5L);;
		IData idata = newIDataWithValue(originalValues);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.longsEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfShorts() {
		String[] originalValues = {"3", "5"};
		List<Short> expected = Lists.newArrayList(Short.valueOf("3"),Short.valueOf("5"));
		IData idata = newIDataWithValue(originalValues);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.shortsEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfFloats() {
		String[] originalValues = {"3.14", "5.8"};
		List<Float> expected = Lists.newArrayList(3.14F, 5.8F);
		IData idata = newIDataWithValue(originalValues);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.floatsEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfDoubles() {
		String[] originalValues = {"3.14", "5.8"};
		List<Double> expected = Lists.newArrayList(3.14, 5.8);
		IData idata = newIDataWithValue(originalValues);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.doublesEntry("value1").getVal());
	}
	
	@Test
	public void testEntryOfBigDecimals() {
		String[] originalValues = {"3.14", "5.8"};
		List<BigDecimal> expected = Lists.newArrayList(BigDecimal.valueOf(3.14), BigDecimal.valueOf(5.8));
		IData idata = newIDataWithValue(originalValues);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.bigDecimalsEntry("value1").getVal());
	}	

	
	@Test
	public void testDateToString() {
		String strValue = "2017-01-01T00:00:00.000Z";
		String expected = strValue;
		
		Date instant = DateTimeUtils.toDate(Instant.parse(strValue));
		
		IData idata = newIDataWithValue(instant);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.stringEntry("value1").getVal());
	}
	
	@Test
	public void testStringToLegacyDate() {
		String strValue = "2017-01-01T00:00:00.000Z";
		Date expectedDate = DateTimeUtils.toDate(Instant.parse(strValue));
		
		IData idata = newIDataWithValue(strValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expectedDate, document.legacyDateEntry("value1").getVal());
	}

	

	@Test
	public void testMandatoryStringFromInteger() {
		Integer originalValue = 3;
		String expected = "3";
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.stringEntry("value1").getNonNullVal());
	}

	@Test
	public void testMandatoryStringNotPresent() {
		IData idata = IDataFactory.create();

		Document document = docFactory.wrap(idata);
		try {
			document.stringEntry("someValue").getNonNullVal();
			fail();
		} catch (InexistentEntryException e) {
			assertTrue(e.getMessage().toLowerCase().contains("entry doesn't exist"));
			assertTrue(e.getMessage().contains("someValue"));
		}
	}

	@Test
	public void testMandatoryValue() {
		Integer originalValue = 5;
		Long expected = 5L;
		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.entry("value1", Long.class).getNonNullVal());
	}

	@Test
	public void testMandatoryValueNotPresent() {
		IData idata = IDataFactory.create();

		Document document = docFactory.wrap(idata);
		try {
			document.entry("someValue", Integer.class).getNonNullVal();
			fail();
		} catch (InexistentEntryException e) {
			assertTrue(e.getMessage().toLowerCase().contains("entry doesn't exist"));
			assertTrue(e.getMessage().contains("someValue"));
		}
	}

	@Test
	public void testMandatoryValueNull() {
		IData idata = newIDataWithValue(null);

		Document document = docFactory.wrap(idata);
		try {
			document.entry("value1", Integer.class).getNonNullVal();
			fail();
		} catch (UnexpectedEntryValueException e) {
			assertTrue(e.getMessage().toLowerCase().contains("null value was found"));
			assertTrue(e.getMessage().contains("value1"));
		}
	}

	@Test
	public void testStringListFromStringArray() {
		Integer[] originalValue = new Integer[] { 1, 2 };
		List<String> expected = Lists.newArrayList("1", "2");

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.stringsEntry("value1").getVal());
	}

	@Test
	public void testIntegersFromStringArray() {
		String[] originalValue = new String[] { "1", "2" };
		List<Integer> expected = Lists.newArrayList(1, 2);

		IData idata = newIDataWithValue(originalValue);

		Document document = docFactory.wrap(idata);
		assertEquals(expected, document.collectionEntry("value1", Integer.class).getVal());
	}
	
	
	

	@Test
	public void testgetValueWithUnconvertableType() {
		IData idata = newIDataWithValue("hello");
		Document document = docFactory.wrap(idata);
		try {
			document.entry("value1", BufferedReader.class).getVal();
			fail();
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().toLowerCase().contains("bufferedreader"));
			assertTrue(e.getMessage().contains("value1"));
		}
	}

	@Test
	public void testNestedResource() {
		IData nestedidata = newIDataWithValue("inside!");

		IData idata = IDataFactory.create();
		IDataCursor cursor = idata.getCursor();
		IDataUtil.put(cursor, "subDocument", nestedidata);
		cursor.destroy();

		Document pipelineRes = docFactory.wrap(idata);
		Document nestedRes = pipelineRes.docEntry("subDocument").getVal();
		{
			assertEquals("inside!", nestedRes.stringEntry("value1").getVal());
		}
	}

	@Test
	public void testOptionalNestedResource() {
		IData idata = newIDataWithValue("someValue");

		Document res = docFactory.wrap(idata);
		assertNull(res.docEntry("inexsitentDoc").getValOrNull(NullValHandling.RETURN_NULL));
	}


	@Test
	public void testPutAsString() {
		IData idata = IDataFactory.create();
		Document document = docFactory.wrap(idata);
		document.stringEntry("value1").putConverted(3);

		// confirm value has actually been added
		IDataCursor cursor = document.getIData().getCursor();
		assertEquals("3", IDataUtil.get(cursor, "value1"));
		assertEquals(1, IDataUtil.size(cursor));
	}

	@Test
	public void testPut() {
		IData idata = IDataFactory.create();
		Document document = docFactory.wrap(idata);
		document.entry("value1").put(3);

		// confirm value has actually been added
		IDataCursor cursor = document.getIData().getCursor();
		assertEquals(3, IDataUtil.get(cursor, "value1"));
		assertEquals(1, IDataUtil.size(cursor));
	}

	@Test
	public void testPutIntegerList() {
		Integer[] expected = {3, 1, 4};
		List<Integer> input = Lists.newArrayList(3, 1, 4);
		
		Document document = docFactory.create();
		document.intsEntry("value1").put(input);

		// confirm value has actually been added
		IDataCursor cursor = document.getIData().getCursor();
		Integer[] returnedValue = (Integer[]) IDataUtil.get(cursor, "value1");
		assertArrayEquals(expected, returnedValue);
	}
	
	
	@Test
	public void testPutCollectionListFromIntegerList() {
		Object[] expected = {3, 1, 4};
		List<Integer> input = Lists.newArrayList(3, 1, 4);
		
		Document document = docFactory.create();
		
		// This compiles because covariants re OK as input, e.g., store Integer elements in an Object collection entry
		document.collectionEntry("value1").put(input);

		// confirm value has actually been added
		IDataCursor cursor = document.getIData().getCursor();
		Object[] returnedValue = (Object[]) IDataUtil.get(cursor, "value1");
		assertArrayEquals(expected, returnedValue);
	}
	
	@Test
	public void testGetValOrDefault_CovariantAndUnmodifiable() {
		List<Object> expected = Lists.<Object>newArrayList(3, 1, 4);
		List<Integer> input = Lists.newArrayList(3, 1, 4);
		
		Document document = docFactory.create();
		
		// This compiles because we accept a covariant, e.g., List<Integer> on an Object collection default value
		List<Object> returnedValue = document.collectionEntry("value1").getValOrDefault(input, NullValHandling.RETURN_NULL);
		
		assertEquals(expected, returnedValue);
		
		// Ensure returned list is unmodifieable
		try {
			returnedValue.add("A");
			fail();
		}
		catch (UnsupportedOperationException e) {
			// success
		}
		
		// but ensure original list hasn't been altered - an Integer List shouldn't contain a String
		assertEquals(3, input.size()); 
		
	}
	
	/*
	 * Ensures that an IData instance is retrieved as a Document, even through entry(Object)  
	 */
	@Test
	public void testGetDocumentFromObjectEntry() {
		IData nestedIData = newIDataWithValue("myNestedVal");
		
		IData topIData = newIDataWithValue(nestedIData);
		Document topDoc = docFactory.wrap(topIData);		
		
		Object retrievedNestedIData = topDoc.entry("value1").getVal();
		
		assertTrue(retrievedNestedIData instanceof Document);
		assertEquals("myNestedVal", ((Document) retrievedNestedIData).entry("value1").getVal());
	}
	
	/*
	 * Ensures that a an IData instance is retrieved when explicitly asking for that type  
	 */
	@Test
	public void testGetIDataFromObjectEntry() {
		IData nestedIData = newIDataWithValue("field1");
		
		IData topIData = newIDataWithValue(nestedIData);
		Document topDoc = docFactory.wrap(topIData);		
		
		Object retrievedNestedIData = topDoc.entry("value1", IData.class).getVal();
		
		assertTrue(retrievedNestedIData instanceof IData);
	}
	
	/*
	 * Ensures that a an IData[] instance is retrieved as a List of documents, even through entry(Object)  
	 */
	@Test
	public void testGetDocumentsFromObjectEntry() {
		IData nestedIData1 = newIDataWithValue("A");
		IData nestedIData2 = newIDataWithValue("B");
		IData[] nestedArray = {nestedIData1, nestedIData2};
		
		IData topIData = newIDataWithValue(nestedArray);
		Document topDoc = docFactory.wrap(topIData);		
		
		Object retrievedDocs = topDoc.entry("value1").getVal();
		
		assertTrue(retrievedDocs instanceof List);
		List<?> retrievedList = (List<?>) retrievedDocs;
				
		Object elem1 = retrievedList.get(0);		
		assertTrue(elem1 instanceof Document);
		assertEquals("A", ((Document)elem1).entry("value1").getVal());
		
		Object elem2 = retrievedList.get(1);		
		assertTrue(elem2 instanceof Document);
		assertEquals("B", ((Document)elem2).entry("value1").getVal());
	}
		
	/*
	 * Ensures that a an Integer[] instance is retrieved as a List of integers, even through entry(Object)  
	 */
	@Test
	public void testGetFromIntegerArrayEntry() {
		
		IData iData = newIDataWithValue(new Integer[] {3,5});
		Document topDoc = docFactory.wrap(iData);		
		
		Object retrievedDocs = topDoc.entry("value1").getVal();
		
		assertTrue(retrievedDocs instanceof List);
		List<?> retrievedList = (List<?>) retrievedDocs;
				
		Object elem1 = retrievedList.get(0);		
		assertTrue(elem1 instanceof Integer);
		assertEquals(3, elem1);
		
		Object elem2 = retrievedList.get(1);		
		assertTrue(elem2 instanceof Integer);
		assertEquals(5, elem2);
	}
	
	@Test
	public void testPutConvertedIntegers() {
		String[] expected = {"3", "1", "4"};
		List<Integer> input = Lists.newArrayList(3, 1, 4);
		
		Document document = docFactory.create();
		document.stringsEntry("value1").putConverted(input);

		// confirm value has actually been added
		IDataCursor cursor = document.getIData().getCursor();
		String[] returnedValue = (String[]) IDataUtil.get(cursor, "value1");
		assertArrayEquals(expected, returnedValue);
	}
	
	/*
	 * Ensures that a Document instance is put as an IData, even when invoking the untyped entry() method.  
	 */
	@Test
	public void testPutDocumentToObjectEntry() {
		IData idata = newIDataWithValue("field1");
		Document nested = docFactory.wrap(idata);		
		
		Document document = docFactory.create();
		document.entry("nestedDoc").put(nested);
		
		Object storedNestedDoc = IDataUtil.get(document.getIData().getCursor(), "nestedDoc");
		
		assertTrue(storedNestedDoc instanceof IData);
	}
	
	/*
	 * Ensures that list of Document instances is put as IData[], even when invoking the untyped entry() method.  
	 */
	@Test
	public void testPutDocumentsToObjectEntry() {
		IData idata1 = newIDataWithValue("A");
		Document nested1 = docFactory.wrap(idata1);		
		
		IData idata2 = newIDataWithValue("B");
		Document nested2 = docFactory.wrap(idata2);
		
		List<Object> docs = Lists.<Object>newArrayList(nested1, nested2);
		
		Document document = docFactory.create();
		document.entry("nestedDoc").put(docs);
		
		Object storedNestedDocs = IDataUtil.get(document.getIData().getCursor(), "nestedDoc");
		assertTrue(storedNestedDocs instanceof IData[]);
		
		IData[] retrievedArray = (IData[]) storedNestedDocs;
				
		assertEquals("A", IDataUtil.get(retrievedArray[0].getCursor(), "value1"));
		
		assertEquals("B", IDataUtil.get(retrievedArray[1].getCursor(), "value1"));
		
	}
		
	@Test
	public void testRemove() {
		IData idata = newIDataWithValue("anyValue");
		Document document = docFactory.wrap(idata);
		assertEquals(1, IDataUtil.size(document.getIData().getCursor()));

		// action
		document.entry("value1").remove();

		// confirm value has been removed
		assertEquals(0, IDataUtil.size(document.getIData().getCursor()));
		assertNull(IDataUtil.get(document.getIData().getCursor(), "value1"));
	}

	@Test
	public void testRemoveFailed() {
		IData idata = newIDataWithValue("anyValue");
		Document document = docFactory.wrap(idata);
		assertEquals(1, IDataUtil.size(document.getIData().getCursor()));

		try {
			document.entry("nonExistingKey").remove();
			fail();
		} catch (InexistentEntryException e) {
			// success
		}

		// confirm no value has been removed
		assertEquals(1, IDataUtil.size(document.getIData().getCursor()));
	}
	
	@Test
	public void testRemoveFailedExplicitParam() {
		IData idata = newIDataWithValue("anyValue");
		Document document = docFactory.wrap(idata);
		assertEquals(1, IDataUtil.size(document.getIData().getCursor()));

		try {
			document.entry("nonExistingKey").remove(RemoveEntryOption.STRICT);
			fail();
		} catch (InexistentEntryException e) {
			// success
		}

		// confirm no value has been removed
		assertEquals(1, IDataUtil.size(document.getIData().getCursor()));
	}
	
	@Test
	public void testRemoveFailedIgnore() {
		IData idata = newIDataWithValue("anyValue");
		Document document = docFactory.wrap(idata);
		assertEquals(1, IDataUtil.size(document.getIData().getCursor()));

		document.entry("nonExistingKey").remove(RemoveEntryOption.LENIENT);

		// confirm no value has been removed
		assertEquals(1, IDataUtil.size(document.getIData().getCursor()));
	}
	
	@Test
	public void testGetTopLevelKeys() {
		List<String> expected = Lists.newArrayList("value1", "value2");

		IData idata = IDataFactory.create();
		IDataCursor cursor = idata.getCursor();
		IDataUtil.put(cursor, "value1", "something");
		IDataUtil.put(cursor, "value2", "another one");
		cursor.destroy();

		Document document = docFactory.wrap(idata);
		CollectionUtils.isEqualCollection(expected, document.getKeys());
	}

	@Test
	public void testPresence_PresentNonNull() {
		IData idata = newIDataWithValue("Hello");
		Document document = docFactory.wrap(idata);
		ItemEntry<String> value1Presence = document.stringEntry("value1");
		assertTrue(value1Presence.isAssigned());
		assertEquals("Hello", value1Presence.getVal());
	}

	@Test
	public void testPresence_PresentButNull() {
		IData idata = newIDataWithValue(null);
		Document document = docFactory.wrap(idata);
		ItemEntry<String> value1Presence = document.stringEntry("value1");
		assertTrue(value1Presence.isAssigned());
		assertNull(value1Presence.getVal());
	}

	@Test
	public void testPresence_Absent() {
		IData idata = IDataFactory.create();
		Document document = docFactory.wrap(idata);
		ItemEntry<String> value1Presence = document.stringEntry("inexistentKey");
		assertFalse(value1Presence.isAssigned());
	}

	@Test
	public void testNestedDoc() {

		// setup
		Document nested = docFactory.create();
		nested.stringEntry("a1").put("val-a1");
		nested.stringEntry("a2").put("val-a2");

		Document top = docFactory.create();
		top.docEntry("nested").put(nested);

		// verify
		IData topIDataObj = top.getIData();
		IData topIData = (IData) topIDataObj;

		Object nestedObj = IDataUtil.get(topIData.getCursor(), "nested");
		assertTrue("Actual type was" + nestedObj.getClass(), nestedObj instanceof IData);

		IData nestedIData = (IData) nestedObj;

		assertEquals("val-a1", IDataUtil.get(nestedIData.getCursor(), "a1"));
		assertEquals("val-a2", IDataUtil.get(nestedIData.getCursor(), "a2"));
	}

	@Test
	public void testInsertNullDocument() {
		Document document = docFactory.create();
		document.docEntry("nullDoc").put(null);
		assertNull(IDataUtil.get(document.getIData().getCursor(), "nullDoc"));
	}

	@Test
	public void testNestedDocs() {
		Document nestedA = docFactory.create();
		nestedA.stringEntry("a1").put("val-a1");
		nestedA.stringEntry("a2").put("val-a2");

		Document nestedB = docFactory.create();
		nestedB.stringEntry("b1").put("val-b1");
		nestedB.stringEntry("b2").put("val-b2");

		List<Document> documents = Lists.newArrayList(nestedA, nestedB);
		// Document[] docArray = documents.toArray(new Document[0]);

		Document top = docFactory.create();
		top.docsEntry("table").put(documents);

		Object tableIDataObj = IDataUtil.get(top.getIData().getCursor(), "table");

		assertTrue(IData[].class.isInstance(tableIDataObj));

		IData[] iDataArray = (IData[]) tableIDataObj;
		assertEquals(2, iDataArray.length);

		assertEquals("val-a1", IDataUtil.get(iDataArray[0].getCursor(), "a1"));
		assertEquals("val-a2", IDataUtil.get(iDataArray[0].getCursor(), "a2"));

		assertEquals("val-b1", IDataUtil.get(iDataArray[1].getCursor(), "b1"));
		assertEquals("val-b2", IDataUtil.get(iDataArray[1].getCursor(), "b2"));
	}

	@Test
	public void testPresence_NestedDocs() {

		IData[] nestedIDatas = new IData[] { newIDataWithValue("nestedValue1"), newIDataWithValue("nestedValue2") };

		IData idata = newIDataWithValue(nestedIDatas);

		Document topDoc = docFactory.wrap(idata);
		CollectionEntry<Document> p = topDoc.docsEntry("value1");
		
		assertTrue(p.isAssigned());
		
		Iterator<Document> it = p.getVal().iterator();
		Document doc1 = it.next();
		assertEquals("nestedValue1", doc1.entry("value1").getVal());
		
		Document doc2 = it.next();
		assertEquals("nestedValue2", doc2.entry("value1").getVal());
	}
	
	@Test
	public void testGetAllEntries() {
		IData idata = IDataFactory.create();
		IDataCursor cursor = idata.getCursor();
		cursor.insertAfter("x", "0");
		cursor.insertAfter("z", "1");
		cursor.insertAfter("y", "2");
		cursor.insertAfter("x", "3");
		cursor.insertAfter("z", "4");
		
		Document document = Documents.wrap(idata);
		EntryIterableResource docEntriesResource = document.getAllEntries();
	    try {
			Iterator<KeyValue> it = docEntriesResource.iterator();
			
			assertTrue(it.hasNext());
			KeyValue keyValue0 = it.next();
			assertEquals(keyValue0.getKey(), "x");
			assertEquals(keyValue0.getValue(), "0");
			
			assertTrue(it.hasNext());
			KeyValue keyValue1 = it.next();
			assertEquals(keyValue1.getKey(), "z");
			assertEquals(keyValue1.getValue(), "1");
			
			assertTrue(it.hasNext());
			KeyValue keyValue2 = it.next();
			assertEquals(keyValue2.getKey(), "y");
			assertEquals(keyValue2.getValue(), "2");
			
			assertTrue(it.hasNext());
			KeyValue keyValue3 = it.next();
			assertEquals(keyValue3.getKey(), "x");
			assertEquals(keyValue3.getValue(), "3");
			
			assertTrue(it.hasNext());
			KeyValue keyValue4 = it.next();
			assertEquals(keyValue4.getKey(), "z");
			assertEquals(keyValue4.getValue(), "4");
			
			assertFalse(it.hasNext());
		}
		finally {
			docEntriesResource.close();
		}
	}
	
	@Test
	public void testGetUnitEntries() {
		IData idata = IDataFactory.create();
		IDataCursor cursor = idata.getCursor();
		cursor.insertAfter("x", "0");
		cursor.insertAfter("z", "1");
		cursor.insertAfter("y", "2");
		cursor.insertAfter("x", "3");
		cursor.insertAfter("z", "4");
		
		Document document = Documents.wrap(idata);
		
		Iterable<KeyValue> unitEntries = document.getUnitEntries();
		Iterator<KeyValue> it = unitEntries.iterator();
		
		assertTrue(it.hasNext());
		KeyValue keyValue0 = it.next();
		assertEquals(keyValue0.getKey(), "x");
		assertEquals(keyValue0.getValue(), "0");
		
		assertTrue(it.hasNext());
		KeyValue keyValue1 = it.next();
		assertEquals(keyValue1.getKey(), "z");
		assertEquals(keyValue1.getValue(), "1");
		
		assertTrue(it.hasNext());
		KeyValue keyValue2 = it.next();
		assertEquals(keyValue2.getKey(), "y");
		assertEquals(keyValue2.getValue(), "2");
			
		assertFalse(it.hasNext());
	}
	
	@Test
	public void testClear() {
		Document document = docFactory.create();
		document.entry("z").put("1");
		document.entry("y").put("2");
		document.entry("x").put("3");
		
		assertEquals(3, document.getTotalEntries());
		assertTrue(document.getAllEntries().iterator().hasNext());
		
		document.clear();
		
		assertEquals(0, document.getTotalEntries());
		assertFalse(document.getAllEntries().iterator().hasNext());
	}
	
	
}