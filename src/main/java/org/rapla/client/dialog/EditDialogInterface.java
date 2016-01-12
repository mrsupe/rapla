package org.rapla.client.dialog;

import org.rapla.client.PopupContext;
import org.rapla.client.swing.EditController;
import org.rapla.framework.RaplaException;

import java.util.Collection;
import java.util.List;

public interface EditDialogInterface<T>
{

    List<?> getObjects();

    DialogInterface getDialog();

    void start(Collection<T> editObjects, String title, PopupContext popupContext, boolean isNew, EditController.EditCallback<List<T>> callback) throws RaplaException;

}
