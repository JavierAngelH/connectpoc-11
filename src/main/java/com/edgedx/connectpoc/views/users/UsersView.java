package com.edgedx.connectpoc.views.users;

import com.edgedx.connectpoc.entity.User;
import com.edgedx.connectpoc.repository.UserRepository;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.annotation.Secured;

import java.util.Locale;
import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_USERS;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_USERS;

@Route(value = PAGE_USERS, layout = MainView.class)
@PageTitle(TITLE_USERS)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class UsersView extends VerticalLayout {

    private final UserRepository userRepository;

    Grid<User> grid = new Grid<>(User.class);

    public UsersView(UserRepository userRepository){
        this.userRepository = userRepository;
        setSizeFull();
        H3 title = new H3();
        title.setText("Users");
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
        grid.setColumns("username");
        grid.addColumn(user -> getRoleName(user.getRole())).setHeader("Role");
        grid.setItems(userRepository.findAll());
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        add(grid);

        Button addUser = new Button("Add user");
        addUser.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button editUser = new Button("Edit selected");
        editUser.setEnabled(false);

        grid.addSelectionListener(selectionEvent -> {
            if (!selectionEvent.getAllSelectedItems().isEmpty())
                editUser.setEnabled(true);
            else
                editUser.setEnabled(false);

        });
        HorizontalLayout buttonLayout = new HorizontalLayout(addUser, editUser);
        buttonLayout.setWidthFull();
        addUser.addClickListener(buttonClickEvent -> {
            ComponentUtil.setData(UI.getCurrent(), User.class, null);
            this.getUI().ifPresent(ui ->
                    ui.navigate(UserDetailView.class));
        });

        editUser.addClickListener(buttonClickEvent -> {
            Optional<User> selected = grid.getSelectedItems().stream().findFirst();
            ComponentUtil.setData(UI.getCurrent(), User.class, selected.get());
            this.getUI().ifPresent(ui ->
                    ui.navigate(UserDetailView.class));
        });

        add(buttonLayout);
    }

    private String getRoleName(String role){
        String name = role.replace("ROLE_", "").toLowerCase(Locale.ROOT);
        return StringUtils.capitalize(name);
    }
}
