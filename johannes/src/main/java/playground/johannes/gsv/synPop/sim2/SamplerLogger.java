/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.johannes.gsv.synPop.sim2;

import java.util.Collection;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import playground.johannes.gsv.synPop.ProxyPerson;
import playground.johannes.gsv.synPop.sim2.SamplerListener;


/**
 * @author johannes
 *
 */
public class SamplerLogger implements SamplerListener {

	private final Logger logger = Logger.getLogger(SamplerLogger.class);
	
	private Timer timer;
	
	private Task task;
	
	@Override
	public void afterStep(Collection<ProxyPerson> population, ProxyPerson person, boolean accpeted) {
		if(timer == null) {
			timer = new Timer();
			task = new Task();
			timer.schedule(task, 2000, 2000);
		}
		
		task.afterStep(population, person, accpeted);

	}
	
	private class Task extends TimerTask implements SamplerListener {

		private long iter;
		
		private long accepts;
		
		private long iters2;
		
		@Override
		public void run() {
			logger.info(String.format(Locale.US, "[%s] Accepted %s of %s steps (%.2f %%).", iter, accepts, iters2, accepts/(double)iters2 * 100));
			accepts = 0;
			iters2 = 0;
		}

		@Override
		public void afterStep(Collection<ProxyPerson> population, ProxyPerson original, boolean accepted) {
			iter++;
			iters2++;
			if(accepted)
				accepts++;
		}

		/* (non-Javadoc)
		 * @see playground.johannes.gsv.synPop.sim2.SamplerListener#afterModify(playground.johannes.gsv.synPop.ProxyPerson)
		 */
		@Override
		public void afterModify(ProxyPerson person) {
			// TODO Auto-generated method stub
			
		}
		
	}

	/* (non-Javadoc)
	 * @see playground.johannes.gsv.synPop.sim2.SamplerListener#afterModify(playground.johannes.gsv.synPop.ProxyPerson)
	 */
	@Override
	public void afterModify(ProxyPerson person) {
		// TODO Auto-generated method stub
		
	}

}
