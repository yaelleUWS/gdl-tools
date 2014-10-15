package se.cambio.cds.model.app.dao;

import se.cambio.cds.model.CDSGlobalNames;
import se.cambio.cds.model.app.dto.CDSAppDTO;
import se.cambio.openehr.model.util.sql.DataSourceLocator;
import se.cambio.openehr.model.util.sql.GeneralOperations;
import se.cambio.openehr.util.exceptions.InstanceNotFoundException;
import se.cambio.openehr.util.exceptions.InternalErrorException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

/**
 * @author iago.corbal
 */
public class SQLGenericAppDAO implements GenericAppDAO {


    private SQLAppDAO dao;
    private DataSource dataSource;

    public SQLGenericAppDAO() throws InternalErrorException {
        dao = SQLAppFactory.getDAO();
        dataSource = DataSourceLocator.getDataSource(CDSGlobalNames.CDSS_DATA_SOURCE);
    }

    public CDSAppDTO searchByCDSAppId(String cdsAppId)
            throws InternalErrorException, InstanceNotFoundException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            return dao.searchByCDSAppId(connection, cdsAppId);
        } catch (SQLException e) {
            throw new InternalErrorException(e);
        } finally {
            GeneralOperations.closeConnection(connection);
        }
    }

    public Collection<CDSAppDTO> searchAll() throws InternalErrorException {
        Connection conexion = null;
        try {
            conexion = dataSource.getConnection();
            return dao.searchAll(conexion);
        } catch (SQLException e) {
            throw new InternalErrorException(e);
        } finally {
            GeneralOperations.closeConnection(conexion);
        }
    }

    public CDSAppDTO add(CDSAppDTO CDSAppDTO)
            throws InternalErrorException {
        Connection conexion = null;
        try {
            conexion = dataSource.getConnection();
            return dao.insert(conexion,CDSAppDTO);
        } catch (SQLException e) {
            throw new InternalErrorException(e);
        } finally {
            GeneralOperations.closeConnection(conexion);
        }
    }

    public void update(CDSAppDTO CDSAppDTO)
            throws InternalErrorException, InstanceNotFoundException {
        Connection conexion = null;
        try {
            conexion = dataSource.getConnection();
            dao.update(conexion,CDSAppDTO);
        } catch (SQLException e) {
            throw new InternalErrorException(e);
        } finally {
            GeneralOperations.closeConnection(conexion);
        }
    }

    public void remove(String cdsAppId)
            throws InternalErrorException, InstanceNotFoundException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            dao.remove(connection,cdsAppId);
        } catch (SQLException e) {
            throw new InternalErrorException(e);
        } finally {
            GeneralOperations.closeConnection(connection);
        }
    }

    @Override
    public Date getLastUpdateDate() throws InternalErrorException {
        Connection conexion = null;
        try {
            conexion = dataSource.getConnection();
            return dao.getLastUpdateDate(conexion);
        } catch (SQLException e) {
            throw new InternalErrorException(e);
        } finally {
            GeneralOperations.closeConnection(conexion);
        }
    }
}
/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 2.0/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  2.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *
 *  The Initial Developers of the Original Code are Iago Corbal and Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2012-2013
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */