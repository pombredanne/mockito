/**
 * 
 */
package org.mockito.internal.runners;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.mockito.Mockito;

public class FrameworkUsageValidator extends RunListener {
    
    private final RunNotifier notifier;

    public FrameworkUsageValidator(RunNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        try {
            Mockito.validateMockitoUsage();
        } catch(Throwable t) {
            notifier.fireTestFailure(new Failure(description, t));
        }
    }
}