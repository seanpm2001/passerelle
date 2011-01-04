package com.isencia.passerelle.workbench.model.launch;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.moml.MoMLParser;

import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;

public class ModelRunner implements IApplication {
	
	
	private static ModelRunner currentInstance;
	public static ModelRunner getRunningInstance() {
		return currentInstance;
	}
	
    private static Logger logger = LoggerFactory.getLogger(ModelRunner.class);   
    
	public Logger getLogger() {
		return logger;
	}

	private long start;
	private Manager manager;
	
	@Override
	public Object start(IApplicationContext applicationContextMightBeNull) throws Exception {
		
		String model = System.getProperty("model");
		runModel(model, true);
		return  IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		
		final long end  = System.currentTimeMillis();
		// Did not like the DateFormat version, there may be something better than this.
		final long time = end-start;
		if (manager!=null) {
			try {
				manager.stop();
			} catch (Throwable ne) {
				logger.error("Cannot stop manager for model.", ne);
			}
			manager = null;
		}
        logger.info("Model completed in "+(time/(60*1000))+"m "+((time/1000)%60)+"s "+(time%1000)+"ms");
	}

	/**
	 * Sometimes can be called 
	 * @param modelPath
	 */
	public void runModel(final String modelPath, final boolean requireService) {
		
		start = System.currentTimeMillis();
		final String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		System.setProperty("eclipse.workspace.home", workspacePath);
		System.setProperty("be.isencia.home",        workspacePath);
		logger.info("Workspace folder set to: "+workspacePath);

		Reader             reader     = null;
		RemoteManagerAgent modelAgent = null;
		try {
			currentInstance = this;
			
			if( modelPath==null) {
				throw new IllegalArgumentException("No model specified",null);
			} else {
				logger.info("Running model : " + modelPath);
				reader = new FileReader(modelPath);
				MoMLParser moMLParser = new MoMLParser();
				CompositeActor compositeActor = (CompositeActor) moMLParser.parse(null, reader);
				
				this.manager = new Manager(compositeActor.workspace(), "model");
				compositeActor.setManager(manager);
				if (requireService) {
					modelAgent = new RemoteManagerAgent(manager);
					modelAgent.start();
				}
				manager.execute(); // Blocks
				
			}
		} catch (IllegalArgumentException illegalArgumentException) { 
			logger.info(illegalArgumentException.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error("Cannot read "+modelPath, e);

		} finally {
			logger.info("End model : "+modelPath);
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
			
			if (modelAgent!=null) modelAgent.stop();
			
			manager         = null;
			currentInstance = null;

			stop();
			System.gc();
		}
	}

	public static void main(String[] args) {
		String model = null;
		// The model is specified with argument -model moml_file
		if( args==null)
			return;
		
		for (int i = 0; i < args.length; i++) {
			if( i>0 && "-model".equals(args[i-1])) {
				model = args[i];
				break;
			}
		}
		
		final ModelRunner runner = new ModelRunner();
		runner.runModel(model, true);
	}

	
}
