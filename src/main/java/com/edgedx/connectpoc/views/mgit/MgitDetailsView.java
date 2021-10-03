package com.edgedx.connectpoc.views.mgit;

import com.edgedx.connectpoc.views.MainView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import static com.edgedx.connectpoc.views.ViewsConstants.*;

@Route(value = PAGE_MGIT_DETAIL, layout = MainView.class)
@PageTitle(TITLE_MGIT_DETAIL)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class MgitDetailsView extends VerticalLayout {

    public MgitDetailsView(){

    }
}
