package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;


import javax.management.MBeanServerConnection;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.launch.ModelRunner;

public class StopAction extends ExecutionAction implements IEditorActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(StopAction.class);
	
	public StopAction() {
		super();
		setId(getClass().getName());
		setText("Stop the workflow if it is running.");
		setImageDescriptor(Activator.getImageDescriptor("/icons/stop_workflow.gif"));
	}

	@Override
	public void run() {
		run(this);
	}
	
	@Override
	public void run(IAction action) {
	    
		try { 
			if (System.getProperty("eclipse.debug.session")!=null) {
			    ModelRunner.getRunningInstance().stop();
				refreshToolbars();
		    
			} else {
				final MBeanServerConnection client = RemoteManagerAgent.getServerConnection(5000);
				addRefreshListener(client);
				client.invoke(RemoteManagerAgent.REMOTE_MANAGER, "stop", null, null);
			}
            
		} catch (Exception e) {
			logger.error("Cannot read configuration", e);
			refreshToolbars();
		}
		
 	}
	
	public boolean isEnabled() {
		if (System.getProperty("eclipse.debug.session")!=null) {
			return ModelRunner.getRunningInstance()!=null;
		}
		try { 
			final MBeanServerConnection client = RemoteManagerAgent.getServerConnection(100);
			return client.getObjectInstance(RemoteManagerAgent.REMOTE_MANAGER)!=null;
		} catch (Throwable e) {
			return false;
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
       
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub

	}

}
