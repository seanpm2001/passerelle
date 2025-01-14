package com.isencia.passerelle.workbench.model.editor.ui.editor;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;

import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.OpenFileAction;

public class PasserelleContextMenuProvider extends
		org.eclipse.gef.ContextMenuProvider {

	private ActionRegistry actionRegistry;

	public PasserelleContextMenuProvider(EditPartViewer viewer,
			ActionRegistry registry) {
		super(viewer);
		setActionRegistry(registry);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ContextMenuProvider#menuAboutToShow(org.eclipse.jface
	 * .action.IMenuManager)
	 */
	public void buildContextMenu(IMenuManager manager) {
		manager.add(new Separator(GEFActionConstants.GROUP_VIEW));
		manager.add(new Separator(GEFActionConstants.GROUP_UNDO));
		manager.add(new Separator(GEFActionConstants.GROUP_COPY));
		manager.add(new Separator(GEFActionConstants.GROUP_PRINT));
		manager.add(new Separator(GEFActionConstants.GROUP_EDIT));
		manager.add(new Separator(GEFActionConstants.GROUP_FIND));
		manager.add(new Separator(GEFActionConstants.GROUP_ADD));
		manager.add(new Separator(GEFActionConstants.GROUP_REST));
		manager.add(new Separator(GEFActionConstants.MB_ADDITIONS));
		manager.add(new Separator(GEFActionConstants.GROUP_SAVE));
		manager.add(new Separator(GEFActionConstants.GROUP_HELP));

		IAction action;
		action = getActionRegistry().getAction(OpenFileAction.ID1);
		if (action != null )
			manager.appendToGroup(GEFActionConstants.GROUP_VIEW,action);
		action = getActionRegistry().getAction(OpenFileAction.ID2);
		if (action != null )
			manager.appendToGroup(GEFActionConstants.GROUP_VIEW,action);

		action = getActionRegistry().getAction(ActionFactory.UNDO.getId());
		manager.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

		action = getActionRegistry().getAction(ActionFactory.REDO.getId());
		manager.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

		action = getActionRegistry().getAction(ActionFactory.COPY.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_COPY, action);
		action = getActionRegistry().getAction(ActionFactory.CUT.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_COPY, action);
		action = getActionRegistry().getAction(ActionFactory.PASTE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_COPY, action);
		action = getActionRegistry().getAction(ActionFactory.CLOSE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
		action = getActionRegistry().getAction(ActionFactory.DELETE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
		action = getActionRegistry().getAction(ActionFactory.CLOSE_PERSPECTIVE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_EDIT, action);

		action = getActionRegistry().getAction(ActionFactory.SAVE.getId());
		if (action != null && action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_SAVE, action);

		

//		action = getActionRegistry().getAction(ActionFactory.HELP_CONTENTS.getId());
//		if (action != null && action.isEnabled())
//			manager.appendToGroup(GEFActionConstants.GROUP_HELP, action);
	}

	private ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	private void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}

}
