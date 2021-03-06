package rocks.inspectit.ui.rcp.ci.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.form.editor.ProfileEditor;
import rocks.inspectit.ui.rcp.ci.form.input.ProfileEditorInput;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Job for loading of the profile from the CMR and opening the profile editor.
 *
 * @author Ivan Senic
 *
 */
public class OpenProfileJob extends Job {

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Profile id.
	 */
	private String profileId;

	/**
	 * Page to open editor in.
	 */
	private IWorkbenchPage page;

	/**
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 * @param profileId
	 *            Profile id.
	 * @param page
	 *            Page to open editor in.
	 */
	public OpenProfileJob(CmrRepositoryDefinition cmrRepositoryDefinition, String profileId, IWorkbenchPage page) {
		super("Loading profile..");
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.profileId = profileId;
		this.page = page;
		setUser(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			return Status.CANCEL_STATUS;
		}

		try {
			Profile profile = cmrRepositoryDefinition.getConfigurationInterfaceService().getProfile(profileId);
			final ProfileEditorInput profileEditorInput = new ProfileEditorInput(profile, cmrRepositoryDefinition);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						page.openEditor(profileEditorInput, ProfileEditor.ID, true);
					} catch (PartInitException e) {
						InspectIT.getDefault().createErrorDialog("Exception occurred opening the Profile editor.", e, -1);
					}
				}
			});
			return Status.OK_STATUS;
		} catch (BusinessException e) {
			return new Status(IStatus.OK, InspectIT.ID, "Exception occurred loading the profile from the CMR.", e);
		}
	}

}