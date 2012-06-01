// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.jtds.jdbc.ConnectionJDBC2;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IProviderService;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.designerproperties.ComponentToRepositoryProperty;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.LinkRulesItem;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RulesItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.IHeaderFooterProviderService;
import org.talend.core.ui.IMDMProviderService;
import org.talend.core.ui.metadata.celleditor.EProcessTypeForRule;
import org.talend.core.ui.metadata.celleditor.RuleOperationChoiceDialog;
import org.talend.core.ui.rule.AbstractRlueOperationChoice;
import org.talend.repository.model.IMetadataService;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.metadata.AbstractCreateTableAction;
import org.talend.repository.ui.actions.metadata.CreateTableAction;
import org.talend.repository.ui.wizards.RepositoryWizard;
import org.talend.repository.ui.wizards.metadata.connection.database.DatabaseWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.delimited.DelimitedFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.excel.ExcelFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.ldif.LdifFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.positional.FilePositionalWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.regexp.RegexpFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.salesforce.SalesforceSchemaWizard;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.XmlFileWizard;
import org.talend.repository.ui.wizards.metadata.connection.genericshema.GenericSchemaWizard;
import org.talend.repository.ui.wizards.metadata.connection.ldap.LDAPSchemaWizard;
import org.talend.repository.ui.wizards.metadata.connection.wsdl.WSDLSchemaWizard;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class MetadataService implements IMetadataService {

    private GenericSchemaWizard genericSchemaWizard = null;

    private Map<String, Object> oldMdmConValues = new HashMap<String, Object>();

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getGenericSchemaWizardDialog(org.eclipse.swt.widgets.Shell,
     * org.eclipse.ui.IWorkbench, boolean, org.eclipse.jface.viewers.ISelection, java.lang.String[], boolean)
     */
    public WizardDialog getGenericSchemaWizardDialog(Shell shell, IWorkbench workbench, boolean creation, ISelection selection,
            String[] existingNames, boolean isSinglePageOnly) {

        genericSchemaWizard = new GenericSchemaWizard(workbench, creation, selection, existingNames, isSinglePageOnly);
        return new WizardDialog(shell, genericSchemaWizard);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getPropertyFromWizardDialog()
     */
    public Property getPropertyFromWizardDialog() {
        if (this.genericSchemaWizard != null) {
            return this.genericSchemaWizard.getConnectionProperty();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryService#getPathForSaveAsGenericSchema()
     */
    public IPath getPathForSaveAsGenericSchema() {
        if (this.genericSchemaWizard != null) {
            return this.genericSchemaWizard.getPathForSaveAsGenericSchema();
        }
        return null;
    }

    public void openMetadataConnection(IRepositoryViewObject o, INode node) {
        final RepositoryNode realNode = RepositoryNodeUtilities.getRepositoryNode(o);
        openMetadataConnection(false, realNode, node);
    }

    @Override
    public ConnectionItem openMetadataConnection(boolean creation, IRepositoryNode repoNode, INode node) {
        RepositoryNode realNode;
        if (repoNode instanceof RepositoryNode) {
            realNode = (RepositoryNode) repoNode;
            IWizard relatedWizard = null;
            ERepositoryObjectType objectType = null;
            if (creation) {
                objectType = realNode.getContentType();
            } else {
                objectType = realNode.getObjectType();
            }
            if (objectType.equals(ERepositoryObjectType.METADATA_CONNECTIONS)) {
                relatedWizard = new DatabaseWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_DELIMITED)) {
                relatedWizard = new DelimitedFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_LDIF)) {
                relatedWizard = new LdifFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_POSITIONAL)) {
                relatedWizard = new FilePositionalWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_REGEXP)) {
                relatedWizard = new RegexpFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_XML)) {
                relatedWizard = new XmlFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_GENERIC_SCHEMA)) {
                relatedWizard = new GenericSchemaWizard(PlatformUI.getWorkbench(), creation, realNode, null, true);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_WSDL_SCHEMA)) {
                relatedWizard = new WSDLSchemaWizard(PlatformUI.getWorkbench(), creation, realNode, null, false);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_LDAP_SCHEMA)) {
                relatedWizard = new LDAPSchemaWizard(PlatformUI.getWorkbench(), creation, realNode, null, false);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_EXCEL)) {
                relatedWizard = new ExcelFileWizard(PlatformUI.getWorkbench(), creation, realNode, null);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA)) {
                relatedWizard = new SalesforceSchemaWizard(PlatformUI.getWorkbench(), creation, realNode, null, false);
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_EBCDIC)) {
                if (PluginChecker.isEBCDICPluginLoaded()) {
                    IProviderService iebcdicService = (IProviderService) GlobalServiceRegister.getDefault().findService(
                            "IEBCDICProviderService");
                    if (iebcdicService != null) {
                        relatedWizard = iebcdicService.newWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_HL7)) {
                if (PluginChecker.isHL7PluginLoaded()) {
                    IProviderService service = (IProviderService) GlobalServiceRegister.getDefault().findService(
                            "IHL7ProviderService");
                    if (service != null) {
                        relatedWizard = service.newWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_MDMCONNECTION)) {
                if (PluginChecker.isMDMPluginLoaded()) {
                    IMDMProviderService service = (IMDMProviderService) GlobalServiceRegister.getDefault().getService(
                            IMDMProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                        //
                        if (node != null && "tMDMReceive".equals(node.getComponent().getName())) {
                            if (relatedWizard != null && relatedWizard instanceof RepositoryWizard) {
                                ConnectionItem connItem = ((RepositoryWizard) relatedWizard).getConnectionItem();
                                if (connItem != null && connItem instanceof MDMConnectionItem) {
                                    org.talend.core.model.metadata.builder.connection.Connection connection = ((MDMConnectionItem) connItem)
                                            .getConnection();
                                    if (connection != null && connection instanceof MDMConnection) {
                                        if (oldMdmConValues.containsKey("username") && oldMdmConValues.get("username") != null) {
                                            ((MDMConnection) connection).setUsername(oldMdmConValues.get("username").toString());
                                            ((MDMConnection) connection).setPassword(oldMdmConValues.get("password").toString());
                                            ((MDMConnection) connection).setServer(oldMdmConValues.get("server").toString());
                                            ((MDMConnection) connection).setPort(oldMdmConValues.get("port").toString());
                                        } else {
                                            ((MDMConnection) connection).setUsername("userName");
                                            ((MDMConnection) connection).setPassword("password");
                                            ((MDMConnection) connection).setServer("localhost");
                                            ((MDMConnection) connection).setPort("8080");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_SAPCONNECTIONS)) {
                if (PluginChecker.isSAPWizardPluginLoaded()) {
                    IProviderService service = (IProviderService) GlobalServiceRegister.getDefault().findService(
                            "ISAPProviderService");
                    if (service != null) {
                        relatedWizard = service.newWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_HEADER_FOOTER)) {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IHeaderFooterProviderService.class)) {
                    IHeaderFooterProviderService service = (IHeaderFooterProviderService) GlobalServiceRegister.getDefault()
                            .getService(IHeaderFooterProviderService.class);
                    if (service != null) {
                        relatedWizard = service.newWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_FTP)) {
                if (PluginChecker.isFTPPluginLoaded()) {
                    IProviderService service = (IProviderService) GlobalServiceRegister.getDefault().findService(
                            "org.talend.core.ui.IFTPProviderService");
                    if (service != null) {
                        relatedWizard = service.newWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            } else if (objectType.equals(ERepositoryObjectType.METADATA_FILE_BRMS)) {
                if (PluginChecker.isBRMSPluginLoaded()) {
                    IProviderService service = (IProviderService) GlobalServiceRegister.getDefault().findService(
                            "IBRMSProviderService");
                    if (service != null) {
                        relatedWizard = service.newWizard(PlatformUI.getWorkbench(), creation, realNode, null);
                    }
                }
            }
            boolean changed = false;
            if (relatedWizard != null) {
                ConnectionItem connItem = null;
                if (node != null && relatedWizard instanceof RepositoryWizard) {// creation && node != null
                    connItem = ((RepositoryWizard) relatedWizard).getConnectionItem();
                    if (connItem != null) {
                        changed = ComponentToRepositoryProperty.setValue(connItem.getConnection(), node);
                    }
                }
                if (connItem != null && changed) {
                    // Open the Wizard
                    WizardDialog wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), relatedWizard);
                    wizardDialog.setPageSize(600, 500);
                    wizardDialog.create();
                    if (wizardDialog.open() == wizardDialog.OK) {
                        return connItem;
                    }
                }
            }
        }
        return null;
    }

    public void openEditSchemaWizard(String value) {
        final RepositoryNode realNode = RepositoryNodeUtilities.getMetadataTableFromConnection(value);
        if (realNode != null) {
            AbstractCreateTableAction action = new CreateTableAction() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.talend.repository.ui.actions.AContextualAction#getSelection()
                 */
                @Override
                public ISelection getSelection() {
                    return new StructuredSelection(realNode);
                }
            };
            action.run();
        }
    }

    @Override
    public void runCreateTableAction(RepositoryNode metadataNode) {
        AbstractCreateTableAction action = new CreateTableAction(metadataNode);
        action.setAvoidUnloadResources(true);
        action.run();
    }

    @Override
    public AbstractRlueOperationChoice getOperationChoice(Shell shell, INode node, RulesItem[] repositoryRuleItems,
            LinkRulesItem[] linkRuleItems, EProcessTypeForRule rule, String ruleToEdit, boolean readOnly) {
        return new RuleOperationChoiceDialog(shell, node, repositoryRuleItems, linkRuleItems, rule, ruleToEdit, readOnly);
    }

    @Override
    public DatabaseMetaData findCustomizedJTDSDBMetadata(Connection jtdsConn) {
        return new JtdsMetadataAdapter((ConnectionJDBC2) jtdsConn);
    }

    @Override
    public void fillOldMdmConValues(Map<String, Object> oldMdmConValues) {
        this.oldMdmConValues = oldMdmConValues;
    }
}
