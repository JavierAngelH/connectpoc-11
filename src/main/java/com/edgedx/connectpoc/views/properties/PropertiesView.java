package com.edgedx.connectpoc.views.properties;

import com.edgedx.connectpoc.entity.Property;
import com.edgedx.connectpoc.repository.PropertiesRepository;
import com.edgedx.connectpoc.views.MainView;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_PROPERTIES;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_PROPERTIES;

@Route(value = PAGE_PROPERTIES, layout = MainView.class)
@PageTitle(TITLE_PROPERTIES)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class PropertiesView extends VerticalLayout {

    private final PropertiesRepository propertiesRepository;

    Grid<Property> grid = new Grid<>(Property.class);

    public PropertiesView(PropertiesRepository propertiesRepository) {
        this.propertiesRepository = propertiesRepository;
        setSizeFull();

        H3 title = new H3();
        title.setText("Properties");
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
        grid.setColumns("propertyName", "propertyValue");
        grid.getColumnByKey("propertyName").setHeader("Property");
        grid.getColumnByKey("propertyValue").setHeader("Value");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM")))
            grid.setItems(propertiesRepository.findAll());
        else
            grid.setItems(propertiesRepository.findAllByScope("USER"));

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        add(grid);

        Button editProperty = new Button("Edit selected");
        editProperty.setEnabled(false);
        editProperty.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        grid.addSelectionListener(selectionEvent -> {
            if (!selectionEvent.getAllSelectedItems().isEmpty())
                editProperty.setEnabled(true);
            else
                editProperty.setEnabled(false);
        });
        HorizontalLayout buttonLayout = new HorizontalLayout(editProperty);
        buttonLayout.setWidthFull();

        editProperty.addClickListener(buttonClickEvent -> {
            Optional<Property> selected = grid.getSelectedItems().stream().findFirst();
            ComponentUtil.setData(UI.getCurrent(), Property.class, selected.get());
            this.getUI().ifPresent(ui ->
                    ui.navigate(PropertyDetailView.class));
        });

        add(buttonLayout);

    }
}
