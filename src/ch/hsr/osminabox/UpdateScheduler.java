package ch.hsr.osminabox;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.context.ArgumentConstants;
import ch.hsr.osminabox.db.downloading.EntityConsistencyServiceImpl;
import ch.hsr.osminabox.db.downloading.NullEntityConsistencyService;
import ch.hsr.osminabox.updating.FireImmediatelyUpdateStrategyDecorator;
import ch.hsr.osminabox.updating.JobDetailMapConstants;
import ch.hsr.osminabox.updating.UpdateJob;
import ch.hsr.osminabox.updating.UpdateStrategy;
import ch.hsr.osminabox.updating.UpdateStrategyFactory;

/**
 * This Class is responsible for gathering the Information an calls the right
 * procedures for an Update call
 * 
 * @author rhof
 */
public class UpdateScheduler {

	private static Logger logger = Logger.getLogger(UpdateScheduler.class);

	private ApplicationContext context;

	public UpdateScheduler(ApplicationContext context) {
		this.context = context;
		logger.setLevel(Level.ALL);
	}

	public void scheduleUpdate(boolean cronUpdate) {
		logger.debug("entering sceduleUpdate");
		if (context.getConsistencyService()
				.checkConsistencyForInitialImportAndUpdate(false)) {
			UpdateStrategy strategy = UpdateStrategyFactory
					.createUpdateStrategy(context);
			if (cronUpdate) {
				logger.info("This is a cron update, it starts immediatelly");
				strategy = new FireImmediatelyUpdateStrategyDecorator(strategy);
			} else {
				logger.info("The update will be performed at: "
						+ strategy.getTrigger().computeFirstFireTime(null));
			}
			SchedulerFactory schedFactory = new StdSchedulerFactory();
			try {
				if(context.isSwitchOn(ArgumentConstants.SWT_NO_CONSISTENCY)){
					logger.warn("Downloading missing Entities from osm-api is diabled!");
					context
					.setApiConsistencyService(new NullEntityConsistencyService());
				}else{
					context
					.setApiConsistencyService(new EntityConsistencyServiceImpl());
				}
				

				Scheduler scheduler = schedFactory.getScheduler();
				scheduler.start();

				JobDetail jobDetail = new JobDetail("UpdateJob", null,
						UpdateJob.class);
				jobDetail.getJobDataMap().put(
						JobDetailMapConstants.UPDATE_STRATEGY, strategy);
				jobDetail.getJobDataMap().put(
						JobDetailMapConstants.APPLICATION_CONTEXT, context);
				jobDetail.getJobDataMap().put(
						JobDetailMapConstants.TERMINATE_SCHEDULER,
						Boolean.valueOf(cronUpdate));
				jobDetail.setName("UpdateJobDetail");

				Trigger trigger = strategy.getTrigger();

				scheduler.scheduleJob(jobDetail, trigger);

			} catch (SchedulerException e) {
				logger.error("Could not schedule update: ");
				e.printStackTrace();
				return;
			}
		}
		logger.debug("leaving sceduleUpdate");
	}

}
