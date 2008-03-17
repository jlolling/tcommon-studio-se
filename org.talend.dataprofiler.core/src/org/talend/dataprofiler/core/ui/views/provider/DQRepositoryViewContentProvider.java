// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.views.provider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataprofiler.core.helper.FolderNodeHelper;
import org.talend.dataprofiler.core.model.nodes.IFolderNode;

/**
 * @author rli
 * 
 */
public class DQRepositoryViewContentProvider extends
		AdapterFactoryContentProvider {
	
	private static ResourceSetImpl resourceSet = new ResourceSetImpl();

	/**
	 * @param adapterFactory
	 */
	public DQRepositoryViewContentProvider() {
		super(MNComposedAdapterFactory.getAdapterFactory());
	}

	public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IFile) {
            String path = ((IFile) parentElement).getFullPath().toString();
            URI uri = URI.createPlatformResourceURI(path, true);
            parentElement = resourceSet.getResource(uri, true);
        } else if (parentElement instanceof IFolderNode) {
            IFolderNode folerNode = (IFolderNode) parentElement;
            if (!(folerNode.isLoaded())) {
                folerNode.loadChildren();
            }
            return folerNode.getChildren();
        } else {
            return FolderNodeHelper.getFolderNode((EObject) parentElement);
        }
        return super.getChildren(parentElement);
    }

	@Override
	public Object[] getElements(Object object) {
		return this.getChildren(object);
	}

	public Object getParent(Object element) {
		if (element instanceof IFile) {
			return ((IResource) element).getParent();
		}
		return super.getParent(element);
	}

	public boolean hasChildren(Object element) {
	    return !(element instanceof TdColumn);
    }

}
