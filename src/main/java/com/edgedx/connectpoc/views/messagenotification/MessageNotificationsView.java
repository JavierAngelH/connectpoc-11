package com.edgedx.connectpoc.views.messagenotification;

import com.edgedx.connectpoc.entity.MessageNotification;
import com.edgedx.connectpoc.repository.MessageNotificationRepository;
import com.edgedx.connectpoc.views.MainView;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_MESSAGE_NOTIFICATIONS;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_MESSAGE_NOTIFICATIONS;

@Route(value = PAGE_MESSAGE_NOTIFICATIONS, layout = MainView.class)
@PageTitle(TITLE_MESSAGE_NOTIFICATIONS)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class MessageNotificationsView extends VerticalLayout {

    private final MessageNotificationRepository messageNotificationRepository;

    Grid<MessageNotification> grid = new Grid<>(MessageNotification.class);

    public MessageNotificationsView(MessageNotificationRepository messageNotificationRepository) {
        this.messageNotificationRepository = messageNotificationRepository;
        setSizeFull();

        H3 title = new H3();
        title.setText("Message Notifications");
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, title);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
        grid.setColumns("triggerName");
        grid.getColumnByKey("triggerName").setHeader("Event").setFlexGrow(3);
        grid.addColumn(messageNotification -> {
            if (messageNotification.getStatus())
                return "Active";
            else
                return "Inactive";
        }).setHeader("Status").setAutoWidth(true);

        grid.setItems(messageNotificationRepository.findAll());
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        add(grid);

        Button editMessage = new Button("Edit message");
        editMessage.setEnabled(false);
        editMessage.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        grid.addSelectionListener(selectionEvent -> {
            if (!selectionEvent.getAllSelectedItems().isEmpty())
                editMessage.setEnabled(true);
            else
                editMessage.setEnabled(false);

        });
        HorizontalLayout buttonLayout = new HorizontalLayout(editMessage);
        buttonLayout.setWidthFull();

        editMessage.addClickListener(buttonClickEvent -> {
            Optional<MessageNotification> selected = grid.getSelectedItems().stream().findFirst();
            ComponentUtil.setData(UI.getCurrent(), MessageNotification.class, selected.get());
            this.getUI().ifPresent(ui ->
                    ui.navigate(MessageNotificationDetailView.class));
        });

        add(buttonLayout);
    }
}
