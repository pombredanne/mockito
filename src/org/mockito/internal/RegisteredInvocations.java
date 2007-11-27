package org.mockito.internal;

import java.util.*;

public class RegisteredInvocations {
    
    private List<Invocation> registeredInvocations = new LinkedList<Invocation>();
    private final InvocationsFinder invocationsFinder;
    
    public RegisteredInvocations(InvocationsFinder invocationsFinder) {
        this.invocationsFinder = invocationsFinder;
    }

    public void add(Invocation invocation) {
        registeredInvocations.add(invocation);        
    }

    public void removeLast() {
        registeredInvocations.remove(registeredInvocations.size()-1);
    }
    
    public void markInvocationsAsVerified(ExpectedInvocation expected, VerifyingMode mode) {
        if (mode.expectedCountIsZero()) {
            return;
        }
        
        if (mode.orderOfInvocationsMatters()) {
            List<InvocationChunk> chunks = unverifiedInvocationChunks(mode);
            chunks.get(0).markAllInvocationsAsVerified();
        } else {
            for (Invocation invocation : registeredInvocations) {
                if (expected.matches(invocation)) {
                    invocation.markVerified();
                }
            }
        }
    }
    
    public List<InvocationChunk> unverifiedInvocationChunks(VerifyingMode verifyingMode) {
        Set<Invocation> allInvocationsInOrder = new TreeSet<Invocation>(
                new Comparator<Invocation>(){
                    public int compare(Invocation o1, Invocation o2) {
                        int comparison = o1.getSequenceNumber().compareTo(o2.getSequenceNumber());
                        assert comparison != 0;
                        return comparison;
                    }});
        
        List<Object> allMocksToBeVerifiedInOrder = verifyingMode.getAllMocksToBeVerifiedInSequence();
        List<Invocation> allInvocations = invocationsFinder.allInvocationsInOrder(allMocksToBeVerifiedInOrder);
        allInvocationsInOrder.addAll(allInvocations);
        
        List<InvocationChunk> chunks = new LinkedList<InvocationChunk>();
        for (Invocation i : allInvocationsInOrder) {
            if (i.isVerified()) {
                continue;
            }
            if (!chunks.isEmpty() 
                    && chunks.get(chunks.size()-1).getInvocation().equals(i)) {
                chunks.get(chunks.size()-1).add(i);
            } else {
                chunks.add(new InvocationChunk(i));
            }
        }
        
        return chunks;
    }
    
    /**
     * gets first registered invocation with the same method name
     * or just first invocation
     */
    public Invocation findSimilarInvocation(ExpectedInvocation expectedInvocation) {
        for (Invocation registered : registeredInvocations) {
            String expectedMethodName = expectedInvocation.getMethod().getName();
            String registeredInvocationName = registered.getMethod().getName();
            if (expectedMethodName.equals(registeredInvocationName) && !registered.isVerified()) {
                return registered;
            }
        }

        return null;
    }
    
    public int countActual(ExpectedInvocation expectedInvocation) {
        int actual = 0;
        for (Invocation registeredInvocation : registeredInvocations) {
            if (expectedInvocation.matches(registeredInvocation)) {
                actual++;
            }
        }

        return actual;
    }

    public List<Invocation> all() {
        return registeredInvocations;
    }
}
