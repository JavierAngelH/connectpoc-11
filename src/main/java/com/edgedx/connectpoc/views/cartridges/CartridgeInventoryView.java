package com.edgedx.connectpoc.views.cartridges;

import com.edgedx.connectpoc.entity.CartridgeInventory;
import com.edgedx.connectpoc.entity.Device;
import com.edgedx.connectpoc.repository.CartridgeInventoryRepository;
import com.edgedx.connectpoc.views.MainView;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_CARTRIDGE_INVENTORY;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_CARTRIDGE_INVENTORY;

@Route(value = PAGE_CARTRIDGE_INVENTORY, layout = MainView.class)
@PageTitle(TITLE_CARTRIDGE_INVENTORY)
@Secured({"ROLE_USER", "ROLE_SYSTEM", "ROLE_VIEWER"})
public class CartridgeInventoryView extends VerticalLayout {

    private final CartridgeInventoryRepository cartridgeInventoryRepository;

    Grid<CartridgeInventory> grid = new Grid<>(CartridgeInventory.class);

    public CartridgeInventoryView(CartridgeInventoryRepository cartridgeInventoryRepository) {
        setSizeFull();
        this.cartridgeInventoryRepository = cartridgeInventoryRepository;

        H3 title = new H3();
        title.setText("Cartridge Inventory");
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
        grid.setColumns("lotId", "expirationDate", "quantity");
        grid.getColumnByKey("lotId").setTextAlign(ColumnTextAlign.CENTER);
        grid.getColumnByKey("quantity").setTextAlign(ColumnTextAlign.CENTER);
        grid.getColumnByKey("expirationDate").setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(cartridgeInventory -> cartridgeInventory.getCartridgeType().getDescription()).setHeader("Type").setTextAlign(ColumnTextAlign.CENTER);
        Device device = ComponentUtil.getData(UI.getCurrent(), Device.class);
        if (device != null) {
            grid.setItems(cartridgeInventoryRepository.findAllByDevice(device));
        } else {
            grid.setItems(cartridgeInventoryRepository.findAll());
        }
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        add(grid);

        Button addCartridge = new Button("Add cartridge");
        addCartridge.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button editCartridge = new Button("Edit selected");
        editCartridge.setEnabled(false);

        grid.addSelectionListener(selectionEvent -> {
            if (!selectionEvent.getAllSelectedItems().isEmpty())
                editCartridge.setEnabled(true);
            else
                editCartridge.setEnabled(false);

        });
        HorizontalLayout buttonLayout = new HorizontalLayout(addCartridge, editCartridge);
        buttonLayout.setWidthFull();
        addCartridge.addClickListener(buttonClickEvent -> {
            ComponentUtil.setData(UI.getCurrent(), CartridgeInventory.class, null);
            this.getUI().ifPresent(ui ->
                    ui.navigate(CartridgeDetailView.class));
        });

        editCartridge.addClickListener(buttonClickEvent -> {
            Optional<CartridgeInventory> selected = grid.getSelectedItems().stream().findFirst();
            ComponentUtil.setData(UI.getCurrent(), CartridgeInventory.class, selected.get());
            this.getUI().ifPresent(ui ->
                    ui.navigate(CartridgeDetailView.class));
        });

        add(buttonLayout);
    }
}
