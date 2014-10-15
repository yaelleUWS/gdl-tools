package se.cambio.cds.model.app.dao;

import se.cambio.cds.model.app.dto.CDSAppDTO;
import se.cambio.openehr.util.exceptions.InstanceNotFoundException;
import se.cambio.openehr.util.exceptions.InternalErrorException;

import java.sql.Connection;
import java.util.Collection;
import java.util.Date;

/**
 * @author iago.corbal
 */
public interface SQLAppDAO {

    public CDSAppDTO searchByCDSAppId(Connection connection, String cdsAppId)
            throws InternalErrorException, InstanceNotFoundException;

    public Collection<CDSAppDTO> searchAll(Connection connection)
	    throws InternalErrorException;

    public CDSAppDTO insert(Connection connection, CDSAppDTO cdsAppDTO)
	    throws InternalErrorException;

    public void update(Connection connection, CDSAppDTO cdsAppDTO)
    throws InternalErrorException, InstanceNotFoundException;
    
    public void remove(Connection connection, String cdsAppId)
	    throws InternalErrorException, InstanceNotFoundException;

    public Date getLastUpdateDate(Connection connection)
            throws InternalErrorException;
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