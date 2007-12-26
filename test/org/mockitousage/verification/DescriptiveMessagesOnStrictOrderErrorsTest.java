/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.verification;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.RequiresValidState;
import org.mockito.Strictly;
import org.mockito.exceptions.cause.TooLittleInvocations;
import org.mockito.exceptions.cause.UndesiredInvocation;
import org.mockito.exceptions.cause.WantedDiffersFromActual;
import org.mockito.exceptions.verification.StrictVerificationFailure;
import org.mockitousage.IMethods;

public class DescriptiveMessagesOnStrictOrderErrorsTest extends RequiresValidState {
    
    private IMethods one;
    private IMethods two;
    private IMethods three;
    private Strictly strictly;

    @Before
    public void setup() {
        one = Mockito.mock(IMethods.class);
        two = Mockito.mock(IMethods.class);
        three = Mockito.mock(IMethods.class);
        
        one.simpleMethod(1);
        one.simpleMethod(11);
        two.simpleMethod(2);
        two.simpleMethod(2);
        three.simpleMethod(3);
        
        strictly = createStrictOrderVerifier(one, two, three);
    }
    
    @Test
    public void shouldPrintStrictErrorAndShowBothWantedAndActual() {
        strictly.verify(one, atLeastOnce()).simpleMethod(1);
        
        try {
            strictly.verify(one).simpleMethod(999);
            fail();
        } catch (StrictVerificationFailure e) {
            String expected = 
                    "\n" +
                    "Strict verification failure" +
                    "\n" +
                    "Wanted invocation:" +
                    "\n" +
                    "IMethods.simpleMethod(999)"; 
            
            assertEquals(expected, e.getMessage());
            
            assertEquals(e.getCause().getClass(), WantedDiffersFromActual.class);
            
            String expectedCause = 
                "\n" +
                "Actual invocation:" +
                "\n" +
                "IMethods.simpleMethod(11)";
            
            assertEquals(expectedCause, e.getCause().getMessage());
        }
    }  
    
    @Test
    public void shouldPrintMethodThatWasNotInvoked() {
        strictly.verify(one).simpleMethod(1);
        strictly.verify(one).simpleMethod(11);
        strictly.verify(two, times(2)).simpleMethod(2);
        strictly.verify(three).simpleMethod(3);
        try {
            strictly.verify(three).simpleMethod(999);
            fail();
        } catch (StrictVerificationFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Strict verification failure" +
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "IMethods.simpleMethod(999)"; 
            assertEquals(expectedMessage, actualMessage);     
        }
    }   
    
    @Test
    public void shouldPrintTooManyInvocations() {
        strictly.verify(one).simpleMethod(1);
        strictly.verify(one).simpleMethod(11);
        try {
            strictly.verify(two, times(1)).simpleMethod(2);
            fail();
        } catch (StrictVerificationFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Strict verification failure" +
                    "\n" +
                    "IMethods.simpleMethod(2)" +
                    "\n" +
                    "Wanted 1 time but was 2"; 
            assertEquals(expectedMessage, actualMessage);      
            
            assertEquals(UndesiredInvocation.class, e.getCause().getClass());

            String expectedCause =
                "\n" +
                "Undesired invocation:";
            assertEquals(expectedCause, e.getCause().getMessage());
        }
    }  
    
    @Test
    public void shouldPrintTooLittleInvocations() {
        two.simpleMethod(2);
        
        strictly.verify(one, atLeastOnce()).simpleMethod(anyInt());
        strictly.verify(two, times(2)).simpleMethod(2);
        strictly.verify(three, atLeastOnce()).simpleMethod(3);
        
        try {
            strictly.verify(two, times(2)).simpleMethod(2);
            fail();
        } catch (StrictVerificationFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Strict verification failure" +
                    "\n" +
                    "IMethods.simpleMethod(2)" +
                    "\n" +
                    "Wanted 2 times but was 1";
            assertEquals(expectedMessage, actualMessage);
            
            assertEquals(e.getCause().getClass(), TooLittleInvocations.class);
            
            String expectedCause = 
                "\n" +
                "Too little invocations:";
            
            assertEquals(expectedCause, e.getCause().getMessage());
        }
    }   
}