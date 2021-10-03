package com.edgedx.connectpoc.views.login;

import com.edgedx.connectpoc.security.SecurityUtils;
import com.edgedx.connectpoc.views.dashboard.DashboardView;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.*;

import static com.edgedx.connectpoc.views.ViewsConstants.VIEWPORT;

@Route
@PageTitle("ConnectPOC")
@JsModule("./styles/shared-styles.js")
@Viewport(VIEWPORT)
public class LoginView extends LoginOverlay
		implements AfterNavigationObserver, BeforeEnterObserver {

	public LoginView() {
		LoginI18n i18n = LoginI18n.createDefault();
		i18n.setHeader(new LoginI18n.Header());
		i18n.getHeader().setTitle("ConnectPOC");
		i18n.setAdditionalInformation(null);
		i18n.setForm(new LoginI18n.Form());
		i18n.getForm().setSubmit("Sign in");
		i18n.getForm().setTitle("Sign in");
		i18n.getForm().setUsername("Username");
		i18n.getForm().setPassword("Password");
		setI18n(i18n);
		setForgotPasswordButtonVisible(false);
		setAction("login");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (SecurityUtils.isUserLoggedIn()) {
			event.forwardTo(DashboardView.class);
		} else {
			setOpened(true);
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		setError(
				event.getLocation().getQueryParameters().getParameters().containsKey(
						"error"));
	}

}
