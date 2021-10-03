package com.edgedx.connectpoc.views.device;

import com.edgedx.connectpoc.entity.Device;
import com.edgedx.connectpoc.repository.DeviceRepository;
import com.edgedx.connectpoc.views.MainView;
import com.edgedx.connectpoc.views.cartridges.CartridgeInventoryView;
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

import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_DEVICE_LIST;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_DEVICE_LIST;

@Route(value = PAGE_DEVICE_LIST, layout = MainView.class)
@PageTitle(TITLE_DEVICE_LIST)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class DeviceListView extends VerticalLayout {

    private final DeviceRepository deviceRepository;

    Grid<Device> grid = new Grid<>(Device.class);

    public DeviceListView(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
        setSizeFull();
        H3 title = new H3();
        title.setText("Device List");
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
        grid.setColumns("serialNumber");
        grid.getColumnByKey("serialNumber");
        grid.addColumn(device -> device.getDeviceType().getDescription()).setHeader("Device Type");

        grid.setItems(deviceRepository.findAll());
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        add(grid);

        Button addDevice = new Button("Add device");
        addDevice.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button editDevice = new Button("Edit selected");
        editDevice.setEnabled(false);

        Button cartridgeInventory = new Button("Cartridge Inventory");
        cartridgeInventory.setEnabled(false);

        grid.addSelectionListener(selectionEvent -> {
            if (!selectionEvent.getAllSelectedItems().isEmpty()) {
                editDevice.setEnabled(true);
                cartridgeInventory.setEnabled(true);
            }
            else {
                editDevice.setEnabled(false);
                cartridgeInventory.setEnabled(false);
            }
        });
        HorizontalLayout buttonLayout = new HorizontalLayout(addDevice, editDevice, cartridgeInventory);
        buttonLayout.setWidthFull();
        addDevice.addClickListener(buttonClickEvent -> {
            this.getUI().ifPresent(ui ->
                    ui.navigate(DeviceDetailView.class));
        });

        editDevice.addClickListener(buttonClickEvent -> {
            Optional<Device> selected = grid.getSelectedItems().stream().findFirst();
            ComponentUtil.setData(UI.getCurrent(), Device.class, selected.get());
            this.getUI().ifPresent(ui ->
                    ui.navigate(DeviceDetailView.class));
        });

        cartridgeInventory.addClickListener(buttonClickEvent -> {
            Optional<Device> selected = grid.getSelectedItems().stream().findFirst();
            ComponentUtil.setData(UI.getCurrent(), Device.class, selected.get());
            this.getUI().ifPresent(ui ->
                    ui.navigate(CartridgeInventoryView.class));
        });

        add(buttonLayout);
    }
}
