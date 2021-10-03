package com.edgedx.connectpoc.views.cartridges;

import com.edgedx.connectpoc.entity.CartridgeInventory;
import com.edgedx.connectpoc.model.CartridgeType;
import com.edgedx.connectpoc.entity.Device;
import com.edgedx.connectpoc.repository.CartridgeInventoryRepository;
import com.edgedx.connectpoc.repository.DeviceRepository;
import com.edgedx.connectpoc.views.ViewsConstants;
import com.edgedx.connectpoc.views.MainView;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.*;
import org.springframework.security.access.annotation.Secured;

import java.time.LocalDate;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_CARTRIDGE_DETAIL;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_CARTRIDGE_DETAIL;

@Route(value = PAGE_CARTRIDGE_DETAIL, layout = MainView.class)
@PageTitle(TITLE_CARTRIDGE_DETAIL)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
@PreserveOnRefresh
public class CartridgeDetailView extends VerticalLayout {

    private final CartridgeInventoryRepository cartridgeInventoryRepository;

    private final DeviceRepository deviceRepository;

    private H3 title = new H3();

    CartridgeInventory cartridge = new CartridgeInventory();

    public CartridgeDetailView(CartridgeInventoryRepository cartridgeInventoryRepository, DeviceRepository deviceRepository){
        this.cartridgeInventoryRepository = cartridgeInventoryRepository;
        this.deviceRepository = deviceRepository;

        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        CartridgeInventory cartridgeParam = ComponentUtil.getData(UI.getCurrent(), CartridgeInventory.class);
        if(cartridgeParam!=null){
            cartridge = cartridgeParam;
            title.setText("Edit Cartridge");
        }
        else{
            title.setText("Add Cartridge");
        }

        FormLayout form = new FormLayout();
        form.setSizeFull();

        TextField lotId = new TextField("Cartridge Lot ID");

        Select<CartridgeType> cartridgeTypeSelect = new Select<CartridgeType>();
        cartridgeTypeSelect.setLabel("Cartridge Type");
        cartridgeTypeSelect.setItems(CartridgeType.values());
        cartridgeTypeSelect.setItemLabelGenerator(cartridgeType -> cartridgeType.getDescription());

        DatePicker expirationDate = new DatePicker();
        expirationDate.setLabel("Expiration Date");
        expirationDate.setInitialPosition(LocalDate.now());
        expirationDate.setMin(LocalDate.now());

        IntegerField quantity = new IntegerField();
        quantity.setLabel("Quantity");
        quantity.setMin(1);

        Select<Device> device = new Select<Device>();
        device.setItems(deviceRepository.findAll());
        device.setLabel("Device");
        device.setItemLabelGenerator(device1 -> device1.getDeviceType().getDescription());

        BeanValidationBinder<CartridgeInventory> binder = new BeanValidationBinder<>(CartridgeInventory.class);
        binder.setBean(cartridge);


        binder.forField(lotId).bind("lotId");
        binder.forField(cartridgeTypeSelect).bind("cartridgeType");
        binder.forField(expirationDate).bind("expirationDate");
        binder.forField(quantity).bind("quantity");
        binder.forField(device).bind("device");

        form.add(lotId, quantity, cartridgeTypeSelect, expirationDate, device);
        add(form);

        if(cartridgeParam != null){
            device.setReadOnly(true);
        }

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);


        HorizontalLayout buttonLayout = new HorizontalLayout(save, cancel);
        buttonLayout.setWidthFull();
        cancel.addClickListener(buttonClickEvent -> {
            this.getUI().ifPresent(ui ->
                    ui.navigate(CartridgeInventoryView.class));
        });

        save.addClickListener(buttonClickEvent -> {
            binder.validate();
            if(binder.isValid())
            {
                cartridgeInventoryRepository.save(cartridge);
                Notification notification = new Notification();
                notification.setText("Information saved succesfully");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(ViewsConstants.NOTIFICATION_DURATION);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.open();
                this.getUI().ifPresent(ui ->
                        ui.navigate(CartridgeInventoryView.class));
            }
        });

        add(buttonLayout);
    }

}
