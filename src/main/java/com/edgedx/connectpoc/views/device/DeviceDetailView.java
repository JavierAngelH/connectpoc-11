package com.edgedx.connectpoc.views.device;

import com.edgedx.connectpoc.entity.*;
import com.edgedx.connectpoc.model.DeviceType;
import com.edgedx.connectpoc.repository.DeviceRepository;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.views.ViewsConstants;
import com.edgedx.connectpoc.views.MainView;
import com.edgedx.connectpoc.views.dashboard.DashboardView;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.*;
import org.springframework.security.access.annotation.Secured;

import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_DEVICE_DETAIL;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_DEVICE_DETAIL;

@Route(value = PAGE_DEVICE_DETAIL, layout = MainView.class)
@PageTitle(TITLE_DEVICE_DETAIL)
@Secured({"ROLE_USER", "ROLE_SYSTEM", "ROLE_VIEWER"})
public class DeviceDetailView extends VerticalLayout {

    private final DeviceRepository deviceRepository;

    private final NodeConfigurationRepository nodeConfigurationRepository;


    private H3 title = new H3();

    Device device = new Device();

    public DeviceDetailView(DeviceRepository deviceRepository, NodeConfigurationRepository nodeConfigurationRepository) {
        this.deviceRepository = deviceRepository;
        this.nodeConfigurationRepository = nodeConfigurationRepository;

        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        Device deviceParam = ComponentUtil.getData(UI.getCurrent(), Device.class);
        if (deviceParam != null) {
            device = deviceParam;
            title.setText("Edit Device");
        } else {
            title.setText("Add Device");
        }

        FormLayout form = new FormLayout();
        form.setSizeFull();


        TextField serialNumber = new TextField("Serial Number");

        Select<DeviceType> deviceTypeSelect = new Select<DeviceType>();
        deviceTypeSelect.setLabel("Device Type");
        deviceTypeSelect.setItems(DeviceType.values());
        deviceTypeSelect.setItemLabelGenerator(deviceType -> deviceType.getDescription());


        BeanValidationBinder<Device> binder = new BeanValidationBinder<>(Device.class);
        binder.setBean(device);


        binder.forField(serialNumber).bind("serialNumber");
        binder.forField(deviceTypeSelect).bind("deviceType");

        form.add(serialNumber, deviceTypeSelect);
        add(form);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);


        HorizontalLayout buttonLayout = new HorizontalLayout(save, cancel);
        buttonLayout.setWidthFull();
        cancel.addClickListener(buttonClickEvent -> {
            this.getUI().ifPresent(ui ->
                    ui.navigate(DashboardView.class));
        });

        save.addClickListener(buttonClickEvent -> {
            binder.validate();
            if (binder.isValid()) {
                Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
                if (nodeConfiguration.isPresent())
                    device.setTestingPoint(nodeConfiguration.get().getFacility());
                deviceRepository.save(device);
                Notification notification = new Notification();
                notification.setText("Device saved succesfully");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(ViewsConstants.NOTIFICATION_DURATION);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.open();
                UI.getCurrent().getPage().getHistory().back();
            }
        });

        add(buttonLayout);
    }


}
