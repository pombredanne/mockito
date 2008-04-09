/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.examples.configure.withbaseclass;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.configuration.ConfigurationSupport;
import org.mockito.configuration.MockitoConfiguration;
import org.mockito.configuration.ReturnValues;
import org.mockito.invocation.InvocationOnMock;

public class MakesMocksNotToReturnNulls {
    
    @Before
    public void configureMockito() {
        //setting custom return values
        MockitoConfiguration.instance().setReturnValues(new MyDefaultReturnValues());
        //initializing annotated mocks
        MockitoAnnotations.initMocks(this);
    }
    
    @After
    public void resetReturnValuesToDefaults() {
        //I don't want mocks from other tests to be reconfigured
        MockitoConfiguration.instance().resetReturnValues();
    }
    
    private final class MyDefaultReturnValues extends ConfigurationSupport implements ReturnValues {
        public Object valueFor(InvocationOnMock invocation) {
            Class<?> returnType = invocation.getMethod().getReturnType();
            Object defaultReturnValue = defaultValueFor(invocation);
            if (defaultReturnValue != null) {
                return defaultReturnValue;
            } else if (!isMockable(returnType)) { 
                return Mockito.mock(returnType);
            } else {
                return defaultReturnValue;
            }
        }
    }
}