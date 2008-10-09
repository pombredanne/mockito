/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.sf.cglib.proxy.MethodProxy;

import org.mockito.internal.configuration.Configuration;
import org.mockito.internal.creation.MockAwareInterceptor;
import org.mockito.internal.invocation.AllInvocationsFinder;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MatchersBinder;
import org.mockito.internal.progress.DeprecatedOngoingStubbing;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.NewOngoingStubbing;
import org.mockito.internal.progress.VerificationMode;
import org.mockito.internal.progress.VerificationModeImpl;
import org.mockito.internal.stubbing.DoesNothing;
import org.mockito.internal.stubbing.MockitoStubber;
import org.mockito.internal.stubbing.Returns;
import org.mockito.internal.stubbing.ThrowsException;
import org.mockito.internal.stubbing.VoidMethodStubbable;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.verification.MissingInvocationInOrderVerifier;
import org.mockito.internal.verification.MissingInvocationVerifier;
import org.mockito.internal.verification.NoMoreInvocationsVerifier;
import org.mockito.internal.verification.NumberOfInvocationsInOrderVerifier;
import org.mockito.internal.verification.NumberOfInvocationsVerifier;
import org.mockito.internal.verification.Verifier;
import org.mockito.internal.verification.VerifyingRecorder;
import org.mockito.stubbing.Answer;

/**
 * Invocation handler set on mock objects.
 *
 * @param <T> type of mock object to handle
 */
public class MockHandler<T> implements MockAwareInterceptor<T> {

    private final VerifyingRecorder verifyingRecorder;
    private final MockitoStubber mockitoStubber;
    private final MatchersBinder matchersBinder;
    private final MockingProgress mockingProgress;
    private final String mockName;

    private T instance;

    public MockHandler(String mockName, MockingProgress mockingProgress, MatchersBinder matchersBinder) {
        this.mockName = mockName;
        this.mockingProgress = mockingProgress;
        this.matchersBinder = matchersBinder;
        this.mockitoStubber = new MockitoStubber(mockingProgress);

        verifyingRecorder = createRecorder();
    }
    
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (mockitoStubber.hasAnswersForStubbing()) {
            //stubbing voids with stubVoid() or doAnswer() style
            Invocation invocation = new Invocation(proxy, method, args, mockingProgress.nextSequenceNumber());
            InvocationMatcher invocationMatcher = matchersBinder.bindMatchers(invocation);
            mockitoStubber.setMethodForStubbing(invocationMatcher);
            return null;
        }
        
        VerificationMode verificationMode = mockingProgress.pullVerificationMode();
        mockingProgress.validateState();

        Invocation invocation = new Invocation(proxy, method, args, mockingProgress.nextSequenceNumber());
        InvocationMatcher invocationMatcher = matchersBinder.bindMatchers(invocation);

        if (verificationMode != null) {
            //verifying
            verifyingRecorder.verify(invocationMatcher, verificationMode);
            return null;
        }

        mockitoStubber.setInvocationForPotentialStubbing(invocationMatcher);
        verifyingRecorder.recordInvocation(invocationMatcher.getInvocation());

        mockingProgress.reportOngoingStubbing(new OngoingStubbingImpl());

        Answer<?> answer = mockitoStubber.findAnswerFor(invocation);
        if (answer != null) {
            return answer.answer(invocation);
        } else if (MockUtil.isMock(instance)) {
            return Configuration.instance().getReturnValues().valueFor(invocation);
        } else {
            return methodProxy.invoke(instance, args);
        }
    }

    public void verifyNoMoreInteractions() {
        verifyingRecorder.verify(VerificationModeImpl.noMoreInteractions());
    }

    public VoidMethodStubbable<T> voidMethodStubbable(T mock) {
        return new VoidMethodStubbableImpl(mock);
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    public List<Invocation> getRegisteredInvocations() {
        return verifyingRecorder.getRegisteredInvocations();
    }

    public String getMockName() {
        return mockName;
    }

    private VerifyingRecorder createRecorder() {
        List<Verifier> verifiers = Arrays.asList(
                new MissingInvocationInOrderVerifier(),
                new NumberOfInvocationsInOrderVerifier(),
                new MissingInvocationVerifier(),
                new NumberOfInvocationsVerifier(),
                new NoMoreInvocationsVerifier());
        return new VerifyingRecorder(new AllInvocationsFinder(), verifiers);
    }

    private final class VoidMethodStubbableImpl implements VoidMethodStubbable<T> {
        private final T mock;

        public VoidMethodStubbableImpl(T mock) {
            this.mock = mock;
        }

        public VoidMethodStubbable<T> toThrow(Throwable throwable) {
            mockitoStubber.addAnswerForVoidMethod(new ThrowsException(throwable));
            return this;
        }

        public VoidMethodStubbable<T> toReturn() {
            mockitoStubber.addAnswerForVoidMethod(new DoesNothing());
            return this;
        }

        public VoidMethodStubbable<T> toAnswer(Answer<?> answer) {
            mockitoStubber.addAnswerForVoidMethod(answer);
            return this;
        }

        public T on() {
            return mock;
        }
    }

    private class OngoingStubbingImpl implements NewOngoingStubbing<T>, DeprecatedOngoingStubbing<T> {
        public NewOngoingStubbing<T> thenReturn(Object value) {
            return thenAnswer(new Returns(value));
        }

        public NewOngoingStubbing<T> thenThrow(Throwable throwable) {
            return thenAnswer(new ThrowsException(throwable));
        }

        public NewOngoingStubbing<T> thenAnswer(Answer<?> answer) {
            verifyingRecorder.eraseLastInvocation();
            mockitoStubber.addAnswer(answer);
            return new ConsecutiveStubbing();
        }

        public DeprecatedOngoingStubbing<T> toReturn(Object value) {
            return toAnswer(new Returns(value));
        }

        public DeprecatedOngoingStubbing<T> toThrow(Throwable throwable) {
            return toAnswer(new ThrowsException(throwable));
        }

        public DeprecatedOngoingStubbing<T> toAnswer(Answer<?> answer) {
            verifyingRecorder.eraseLastInvocation();
            mockitoStubber.addAnswer(answer);
            return new ConsecutiveStubbing();
        }
    }

    private class ConsecutiveStubbing implements NewOngoingStubbing<T>, DeprecatedOngoingStubbing<T> {
        public NewOngoingStubbing<T> thenReturn(Object value) {
            return thenAnswer(new Returns(value));
        }

        public NewOngoingStubbing<T> thenThrow(Throwable throwable) {
            return thenAnswer(new ThrowsException(throwable));
        }

        public NewOngoingStubbing<T> thenAnswer(Answer<?> answer) {
            mockitoStubber.addConsecutiveAnswer(answer);
            return this;
        }
        
        public DeprecatedOngoingStubbing<T> toReturn(Object value) {
            return toAnswer(new Returns(value));
        }

        public DeprecatedOngoingStubbing<T> toThrow(Throwable throwable) {
            return toAnswer(new ThrowsException(throwable));
        }

        public DeprecatedOngoingStubbing<T> toAnswer(Answer<?> answer) {
            mockitoStubber.addConsecutiveAnswer(answer);
            return this;
        }
    }    
    
    @SuppressWarnings("unchecked")
    public void setAnswersForStubbing(List<Answer> answers) {
        mockitoStubber.setAnswersForStubbing(answers);
    }
}