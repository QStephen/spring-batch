package org.springframework.batch.sample;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.springframework.batch.core.BatchStatus.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.ListableJobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class JobOperatorFunctionalTests {

	private static final Log logger = LogFactory.getLog(JobOperatorFunctionalTests.class);

	@Autowired
	private JobOperator tested;

	@Autowired
	private Job job;

	@Autowired
	private ListableJobRegistry jobRegistry;

	@Before
	public void setUp() throws Exception {
		jobRegistry.register(new ReferenceJobFactory(job));
	}

	@Test
	public void testStartStopResumeJob() throws Exception {

		String params = new JobParametersBuilder().addLong("jobOperatorTestParam", 7L).toJobParameters().toString();
		long executionId = tested.start(job.getName(), params);
		stopAndCheckStatus(executionId);

		long resumedExecutionId = tested.resume(executionId);
		stopAndCheckStatus(resumedExecutionId);

	}

	/**
	 * @param executionId id of running job execution
	 */
	private void stopAndCheckStatus(long executionId) throws Exception {

		// wait to the job to get up and running
		Thread.sleep(1000);

		assertTrue(tested.getRunningExecutions(job.getName()).contains(executionId));
		assertTrue(tested.getSummary(executionId).contains(STARTED.toString()));

		tested.stop(executionId);

		int count = 0;
		while (tested.getRunningExecutions(job.getName()).contains(executionId) && count <= 10) {
			logger.info("Checking for running JobExecution: count=" + count);
			Thread.sleep(100);
			count++;
		}

		String summary = tested.getSummary(executionId);
		assertFalse("Timed out waiting for job to end.", summary.contains(STARTED.toString()));
		assertTrue(summary.contains(STOPPED.toString()));
	}

}