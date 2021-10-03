package com.edgedx.connectpoc.views.messagenotification;

import com.edgedx.connectpoc.entity.MessageNotification;
import com.edgedx.connectpoc.repository.MessageNotificationRepository;
import com.edgedx.connectpoc.views.ViewsConstants;
import com.edgedx.connectpoc.views.MainView;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_MESSAGENOTIFICATION_DETAIL;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_MESSAGENOTIFICATION_DETAIL;

@Route(value = PAGE_MESSAGENOTIFICATION_DETAIL, layout = MainView.class)
@PageTitle(TITLE_MESSAGENOTIFICATION_DETAIL)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
@PreserveOnRefresh
public class MessageNotificationDetailView extends VerticalLayout implements BeforeEnterObserver {

    private final MessageNotificationRepository messageNotificationRepository;

    public MessageNotificationDetailView(MessageNotificationRepository messageNotificationRepository){
        this.messageNotificationRepository = messageNotificationRepository;

        H3 title = new H3("Edit Message Notification");

        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        MessageNotification messageNotification = ComponentUtil.getData(UI.getCurrent(), MessageNotification.class);

        FormLayout form = new FormLayout();
        form.setSizeFull();

        TextField triggerName = new TextField("Trigger Name");
        TextField text = new TextField("Text");
        Checkbox status = new Checkbox("Active");

        BeanValidationBinder<MessageNotification> binder = new BeanValidationBinder<>(MessageNotification.class);
        binder.setBean(messageNotification);
        binder.forField(triggerName).bind("triggerName");
        binder.forField(text).bind("text");
        binder.forField(status).bind("status");

        form.add(triggerName, text, status);
        add(form);

        triggerName.setReadOnly(true);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);


        HorizontalLayout buttonLayout = new HorizontalLayout(save, cancel);
        buttonLayout.setWidthFull();
        cancel.addClickListener(buttonClickEvent -> {
            this.getUI().ifPresent(ui ->
                    ui.navigate(MessageNotificationsView.class));
        });

        save.addClickListener(buttonClickEvent -> {
            binder.validate();
            if(binder.isValid())
            {
                messageNotificationRepository.save(messageNotification);
                Notification notification = new Notification();
                notification.setText("Message updated succesfully");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(ViewsConstants.NOTIFICATION_DURATION);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.open();
                this.getUI().ifPresent(ui ->
                        ui.navigate(MessageNotificationsView.class));
            }
        });

        add(buttonLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        MessageNotification messageParam = ComponentUtil.getData(UI.getCurrent(), MessageNotification.class);
        if (messageParam == null) {
            beforeEnterEvent.forwardTo(MessageNotificationsView.class);
        }
    }
}
