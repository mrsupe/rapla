package org.rapla.client.swing.internal.dagger;

import org.rapla.client.ClientService;
import org.rapla.client.UserClientService;
import org.rapla.client.swing.dagger.DaggerRaplaJavaClientStartupModule;
import org.rapla.components.iolayer.DefaultIO;
import org.rapla.components.iolayer.IOInterface;
import org.rapla.components.iolayer.WebstartIO;
import org.rapla.facade.ClientFacade;
import org.rapla.framework.StartupEnvironment;
import org.rapla.inject.InjectionContext;
import org.rapla.inject.dagger.DaggerReflectionStarter;
import org.rapla.inject.raplainject.SimpleRaplaInjector;
import org.rapla.logger.Logger;

import javax.inject.Provider;

public class DaggerClientCreator
{
    static class UserServiceProvider implements  Provider<UserClientService>
    {
        UserClientService client;

        @Override public UserClientService get()
        {
            return client;
        }

        public void setClient(UserClientService client)
        {
            this.client = client;
        }
    }

    public static ClientService create(StartupEnvironment startupEnvironment) throws Exception
    {
        String moduleId = DaggerReflectionStarter.loadModuleId( ClientService.class.getClassLoader());
        return create(startupEnvironment, moduleId);
    }

    public static ClientService create(StartupEnvironment startupEnvironment, String moduleId) throws Exception
    {

        final ClientService client;
        UserServiceProvider userClientServiceProvider = new UserServiceProvider();
        Logger logger = startupEnvironment.getBootstrapLogger();
        SimpleRaplaInjector injector = new SimpleRaplaInjector( logger);
        boolean webstartEnabled = startupEnvironment.getStartupMode() == StartupEnvironment.WEBSTART;
        injector.addComponentInstance(Logger.class,logger);
        injector.addComponentInstanceProvider(IOInterface.class, () -> webstartEnabled ? new WebstartIO(logger): new DefaultIO(logger));
        injector.addComponentInstanceProvider(UserClientService.class, userClientServiceProvider);
        injector.addComponentInstance(StartupEnvironment.class, startupEnvironment);
        injector.initFromMetaInfService(InjectionContext.swing);
        //if (true)
        {
            client = injector.getInstance( ClientService.class);
            userClientServiceProvider.setClient( (UserClientService) client );
            return client;
        }
//        final DaggerRaplaJavaClientStartupModule startupModule = new DaggerRaplaJavaClientStartupModule(startupEnvironment,userClientServiceProvider);
//        boolean useReflection = true;
//        if (useReflection)
//        {
//            client = DaggerReflectionStarter.startWithReflectionAndStartupModule(moduleId,ClientService.class, DaggerReflectionStarter.Scope.JavaClient, startupModule);
//        }
//        else
//        {
//            org.rapla.client.swing.dagger.RaplaJavaClientComponent component= org.rapla.client.swing.dagger.DaggerRaplaJavaClientComponent.builder().daggerRaplaJavaClientStartupModule(startupModule).build();
//            client = component.getClientService();
//        }
//        userClientServiceProvider.setClient( (UserClientService) client );
//        return client;
    }

    public static ClientFacade createFacade(StartupEnvironment startupEnvironment) throws Exception
    {
        String moduleId = DaggerReflectionStarter.loadModuleId( ClientService.class.getClassLoader());
        final ClientFacade client;
        UserServiceProvider userClientServiceProvider = new UserServiceProvider();
        final DaggerRaplaJavaClientStartupModule startupModule = new DaggerRaplaJavaClientStartupModule(startupEnvironment,userClientServiceProvider);
        boolean useReflection = true;
        if (useReflection)
        {
            client = DaggerReflectionStarter.startWithReflectionAndStartupModule(moduleId,ClientFacade.class, DaggerReflectionStarter.Scope.JavaClient, startupModule);
        }
        else
        {
//            org.rapla.client.swing.dagger.RaplaJavaClientComponent component= org.rapla.client.swing.dagger.DaggerRaplaJavaClientComponent.builder().daggerRaplaJavaClientStartupModule(startupModule).build();
            client = null;//component.getClientFacade();
        }
        userClientServiceProvider.setClient( (UserClientService) client );
        return client;
    }

}
