package org.rapla.client.gwt.components;

import java.util.Date;

import org.gwtbootstrap3.extras.datepicker.client.ui.DatePicker;
import org.gwtbootstrap3.extras.datepicker.client.ui.base.constants.DatePickerLanguage;
import org.rapla.components.i18n.BundleManager;
import org.rapla.components.util.ParseDateException;
import org.rapla.components.util.SerializableDateTimeFormat;
import org.rapla.framework.RaplaLocale;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

public class DateComponent extends SimplePanel implements ValueChangeHandler<String>
{
    public interface DateValueChanged
    {
        void valueChanged(Date newValue);
    }

    private final TextBox tb;
    private final RaplaLocale locale;
    private final DatePicker datePicker;
    private final DateValueChanged changeHandler;

    public DateComponent(Date initDate, RaplaLocale locale, final DateValueChanged changeHandler, BundleManager bundleManager)
    {
        super();
        addStyleName("datePicker");
        this.locale = locale;
        this.changeHandler = changeHandler;
        if (initDate == null)
        {
            initDate = new Date();
        }
//        if (!InputUtils.isHtml5DateInputSupported())
        {
            datePicker = new DatePicker();
            final DatePickerLanguage lang = DatePickerLanguage.valueOf(locale.getLocale().getLanguage().toUpperCase());
            datePicker.setLanguage(lang);
            datePicker.setFormat(bundleManager.getFormats().getFormatDateShort().toLowerCase());
//            datePicker.setFormat("dd.mm.yyyy");
            datePicker.setShowTodayButton(true);
            datePicker.setForceParse(true);
            add(datePicker);
            datePicker.addValueChangeHandler(new ValueChangeHandler<Date>()
            {
                @Override
                public void onValueChange(ValueChangeEvent<Date> event)
                {
                    final Date value = event.getValue();
                    changeHandler.valueChanged(value);
                }
            });
            tb = null;
        }
//        else
//        {
//            datePicker = null;
//            tb = new TextBox();
//            tb.setStyleName("dateComponent");
//            add(tb);
//            tb.setValue(locale.formatDate(initDate), false);
//            tb.getElement().setAttribute("type", "date");
//            tb.addValueChangeHandler(this);
//        }
        setDate(initDate);
    }

    public void setDate(Date date)
    {
        if (tb != null)
        {
//            tb.setValue(locale.formatDate(date), false);
            this.tb.setValue(SerializableDateTimeFormat.INSTANCE.formatDate(date), false);
        }
        if (datePicker != null)
        {
            datePicker.setValue(date, false);
        }
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event)
    {
        final String dateInIso = event.getValue();
        try
        {
            final Date newValue = SerializableDateTimeFormat.INSTANCE.parseDate(dateInIso, false);
            setDate(newValue);
            changeHandler.valueChanged(newValue);
        }
        catch (ParseDateException e)
        {
            GWT.log("error parsing date " + dateInIso, e);
        }
    }
}
