package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.jmx.RemoteManagerAgent;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public abstract class ExecutionAction extends Action {
	
	private static final Logger logger = LoggerFactory.getLogger(ExecutionAction.class);

	protected void addRefreshListener() throws Exception {
		addRefreshListener(null);
	}
	/**
	 * Might try to add listener
	 * @throws Exception
	 */
	protected void addRefreshListener(MBeanServerConnection client) throws Exception {
		try {
			if (client==null) client = RemoteManagerAgent.getServerConnection(5000);
			logger.debug("Client connected = "+client);
			if (!client.isRegistered(RemoteManagerAgent.REMOTE_MANAGER)) return;
			logger.debug("Adding notification listener");
			client.addNotificationListener(RemoteManagerAgent.REMOTE_MANAGER, createRefreshListener(), null, this);
			logger.debug("Added notification listener");
		} catch (Exception e) {
			logger.error("Cannot add listener", e);
		}
	}

	private NotificationListener createRefreshListener() {
		return new NotificationListener() {		
			@Override
			public void handleNotification(Notification notification, Object handback) {
				refreshToolbars();
			}
		};
	}
	
	protected void refreshToolbars() {
		final Job updateActionBars = new Job("Update action bars") {
			@Override
			public IStatus run(IProgressMonitor mon) {
				final IEditorPart editor = EclipseUtils.getPage().getActiveEditor();
				if (editor!=null) {
					final SubActionBars2 bars = (SubActionBars2)editor.getEditorSite().getActionBars();
					logger.debug("Doing refresh of toolbar actions.");
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							bars.deactivate();
							bars.activate(true);
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		updateActionBars.setUser(false);
		updateActionBars.setSystem(true);
		updateActionBars.schedule(100);
	}
}
