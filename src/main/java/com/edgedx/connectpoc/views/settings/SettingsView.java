package com.edgedx.connectpoc.views.settings;

import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.views.MainView;
import com.edgedx.connectpoc.views.device.DeviceListView;
import com.edgedx.connectpoc.views.messagenotification.MessageNotificationsView;
import com.edgedx.connectpoc.views.nodeconfiguration.NodeConfigurationDetailView;
import com.edgedx.connectpoc.views.nodehealthstatistics.NodeHealthStatisticsView;
import com.edgedx.connectpoc.views.properties.PropertiesView;
import com.edgedx.connectpoc.views.users.UsersView;
import com.github.appreciated.card.RippleClickableCard;
import com.github.appreciated.card.content.IconItem;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_SETTINGS;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_SETTINGS;

@Route(value = PAGE_SETTINGS, layout = MainView.class)
@PageTitle(TITLE_SETTINGS)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

    private final NodeConfigurationRepository nodeConfigurationRepository;

    public SettingsView(NodeConfigurationRepository nodeConfigurationRepository){
        this.nodeConfigurationRepository = nodeConfigurationRepository;

        H3 title = new H3();
        title.setText("Settings");
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        title.getElement().getStyle().set("text-align", "center");
        title.setWidthFull();
        add(title);

        FormLayout layoutCharts = new FormLayout();
        layoutCharts.setWidthFull();

        RippleClickableCard devicesCard = buildDevicesCard();
        RippleClickableCard configurationCard = buildNodeConfigurationCard();
        RippleClickableCard healthStatisticsCard = buildNodeHealthStatisticsCard();
        RippleClickableCard propertiesCard = buildPropertiesCard();
        RippleClickableCard messageNotificationsCard = buildMessageNotificationsCard();
        RippleClickableCard usersCard = buildUsersCard();

        layoutCharts.add(devicesCard, configurationCard, healthStatisticsCard, propertiesCard, messageNotificationsCard,
                usersCard);
        layoutCharts.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("900px", 4, FormLayout.ResponsiveStep.LabelsPosition.TOP));
        add(layoutCharts);
    }

    public RippleClickableCard buildDevicesCard() {
        Icon icon = VaadinIcon.SERVER.create();
        icon.setColor("rgb(89 91 94)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, "Devices");
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                   this.getUI().ifPresent(ui ->
                            ui.navigate(DeviceListView.class));
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildNodeConfigurationCard() {
        Icon icon = VaadinIcon.MOBILE.create();
        icon.setColor("rgb(89 91 94)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, "Node Configuration");
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    this.getUI().ifPresent(ui ->
                            ui.navigate(NodeConfigurationDetailView.class));
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildNodeHealthStatisticsCard() {
        Icon icon = VaadinIcon.LINES_LIST.create();
        icon.setColor("rgb(89 91 94)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, "Node Health Statistics");
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    this.getUI().ifPresent(ui ->
                            ui.navigate(NodeHealthStatisticsView.class));
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildPropertiesCard() {
        Icon icon = VaadinIcon.MODAL_LIST.create();
        icon.setColor("rgb(89 91 94)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, "Properties");
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    this.getUI().ifPresent(ui ->
                            ui.navigate(PropertiesView.class));
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildMessageNotificationsCard() {
        Icon icon = VaadinIcon.TASKS.create();
        icon.setColor("rgb(89 91 94)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, "Message Notifications");
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    this.getUI().ifPresent(ui ->
                            ui.navigate(MessageNotificationsView.class));
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildUsersCard() {
        Icon icon = VaadinIcon.USERS.create();
        icon.setColor("rgb(89 91 94)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, "Users");
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    this.getUI().ifPresent(ui ->
                            ui.navigate(UsersView.class));
                }, item);
        card.setWidth("100%");
        return card;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if(!nodeConfiguration.isPresent())
            beforeEnterEvent.forwardTo(NodeConfigurationDetailView.class);
    }

}
