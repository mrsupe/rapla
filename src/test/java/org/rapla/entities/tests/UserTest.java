/*--------------------------------------------------------------------------*
 | Copyright (C) 2014 Christopher Kohlhaas                                  |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.entities.tests;
import java.util.Locale;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.rapla.RaplaTestCase;
import org.rapla.ServletTestBase;
import org.rapla.entities.Category;
import org.rapla.entities.User;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.RaplaException;
import org.rapla.framework.logger.Logger;
import org.rapla.framework.logger.RaplaBootstrapLogger;
import org.rapla.server.ServerServiceContainer;

import javax.inject.Provider;

@RunWith(JUnit4.class)
public class UserTest  {
    
    ClientFacade adminFacade;
    ClientFacade testFacade;
    Locale locale;
    Server server;

    @Before
    public void setUp() throws Exception {
        int port = 8052;
        final Logger raplaLogger = RaplaBootstrapLogger.createRaplaLogger();
        final ServerServiceContainer servlet = RaplaTestCase.createServer(raplaLogger, "testdefault.xml");
        server = ServletTestBase.createServer(servlet, port);
        // start the client service
        final Provider<ClientFacade> facadeWithRemote = RaplaTestCase.createFacadeWithRemote(raplaLogger, port);
        adminFacade = facadeWithRemote.get();
        adminFacade.login("homer","duffs".toCharArray());
        locale = Locale.getDefault();

        try
        {
            Category groups = adminFacade.edit( adminFacade.getUserGroupsCategory() );
            Category testGroup = adminFacade.newCategory();
            testGroup.setKey("test-group");
            groups.addCategory( testGroup );
            adminFacade.store( groups );
        } catch (RaplaException ex) {
            adminFacade.logout();
            throw ex;
            
        }
        testFacade = facadeWithRemote.get();
        boolean canLogin = testFacade.login("homer","duffs".toCharArray());
        Assert.assertTrue("Can't login", canLogin);
    }

    @After
    public void tearDown() throws Exception {
        adminFacade.logout();
        testFacade.logout();
        server.stop();
    }

    @Test
    public void testCreateAndRemoveUser() throws Exception {
        User user = adminFacade.newUser();
        user.setUsername("test");
        user.setName("Test User");
        adminFacade.store( user );
        testFacade.refresh();
        User newUser = testFacade.getUser("test");
        testFacade.remove( newUser );
        // first create a new resource and set the permissions
    }


}





