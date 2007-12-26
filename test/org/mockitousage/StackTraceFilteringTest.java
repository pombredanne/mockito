/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.util.ExtraMatchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.RequiresValidState;
import org.mockito.StateResetter;
import org.mockito.Strictly;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.StrictVerificationFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;

public class StackTraceFilteringTest extends RequiresValidState {
    
    private IMethods mock;

    @After
    public void resetState() {
        StateResetter.reset();
    }
    
    @Before
    public void setup() {
        resetState();
        mock = Mockito.mock(IMethods.class);
    }
    
    @Test
    public void shouldFilterStackTraceOnVerify() {
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerify"));
        }
    }
    
    @Test
    public void shouldFilterStackTraceOnVerifyNoMoreInteractions() {
        mock.oneArg(true);
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerifyNoMoreInteractions"));
        }
    }
    
    @Test
    public void shouldFilterStackTraceOnVerifyZeroInteractions() {
        mock.oneArg(true);
        try {
            verifyZeroInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerifyZeroInteractions"));
        }
    }
    
    @Test
    public void shouldFilterStacktraceOnMockitoException() {
        verify(mock);
        try {
            verify(mock).oneArg(true); 
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceOnMockitoException"));
        }
    }
    
    @Test
    public void shouldFilterStacktraceWhenStrictlyVerifying() {
        Strictly strictly = createStrictOrderVerifier(mock);
        mock.oneArg(true);
        mock.oneArg(false);
        
        try {
            strictly.verify(mock).oneArg(false); 
            fail();
        } catch (StrictVerificationFailure e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenStrictlyVerifying"));
        }
    }
    
    @Test
    public void shouldFilterStacktraceWhenStrictlyThrowsMockitoException() {
        try {
            createStrictOrderVerifier();
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenStrictlyThrowsMockitoException"));
        }
    }
    
    @Test
    public void shouldFilterStacktraceWhenStrictlyVerifies() {
        try {
            Strictly strictly = createStrictOrderVerifier(mock);
            strictly.verify(null);
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenStrictlyVerifies"));
        }
    }
    
    @Test
    public void shouldFilterStackTraceWhenThrowingExceptionFromMockHandler() {
        try {
            stub(mock.oneArg(true)).andThrow(new Exception());
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStackTraceWhenThrowingExceptionFromMockHandler"));
        }
    }
    
    @Test
    public void shouldShowProperExceptionStackTrace() throws Exception {
        stub(mock.simpleMethod()).andThrow(new RuntimeException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldShowProperExceptionStackTrace"));
        }
    }
}
