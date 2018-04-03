package org.rapla.client.gwt;

import com.google.gwt.core.client.JsDate;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import org.rapla.RaplaResources;
import org.rapla.client.ReservationController;
import org.rapla.client.dialog.gwt.VueDialog;
import org.rapla.client.dialog.gwt.components.BulmaTextColor;
import org.rapla.client.dialog.gwt.components.VueLabel;
import org.rapla.client.dialog.gwt.components.layout.VerticalFlex;
import org.rapla.client.menu.MenuFactory;
import org.rapla.client.menu.MenuInterface;
import org.rapla.client.menu.gwt.VueMenu;
import org.rapla.client.menu.gwt.DefaultVueMenuItem;
import org.rapla.components.util.TimeInterval;
import org.rapla.entities.User;
import org.rapla.entities.configuration.CalendarModelConfiguration;
import org.rapla.entities.configuration.Preferences;
import org.rapla.entities.configuration.RaplaMap;
import org.rapla.facade.CalendarOptions;
import org.rapla.facade.CalendarSelectionModel;
import org.rapla.facade.RaplaComponent;
import org.rapla.facade.RaplaFacade;
import org.rapla.facade.client.ClientFacade;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.logger.Logger;
import org.rapla.plugin.abstractcalendar.RaplaBuilder;
import org.rapla.plugin.autoexport.AutoExportPlugin;
import org.rapla.plugin.tableview.RaplaTableColumn;
import org.rapla.plugin.tableview.RaplaTableModel;
import org.rapla.plugin.tableview.internal.TableConfig;
import org.rapla.scheduler.Promise;
import org.rapla.scheduler.ResolvedPromise;
import org.rapla.storage.dbrm.RemoteAuthentificationService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsType
public class JsApi {

  private final RaplaFacade facade;
  private final Logger logger;
  private final ReservationController reservationController;
  private final CalendarSelectionModel calendarModel;
  private final RemoteAuthentificationService remoteAuthentificationService;
  private final RaplaLocale raplaLocale;
  private final ClientFacade clientFacade;
  private final Provider<RaplaBuilder> raplaBuilder;
  private final RaplaResources i18n;
  private final MenuFactory menuFactory;
  private final TableConfig.TableConfigLoader tableConfigLoader;

  @JsIgnore
  @Inject
  public JsApi(
      ClientFacade facade, Logger logger, ReservationController reservationController, CalendarSelectionModel calendarModel,
      RemoteAuthentificationService remoteAuthentificationService, RaplaLocale raplaLocale, Provider<RaplaBuilder> raplaBuilder, RaplaResources i18n,
      MenuFactory menuFactory, TableConfig.TableConfigLoader tableConfigLoader
  ) {
    this.clientFacade = facade;
    this.i18n = i18n;
    this.menuFactory = menuFactory;
    this.tableConfigLoader = tableConfigLoader;
    this.facade = clientFacade.getRaplaFacade();
    this.logger = logger;
    this.reservationController = reservationController;
    this.calendarModel = calendarModel;
    this.remoteAuthentificationService = remoteAuthentificationService;
    this.raplaLocale = raplaLocale;
    this.raplaBuilder = raplaBuilder;
  }

  public MenuFactory getMenuFactory() {
    return menuFactory;
  }

  public CalendarSelectionModel getCalendarModel() {
    return calendarModel;
  }

  public RemoteAuthentificationService getRemoteAuthentification() {
    return remoteAuthentificationService;
  }

  public RaplaLocale getRaplaLocale() {
    return raplaLocale;
  }

  public ReservationController getReservationController() {
    return reservationController;
  }

  public RaplaBuilder createBuilder() {
    return raplaBuilder.get();
  }

  public RaplaResources getI18n() {
    return i18n;
  }

  public void warn(String message) {
    logger.warn(message);
  }

  public void info(String message) {
    logger.info(message);
  }

  public void error(String message) {
    logger.error(message);
  }

  public void debug(String message) {
    logger.debug(message);
  }

  public CalendarOptions getCalendarOptions() throws RaplaException {
    return RaplaComponent.getCalendarOptions(getUser(), facade);
  }

  public User getUser() throws RaplaException {
    return clientFacade.getUser();
  }

  public String[] getCalendarNames() throws RaplaException {
    final Preferences preferences = getFacade().getPreferences(getUser());
    RaplaMap<CalendarModelConfiguration> exportMap = preferences.getEntry(AutoExportPlugin.PLUGIN_ENTRY);
    if (exportMap != null) {
      return exportMap.keySet().toArray(new String[] {});
    }
    return new String[] {};
  }

  public RaplaFacade getFacade() {
    return facade;
  }

  public JsDate toJsDate(Date date) {
    if (date != null)
      return JsDate.create(date.getTime());
    return null;
  }

  public Object[] toArray(Collection<?> collection) {
    return collection.toArray();
  }

  public Object[] streamToArray(Stream<?> stream) {
    return stream.toArray();
  }

  public Promise<Integer> testDialog() {
    VueDialog dialog = new VueDialog(
        new VerticalFlex()
            .addChild(new VueLabel("Hallo Welt 1").color(BulmaTextColor.DANGER))
            .addChild(new VueLabel("Hallo Welt 2").color(BulmaTextColor.SUCCESS)),
        new String[] {}
    );
    dialog.start(false)
          .thenAccept(i -> RaplaVue.emit("gwt-dialog-close"));
    return dialog.getPromise();
  }

  public MenuInterface testMenu() {
    final VueMenu menu = new VueMenu();
    menu.addMenuItem(new DefaultVueMenuItem("Item 1").action((ctx) -> logger.info("user has chosen 'Item 1'")));
    menu.addSeparator();
    menu.addMenuItem(new DefaultVueMenuItem("Item 2").action((ctx) -> logger.info("user has chosen 'Item 2'")));
    return menu;
  }

  public Integer toInteger(int integer) {
    return Integer.valueOf(integer);
  }

  public Set<Object> asSet(Object[] elements) {
    return Arrays.stream(elements).collect(Collectors.toSet());
  }

  public TimeInterval createInterval(Date from, Date to) {
    return new TimeInterval(from, to);
  }

  public Promise<RaplaTableModel> loadTableModel(CalendarSelectionModel model) {
    final String viewId = model.getViewId();
    if (viewId.equals("table_appointments")) {
      return loadTableModel("appointments", (() -> model.queryBlocks(model.getTimeIntervall())));
    } else if (viewId.equals("table_events")) {
      return loadTableModel("events", (() -> model.queryReservations(model.getTimeIntervall()).thenApply(ArrayList::new)));
    } else {
      return new ResolvedPromise<>(new RaplaException("No table data found for view " + viewId));
    }
  }

  private <T> Promise<RaplaTableModel> loadTableModel(String viewId, Supplier<Promise<List<T>>> initFunction) {
    RaplaTableModel<T, Object> tableModel;
    try {
      User user = getUser();
      List<RaplaTableColumn<T, Object>> raplaTableColumns = tableConfigLoader.loadColumns(viewId, user);
      tableModel = new RaplaTableModel<>(raplaTableColumns);
    } catch (RaplaException e) {
      return new ResolvedPromise<>(e);
    }
    return initFunction.get().thenApply((blocks) -> tableModel.setObjects(blocks));
  }

}
