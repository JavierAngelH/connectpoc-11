package com.edgedx.connectpoc.views.properties;

import com.edgedx.connectpoc.entity.Property;
import com.edgedx.connectpoc.repository.PropertiesRepository;
import com.edgedx.connectpoc.views.ViewsConstants;
import com.edgedx.connectpoc.views.MainView;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.*;
import org.springframework.security.access.annotation.Secured;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_PROPERTY_DETAIL;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_PROPERTY_DETAIL;

@Route(value = PAGE_PROPERTY_DETAIL, layout = MainView.class)
@PageTitle(TITLE_PROPERTY_DETAIL)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
@PreserveOnRefresh
public class PropertyDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final PropertiesRepository propertiesRepository;

    public PropertyDetailView(PropertiesRepository propertiesRepository){
        this.propertiesRepository = propertiesRepository;
        H3 title = new H3("Edit Property");

        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        Property property = ComponentUtil.getData(UI.getCurrent(), Property.class);

        FormLayout form = new FormLayout();
        form.setSizeFull();

        TextField propertyName = new TextField("Property Name");
        TextField propertyValue = new TextField("Property Value");

        BeanValidationBinder<Property> binder = new BeanValidationBinder<>(Property.class);
        binder.setBean(property);
        binder.forField(propertyName).bind("propertyName");
        binder.forField(propertyValue).bind("propertyValue");

        form.add(propertyName, propertyValue);
        add(form);

        propertyName.setReadOnly(true);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttonLayout = new HorizontalLayout(save, cancel);
        buttonLayout.setWidthFull();
        cancel.addClickListener(buttonClickEvent -> {
            this.getUI().ifPresent(ui ->
                    ui.navigate(PropertiesView.class));
        });

        save.addClickListener(buttonClickEvent -> {
            binder.validate();
            if(binder.isValid())
            {
                propertiesRepository.save(property);
                Notification notification = new Notification();
                notification.setText("Property updated succesfully");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(ViewsConstants.NOTIFICATION_DURATION);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.open();
                this.getUI().ifPresent(ui ->
                        ui.navigate(PropertiesView.class));
            }
        });
        add(buttonLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Property propertyParam = ComponentUtil.getData(UI.getCurrent(), Property.class);
        if (propertyParam == null) {
            beforeEnterEvent.forwardTo(PropertiesView.class);
        }
    }
}