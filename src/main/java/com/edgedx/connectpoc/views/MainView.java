package com.edgedx.connectpoc.views;

import com.edgedx.connectpoc.security.SecurityUtils;
import com.edgedx.connectpoc.views.dashboard.DashboardView;
import com.edgedx.connectpoc.views.problemnotification.ProblemNotificationView;
import com.edgedx.connectpoc.views.settings.SettingsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinServlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.*;

@Viewport(VIEWPORT)
@PWA(name = "ConnectPOC", shortName = "ConnectPOC",
        startPath = "login",
        backgroundColor = "#227aef", themeColor = "#227aef",
        offlinePath = "offline-page.html",
        offlineResources = {"images/offline-login-banner.jpg"},
        enableInstallPrompt = false)
@Push
public class MainView extends AppLayout
        implements Broadcaster.BroadcastListener {

    private final Tabs menu;

    public MainView() {
        Broadcaster.register(this);
        this.addDetachListener(detachEvent -> {
            Broadcaster.unregister(this);
        });
        this.setDrawerOpened(false);
        Image logo = new Image("images/icon.png", "ConnectPOC");
        logo.setWidth("50px");
        logo.setHeight("50px");
        Span appName = new Span(logo);
        appName.getElement().getStyle().set("margin-left", "5px");
        appName.getElement().getStyle().set("margin-top", "5px");

        menu = createMenuTabs();
        //	menu.setOrientation(Tabs.Orientation.VERTICAL);
        menu.setOrientation(Tabs.Orientation.HORIZONTAL);
        this.addToNavbar(true, menu);
        //	this.addToDrawer(menu);

        getElement().addEventListener("search-focus", e -> {
            getElement().getClassList().add("hide-navbar");
        });

        getElement().addEventListener("search-blur", e -> {
            getElement().getClassList().remove("hide-navbar");
        });

    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();

        RouteConfiguration configuration = RouteConfiguration.forSessionScope();
        if (configuration.isRouteRegistered(this.getContent().getClass())) {
            String target = configuration.getUrl(this.getContent().getClass());
            Optional<Component> tabToSelect = menu.getChildren().filter(tab -> {
                Component child = tab.getChildren().findFirst().get();
                return child instanceof RouterLink && ((RouterLink) child).getHref().equals(target);
            }).findFirst();
            tabToSelect.ifPresent(tab -> menu.setSelectedTab((Tab) tab));
        } else {
            menu.setSelectedTab(null);
        }
    }

    private static Tabs createMenuTabs() {
        final Tabs tabs = new Tabs();
        // tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.add(getAvailableTabs());
        return tabs;
    }

    private static Tab[] getAvailableTabs() {
        final List<Tab> tabs = new ArrayList<>(4);

        tabs.add(createTab(VaadinIcon.PIE_CHART, TITLE_DASHBOARD, DashboardView.class));

        if (SecurityUtils.isAccessGranted(SettingsView.class)) {
            tabs.add(createTab(VaadinIcon.COG_O, TITLE_SETTINGS, SettingsView.class));
        }

        if (SecurityUtils.isAccessGranted(ProblemNotificationView.class)) {
            tabs.add(createTab(VaadinIcon.WARNING, TITLE_PROBLEMNOTIFICATION, ProblemNotificationView.class));
        }

        final String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
        final Tab logoutTab = createTab(createLogoutLink(contextPath));
        tabs.add(logoutTab);
        return tabs.toArray(new Tab[tabs.size()]);
    }

    private static Tab createTab(VaadinIcon icon, String title, Class<? extends Component> viewClass) {
        return createTab(populateLink(new RouterLink(null, viewClass), icon, title));
    }

    private static Tab createTab(Component content) {
        final Tab tab = new Tab();
        tab.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
        tab.add(content);
        return tab;
    }

    private static Anchor createLogoutLink(String contextPath) {
        final Anchor a = populateLink(new Anchor(), VaadinIcon.SIGN_OUT, TITLE_LOGOUT);
        a.setHref(contextPath + "/logout");
        return a;
    }

    private static <T extends HasComponents> T populateLink(T a, VaadinIcon icon, String title) {
        a.add(icon.create());
        a.add(title);
        return a;
    }

    @Override
    public void receiveBroadcast(final String message, NotificationVariant notificationVariant) {
        // Must lock the session to execute logic safely
        getUI().get().access(() -> {
            if (message.equals("reload")) {
                getUI().get().getPage().reload();
            } else {
                Span content = new Span(message);
                Notification notification = new Notification(content);
                notification.addThemeVariants(notificationVariant);
                notification.setPosition(Notification.Position.TOP_END);
                notification.open();
                notification.setDuration(10000);
                content.addClickListener(event -> notification.close());
            }
        });
    }


}