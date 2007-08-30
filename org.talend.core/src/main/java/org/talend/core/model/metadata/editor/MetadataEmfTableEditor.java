// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.core.model.metadata.editor;

import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.utils.data.list.UniqueStringGenerator;
import org.talend.core.i18n.Messages;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.metadata.types.TypesManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class MetadataEmfTableEditor extends ExtendedTableModel<MetadataColumn> {

	private String defaultLabel = "newColumn"; //$NON-NLS-1$

	private static int indexNewColumn;

	public static final String VALID_CHAR_COLUMN_NAME = "a-zA-Z_0-9"; //$NON-NLS-1$

	private static final PatternCompiler COMPILER = new Perl5Compiler();

	private static final String VALID_PATTERN_COLUMN_NAME = "^[a-zA-Z_][" + VALID_CHAR_COLUMN_NAME + "]*$"; //$NON-NLS-1$ //$NON-NLS-2$

	private static Pattern validPatternColumnNameRegexp = null;

	private MetadataTable metadataTable;

	public MetadataEmfTableEditor(String titleName) {
		super(titleName);
	}

	public MetadataEmfTableEditor() {
		super();
	}

	public MetadataEmfTableEditor(MetadataTable metadataTable, String titleName) {
		super(titleName);
		setMetadataTable(metadataTable);
	}

	public String getTitleName() {
		return getName();
	}

	public List<MetadataColumn> getMetadataColumnList() {
		return getBeansList();
	}

	public MetadataTable getMetadataTable() {
		return this.metadataTable;
	}

	/**
	 * set MetadataTable.
	 * 
	 * @param metadataEditorTable
	 */
	public void setMetadataTable(MetadataTable metadataTable) {
		this.metadataTable = metadataTable;
		registerDataList((List<MetadataColumn>) this.metadataTable.getColumns());
	}

	/**
	 * 
	 * DOC amaumont Comment method "validateColumnName". Check columName width
	 * the list of columns to be see on the table
	 * 
	 * @param columnName
	 * @param beanPosition
	 * @return
	 */
	public String validateColumnName(String columnName, int beanPosition) {
		return validateColumnName(columnName, beanPosition, getBeansList());
	}

	/**
	 * 
	 * DOC amaumont Comment method "validateColumnName".
	 * 
	 * @param columnName
	 * @param beanPosition
	 * @param List
	 *            <MetadataColumn>
	 * @return
	 */
	public String validateColumnName(String columnName, int beanPosition,
			List<MetadataColumn> metadataColumns) {
		if (columnName == null) {
			return Messages
					.getString("MetadataEmfTableEditor.ColumnNameIsNullError"); //$NON-NLS-1$
		}

		validPatternColumnNameRegexp = null;
		if (validPatternColumnNameRegexp == null) {
			try {
				validPatternColumnNameRegexp = COMPILER
						.compile(VALID_PATTERN_COLUMN_NAME);
			} catch (MalformedPatternException e) {
				throw new RuntimeException(e);
			}
		}
		Perl5Matcher matcher = new Perl5Matcher();
		boolean match = matcher.matches(columnName,
				validPatternColumnNameRegexp);
		if (!match) {
			return Messages.getString(
					("MetadataEmfTableEditor.ColumnInvalid"), columnName); //$NON-NLS-1$ //$NON-NLS-2$
		}

		int lstSize = metadataColumns.size();
		for (int i = 0; i < lstSize; i++) {
			if (columnName.equals(metadataColumns.get(i).getLabel())
					&& i != beanPosition) {
				return Messages.getString(
						"MetadataEmfTableEditor.ColumnNameExists", columnName); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return null;
	}

	/**
	 * DOC ocarbone. getValidateColumnName return a valid label to a column (not
	 * empty, no doublon, no illegal char).
	 * 
	 * @param columnLabel
	 * @param List
	 *            <MetadataColumn>
	 * @return string
	 */
	public String getNextGeneratedColumnName(String columnLabel) {
		return getNextGeneratedColumnName(columnLabel, getBeansList());
	}

	/**
	 * DOC ocarbone. getValidateColumnName return a valid label to a column (not
	 * empty, no doublon, no illegal char).
	 * 
	 * @param columnLabel
	 * @param i
	 * @param List
	 *            <MetadataColumn>
	 * @return string
	 */
	protected String getNextGeneratedColumnName(String oldColumnName,
			List<MetadataColumn> metadataColumns) {

		UniqueStringGenerator<MetadataColumn> uniqueStringGenerator = new UniqueStringGenerator<MetadataColumn>(
				oldColumnName, metadataColumns) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.talend.commons.utils.data.list.UniqueStringGenerator#getBeanString(java.lang.Object)
			 */
			@Override
			protected String getBeanString(MetadataColumn bean) {
				return bean.getLabel();
			}

		};

		return uniqueStringGenerator.getUniqueString();
	}

	/**
	 * DOC amaumont Comment method "createNewMetadataColumn".
	 * 
	 * @return
	 */
	public MetadataColumn createNewMetadataColumn(String dbmsId) {
		MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE
				.createMetadataColumn();

		String columnName = getNextGeneratedColumnName(defaultLabel);
		metadataColumn.setLabel(columnName);
		metadataColumn.setOriginalField(columnName);
		ECodeLanguage codeLanguage = LanguageManager.getCurrentLanguage();
		if (codeLanguage == ECodeLanguage.JAVA) {
			metadataColumn.setTalendType(JavaTypesManager.getDefaultJavaType()
					.getId());
			if (dbmsId != null) {
				metadataColumn.setSourceType(TypesManager
						.getDBTypeFromTalendType(dbmsId, metadataColumn
								.getTalendType()));
			}
		}
		return metadataColumn;
	}

	/**
	 * Getter for defaultLabel.
	 * 
	 * @return the defaultLabel
	 */
	public String getDefaultLabel() {
		return this.defaultLabel;
	}

	/**
	 * Sets the defaultLabel.
	 * 
	 * @param defaultLabel
	 *            the defaultLabel to set
	 */
	public void setDefaultLabel(String defaultLabel) {
		this.defaultLabel = defaultLabel;
	}

}
