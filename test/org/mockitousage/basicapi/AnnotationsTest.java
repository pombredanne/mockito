/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.basicapi;

import static org.mockito.Mockito.*;
import static org.mockito.internal.stubbing.defaultanswers.Answers.RETURNS_MOCKS;
import static org.mockito.internal.stubbing.defaultanswers.Answers.RETURNS_DEFAULTS;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.stubbing.defaultanswers.Answers;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

@SuppressWarnings("unchecked")
public class AnnotationsTest extends TestBase {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface NotAMock {}

    @Mock List list;
    @Mock final Map map = new HashMap();
        
    @NotAMock Set notAMock;

    @SuppressWarnings("deprecation")
    @MockitoAnnotations.Mock List listTwo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldInitMocks() throws Exception {
        list.clear();
        map.clear();
        listTwo.clear();

        verify(list).clear();
        verify(map).clear();
        verify(listTwo).clear();
    }

    @Test
    public void shouldScreamWhenInitializingMocksForNullClass() throws Exception {
        try {
            MockitoAnnotations.initMocks(null);
            fail();
        } catch (MockitoException e) {
            assertEquals("testClass cannot be null. For info how to use @Mock annotations see examples in javadoc for MockitoAnnotations class",
                    e.getMessage());
        }
    }

    @Test
    public void shouldLookForAnnotatedMocksInSuperClasses() throws Exception {
        Sub sub = new Sub();
        MockitoAnnotations.initMocks(sub);

        assertNotNull(sub.getMock());
        assertNotNull(sub.getBaseMock());
        assertNotNull(sub.getSuperBaseMock());
    }
    
    @Mock(answer = RETURNS_MOCKS, name = "i have a name") IMethods namedAndReturningMocks;
    @Mock(answer = RETURNS_DEFAULTS) IMethods returningDefaults;
    @Mock(extraInterfaces = {List.class}) IMethods hasExtraInterfaces;
    @Mock() IMethods noExtraConfig;

    @Test
    public void shouldInitMocksWithGivenSettings() throws Exception {
        assertEquals("i have a name", namedAndReturningMocks.toString());
        assertNotNull(namedAndReturningMocks.iMethodsReturningMethod());
       
        assertEquals("returningDefaults", returningDefaults.toString());
        assertEquals(0, returningDefaults.intReturningMethod()); 
        
        assertTrue(hasExtraInterfaces instanceof List);
        
        assertEquals(0, noExtraConfig.intReturningMethod());        
    }

    class SuperBase {
        @Mock private IMethods mock;

        public IMethods getSuperBaseMock() {
            return mock;
        }
    }

    class Base extends SuperBase {
        @Mock private IMethods mock;

        public IMethods getBaseMock() {
            return mock;
        }
    }

    class Sub extends Base {
        @Mock private IMethods mock;

        public IMethods getMock() {
            return mock;
        }
    }
}