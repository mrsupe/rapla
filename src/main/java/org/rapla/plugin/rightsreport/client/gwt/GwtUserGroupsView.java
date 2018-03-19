package org.rapla.plugin.rightsreport.client.gwt;

import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import org.rapla.client.RaplaWidget;
import org.rapla.inject.DefaultImplementation;
import org.rapla.inject.InjectionContext;
import org.rapla.plugin.rightsreport.client.AdminUserUserGroupsView;
import org.rapla.scheduler.Promise;

@DefaultImplementation(of=AdminUserUserGroupsView.class,context = InjectionContext.gwt)
public class GwtUserGroupsView implements AdminUserUserGroupsView {
    @Override
    public Promise<RaplaWidget> init(BiFunction<Object, Object, Promise<Void>> moveFunction, Runnable closeCmd) {
        return null;
    }

    @Override
    public void updateView() {

    }

    @Override
    public Object getComponent() {
        return null;
    }
}
