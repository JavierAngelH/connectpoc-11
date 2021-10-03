package com.edgedx.connectpoc.views.problemnotification;

import com.edgedx.connectpoc.views.MainView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_PROBLEMNOTIFICATION;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_PROBLEMNOTIFICATION;

@Route(value = PAGE_PROBLEMNOTIFICATION, layout = MainView.class)
@PageTitle(TITLE_PROBLEMNOTIFICATION)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class ProblemNotificationView extends VerticalLayout {

    public ProblemNotificationView(){
        
    }
}
