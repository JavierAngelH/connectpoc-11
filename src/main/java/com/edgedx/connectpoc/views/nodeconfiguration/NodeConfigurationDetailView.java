package com.edgedx.connectpoc.views.nodeconfiguration;

import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.views.ViewsConstants;
import com.edgedx.connectpoc.views.MainView;
import com.edgedx.connectpoc.views.dashboard.DashboardView;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.*;

@Route(value = PAGE_NODECONFIGURATION_DETAIL, layout = MainView.class)
@PageTitle(TITLE_NODECONFIGURATION_DETAIL)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class NodeConfigurationDetailView extends VerticalLayout {

    private final String[] COUNTRIES_LIST = {"Ethiopia", "Kenya", "Mozambique", "India", "US"};

    private final NodeConfigurationRepository nodeConfigurationRepository;

    private H3 title = new H3();

    NodeConfiguration nodeConfiguration = new NodeConfiguration();

    public NodeConfigurationDetailView(NodeConfigurationRepository nodeConfigurationRepository) {
        this.nodeConfigurationRepository = nodeConfigurationRepository;

        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        Optional<NodeConfiguration> existingConfig = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if(existingConfig.isPresent()){
            nodeConfiguration = existingConfig.get();
            title.setText("Edit Node Configuration");
        } else {
            title.setText("Set Node Configuration");
        }

        FormLayout form = new FormLayout();
        form.setSizeFull();

        TextField facility = new TextField("Facility Name");

        Select<String> country = new Select<String>();
        country.setLabel("Country");
        country.setItems(COUNTRIES_LIST);

        TextField province = new TextField("Province");

        TextField district = new TextField("District");

        TextField facilityCode = new TextField("Facility Code");

        BeanValidationBinder<NodeConfiguration> binder = new BeanValidationBinder<>(NodeConfiguration.class);
        binder.setBean(nodeConfiguration);


        binder.forField(facility).bind("facility");
        binder.forField(country).bind("country");
        binder.forField(province).bind("province");
        binder.forField(district).bind("district");
        binder.forField(facilityCode).bind("facilityCode");

        if(existingConfig.isPresent())
            facility.setReadOnly(true);

            form.add(country, district, province, facility, facilityCode);
        add(form);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout buttonLayout = new HorizontalLayout(save);

        save.addClickListener(buttonClickEvent -> {
            binder.validate();
            if(binder.isValid())
            {
                nodeConfigurationRepository.save(nodeConfiguration);
                Notification notification = new Notification();
                notification.setText("Configuration saved succesfully");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(ViewsConstants.NOTIFICATION_DURATION);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.open();
                this.getUI().ifPresent(ui ->
                        ui.navigate(DashboardView.class));
            }
        });

        buttonLayout.setWidthFull();
        if (existingConfig.isPresent()) {
            Button cancel = new Button("Cancel");
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
            cancel.addClickListener(buttonClickEvent -> {
                UI.getCurrent().getPage().getHistory().back();
            });
            buttonLayout.add(cancel);
        }

        add(buttonLayout);
    }
}
