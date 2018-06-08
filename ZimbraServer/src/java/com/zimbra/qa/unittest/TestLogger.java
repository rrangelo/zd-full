/*
 * 
 */
package com.zimbra.qa.unittest;

import com.zimbra.common.util.ZimbraLog;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TestLogger extends RunListener {

    @Override
    public void testRunStarted(Description description) throws Exception {
        ZimbraLog.test.info("Starting test suite.");
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        ZimbraLog.test.info("Finished test suite.");
    }

    @Override
    public void testStarted(Description desc) throws Exception {
        ZimbraLog.test.info("Starting test %s.%s.", desc.getClassName(), desc.getMethodName());
    }

    @Override
    public void testFinished(Description desc) throws Exception {
        ZimbraLog.test.info("Test %s.%s completed.", desc.getClassName(), desc.getMethodName());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        Description desc = failure.getDescription();
        ZimbraLog.test.error("Test %s.%s failed.", desc.getClassName(), desc.getMethodName(), failure.getException());
    }

    @Override
    public void testIgnored(Description desc) throws Exception {
        ZimbraLog.test.info("Test %s.%s ignored.", desc.getClassName(), desc.getMethodName());
    }
}
