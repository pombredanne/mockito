package org.mockitousage.basicapi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMock;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockitoutil.TestBase;

import java.util.*;

public class MockInjectionTest extends TestBase {

	private SuperUnderTest superUnderTestWithoutInjection = new SuperUnderTest();
	@InjectMock private SuperUnderTest superUnderTest = new SuperUnderTest();
	@InjectMock private BaseUnderTest baseUnderTest = new BaseUnderTest();
	@InjectMock private SubUnderTest subUnderTest = new SubUnderTest();
	@InjectMock private OtherBaseUnderTest otherBaseUnderTest = new OtherBaseUnderTest();
	@Mock private Map map;
	@Mock private List list;
	@Mock private Set set;
	@Mock private Set histogram1;
	@Mock private Set histogram2;
	@Spy private TreeSet searchTree = new TreeSet();

	@Before
	public void init() {
		// initMocks called in TestBase Before method, so instances ar not the same
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldInjectMocksIfAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertSame(list, superUnderTest.getAList());
	}

	@Test
	public void shouldNotInjectIfNotAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertNull(superUnderTestWithoutInjection.getAList());
	}

	@Test
	public void shouldInjectMocksForClassHierarchyIfAnnotated() {
		MockitoAnnotations.initMocks(this);
		assertSame(list, baseUnderTest.getAList());
		assertSame(map, baseUnderTest.getAMap());
	}

	@Test
	public void shouldInjectMocksByName() {
		MockitoAnnotations.initMocks(this);
		assertSame(histogram1, subUnderTest.getHistogram1());
		assertSame(histogram2, subUnderTest.getHistogram2());
	}

	@Test
	public void shouldInjectSpies() {
		MockitoAnnotations.initMocks(this);
		assertSame(searchTree, otherBaseUnderTest.getSearchTree());
	}

	class SuperUnderTest {

		private List aList;

		public List getAList() {
			return aList;
		}
	}

	class BaseUnderTest extends SuperUnderTest {
		private Map aMap;

		public Map getAMap() {
			return aMap;
		}
	}

	class OtherBaseUnderTest extends SuperUnderTest {
		private TreeSet searchTree;

		public TreeSet getSearchTree() {
			return searchTree;
		}
	}

	class SubUnderTest extends BaseUnderTest {
		private Set histogram1;
		private Set histogram2;

		public Set getHistogram1() {
			return histogram1;
		}

		public Set getHistogram2() {
			return histogram2;
		}
	}
}
