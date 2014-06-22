// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.metadata.seeker;

import java.util.List;

import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 * 
 */
public class NormalMetadatasRepositorySeeker extends AbstractMetadataRepoViewSeeker {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.repository.seeker.AbstractRepoViewSeeker#getValidationTypes()
     */
    @Override
    protected List<ERepositoryObjectType> getValidationTypes() {
        List<ERepositoryObjectType> validationTypes = super.getValidationTypes();
        validationTypes.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        validationTypes.add(ERepositoryObjectType.METADATA_FILE_DELIMITED);
        validationTypes.add(ERepositoryObjectType.METADATA_FILE_EXCEL);
        validationTypes.add(ERepositoryObjectType.METADATA_FILE_FTP);
        validationTypes.add(ERepositoryObjectType.METADATA_FILE_LDIF);
        validationTypes.add(ERepositoryObjectType.METADATA_FILE_POSITIONAL);
        validationTypes.add(ERepositoryObjectType.METADATA_FILE_REGEXP);
        validationTypes.add(ERepositoryObjectType.METADATA_FILE_XML);
        validationTypes.add(ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
        validationTypes.add(ERepositoryObjectType.METADATA_LDAP_SCHEMA);
        validationTypes.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
        validationTypes.add(ERepositoryObjectType.METADATA_WSDL_SCHEMA);
        return validationTypes;
    }

}
