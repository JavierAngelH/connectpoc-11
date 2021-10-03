package com.edgedx.connectpoc.views.users;

import com.edgedx.connectpoc.entity.User;
import com.edgedx.connectpoc.repository.UserRepository;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_USER_DETAIL;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_USER_DETAIL;

@Route(value = PAGE_USER_DETAIL, layout = MainView.class)
@PageTitle(TITLE_USER_DETAIL)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class UserDetailView extends VerticalLayout {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    User user = new User();

    public UserDetailView(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        H3 title = new H3();

        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        User userParam = ComponentUtil.getData(UI.getCurrent(), User.class);
        if(userParam!=null){
            user = userParam;
            title.setText("Edit User");
        }
        else{
            title.setText("Add User");
        }

        FormLayout form = new FormLayout();
        form.setSizeFull();

        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");

        Select<String> role = new Select<String>();
        role.setLabel("Role");
        role.setItems(new String[]{"ROLE_SYSTEM", "ROLE_USER", "ROLE_VIEWER"});
        role.setItemLabelGenerator(role1 -> getRoleName(role1));

        BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);
        binder.setBean(user);

        binder.forField(username).bind("username");
        binder.forField(password).bind(user -> password.getEmptyValue(), (user, pass) -> {
                    if (!password.getEmptyValue().equals(pass) && !password.getValue().equals(user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(pass));
                    }
                });
        binder.forField(role).bind("role");

        form.add(role, username, password);
        add(form);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);


        HorizontalLayout buttonLayout = new HorizontalLayout(save, cancel);
        buttonLayout.setWidthFull();
        cancel.addClickListener(buttonClickEvent -> {
            this.getUI().ifPresent(ui ->
                    ui.navigate(UsersView.class));
        });

        save.addClickListener(buttonClickEvent -> {
            binder.validate();
            if(binder.isValid())
            {
                userRepository.save(user);
                Notification notification = new Notification();
                notification.setText("Information saved succesfully");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(ViewsConstants.NOTIFICATION_DURATION);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.open();
                this.getUI().ifPresent(ui ->
                        ui.navigate(UsersView.class));
            }
        });

        add(buttonLayout);
    }

    private String getRoleName(String role){
        String name = role.replace("ROLE_", "").toLowerCase(Locale.ROOT);
        return StringUtils.capitalize(name);
    }
}
