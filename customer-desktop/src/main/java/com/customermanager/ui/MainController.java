package com.customermanager.ui;

import com.customermanager.model.Customer;
import com.customermanager.service.CustomerService;
import com.customermanager.util.ApiResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * The main application window.
 * Contains the toolbar (search + buttons), the customer table, and a status bar.
 */
public class MainController extends BorderPane {

    // ── Services & state ──────────────────────────────────────────────────────
    private final CustomerService service = new CustomerService();
    private final ObservableList<Customer> customerData = FXCollections.observableArrayList();
    private final Stage ownerStage;

    // ── UI controls ───────────────────────────────────────────────────────────
    private final TableView<Customer> table = new TableView<>();
    private final TextField searchField     = new TextField();
    private final Label statusLabel         = new Label("Ready");
    private final ProgressIndicator spinner = new ProgressIndicator();
    private final Button addBtn             = iconButton("＋ Add Customer", "#3b82f6", "white");
    private final Button editBtn            = iconButton("✎ Edit",          "#f59e0b", "white");
    private final Button deleteBtn          = iconButton("✕ Delete",        "#ef4444", "white");
    private final Button refreshBtn         = iconButton("↺ Refresh",       "#64748b", "white");

    public MainController(Stage owner) {
        this.ownerStage = owner;
        buildUI();
        loadCustomers();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI construction
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUI() {
        setStyle("-fx-background-color: #f8fafc;");

        setTop(buildHeader());
        setCenter(buildTableArea());
        setBottom(buildStatusBar());
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private VBox buildHeader() {
        // App title
        Label appTitle = new Label("Customer Manager");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        appTitle.setTextFill(Color.web("#1e293b"));

        Label subtitle = new Label("Manage your customers via the REST API");
        subtitle.setFont(Font.font("System", 12));
        subtitle.setTextFill(Color.web("#64748b"));

        VBox titleBox = new VBox(2, appTitle, subtitle);

        // Search
        searchField.setPromptText("🔍  Search by name, email or phone...");
        searchField.setPrefWidth(280);
        searchField.setPrefHeight(36);
        searchField.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 8; " +
                             "-fx-background-radius: 8; -fx-padding: 0 10;");

        Button searchBtn = iconButton("Search", "#3b82f6", "white");
        searchBtn.setOnAction(e -> onSearch());
        searchField.setOnAction(e -> onSearch());

        Button clearBtn = iconButton("Clear", "#94a3b8", "white");
        clearBtn.setOnAction(e -> { searchField.clear(); loadCustomers(); });

        HBox searchBox = new HBox(8, searchField, searchBtn, clearBtn);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        addBtn.setOnAction(e -> onAdd());
        editBtn.setOnAction(e -> onEdit());
        deleteBtn.setOnAction(e -> onDelete());
        refreshBtn.setOnAction(e -> loadCustomers());

        editBtn.setDisable(true);
        deleteBtn.setDisable(true);

        HBox actionBox = new HBox(8, addBtn, editBtn, deleteBtn, refreshBtn);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(actionBox, Priority.ALWAYS);

        HBox toolbar = new HBox(16, searchBox, actionBox);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Enable edit/delete only when a row is selected
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean hasSelection = selected != null;
            editBtn.setDisable(!hasSelection);
            deleteBtn.setDisable(!hasSelection);
        });

        VBox header = new VBox(12, titleBox, toolbar);
        header.setPadding(new Insets(20, 20, 16, 20));
        header.setStyle("-fx-background-color: white; " +
                        "-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        return header;
    }

    // ── Table area ────────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private StackPane buildTableArea() {
        // Columns
        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(140);

        TableColumn<Customer, String> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(c -> {
            String raw = c.getValue().getCreatedAt();
            if (raw == null) return new SimpleStringProperty("—");
            // Show only the date part for brevity
            return new SimpleStringProperty(raw.length() > 10 ? raw.substring(0, 10) : raw);
        });
        createdCol.setPrefWidth(120);

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, createdCol);
        table.setItems(customerData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No customers found."));
        table.setStyle("-fx-background-color: white;");

        // Row hover style
        table.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.hoverProperty().addListener((obs, wasHovered, isHovered) ->
                    row.setStyle(isHovered ? "-fx-background-color: #eff6ff;" : ""));
            return row;
        });

        // Double-click to edit
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                onEdit();
            }
        });

        // Spinner overlay
        spinner.setMaxSize(48, 48);
        spinner.setVisible(false);

        StackPane stack = new StackPane(table, spinner);
        stack.setPadding(new Insets(12, 20, 12, 20));
        return stack;
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private HBox buildStatusBar() {
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setTextFill(Color.web("#64748b"));

        Label countLabel = new Label();
        customerData.addListener((javafx.collections.ListChangeListener<Customer>) c ->
                countLabel.setText(customerData.size() + " customer(s)"));
        countLabel.setFont(Font.font("System", 12));
        countLabel.setTextFill(Color.web("#94a3b8"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(10, statusLabel, spacer, countLabel);
        bar.setPadding(new Insets(8, 20, 10, 20));
        bar.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CRUD actions
    // ─────────────────────────────────────────────────────────────────────────

    private void onAdd() {
        CustomerFormDialog dialog = new CustomerFormDialog(ownerStage, null);
        dialog.showAndWait();
        Customer customer = dialog.getResult();
        if (customer == null) return;

        runAsync(
            () -> service.createCustomer(customer),
            response -> {
                if (response.isSuccess()) {
                    customerData.add(response.getData());
                    showStatus("✔  Customer \"" + response.getData().getName() + "\" added.", false);
                } else {
                    showError("Failed to add customer", response.getErrorMessage());
                }
            }
        );
    }

    private void onEdit() {
        Customer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        CustomerFormDialog dialog = new CustomerFormDialog(ownerStage, selected);
        dialog.showAndWait();
        Customer updated = dialog.getResult();
        if (updated == null) return;

        runAsync(
            () -> service.updateCustomer(selected.getId(), updated),
            response -> {
                if (response.isSuccess()) {
                    int idx = customerData.indexOf(selected);
                    if (idx >= 0) customerData.set(idx, response.getData());
                    showStatus("✔  Customer updated.", false);
                } else {
                    showError("Failed to update customer", response.getErrorMessage());
                }
            }
        );
    }

    private void onDelete() {
        Customer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Customer");
        confirm.setHeaderText("Delete \"" + selected.getName() + "\"?");
        confirm.setContentText("This action cannot be undone.");
        confirm.initOwner(ownerStage);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        runAsync(
            () -> service.deleteCustomer(selected.getId()),
            response -> {
                if (response.isSuccess()) {
                    customerData.remove(selected);
                    showStatus("✔  Customer deleted.", false);
                } else {
                    showError("Failed to delete customer", response.getErrorMessage());
                }
            }
        );
    }

    private void onSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) { loadCustomers(); return; }

        runAsync(
            () -> service.searchCustomers(query),
            response -> {
                if (response.isSuccess()) {
                    customerData.setAll(response.getData());
                    showStatus("Found " + response.getData().size() + " result(s) for \"" + query + "\".", false);
                } else {
                    showError("Search failed", response.getErrorMessage());
                }
            }
        );
    }

    private void loadCustomers() {
        runAsync(
            service::getAllCustomers,
            response -> {
                if (response.isSuccess()) {
                    List<Customer> list = response.getData();
                    customerData.setAll(list != null ? list : List.of());
                    showStatus("Loaded " + customerData.size() + " customer(s).", false);
                } else {
                    showError("Could not load customers", response.getErrorMessage());
                }
            }
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Async helper (runs API call off the FX thread, then updates UI on it)
    // ─────────────────────────────────────────────────────────────────────────

    private <T> void runAsync(java.util.function.Supplier<ApiResponse<T>> apiCall,
                               java.util.function.Consumer<ApiResponse<T>> onDone) {
        setLoading(true);
        Task<ApiResponse<T>> task = new Task<>() {
            @Override protected ApiResponse<T> call() { return apiCall.get(); }
        };
        task.setOnSucceeded(e -> {
            setLoading(false);
            onDone.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            setLoading(false);
            showError("Unexpected error", task.getException().getMessage());
        });
        new Thread(task, "api-thread").start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Utility helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        spinner.setVisible(loading);
        addBtn.setDisable(loading);
        refreshBtn.setDisable(loading);
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setTextFill(isError ? Color.web("#ef4444") : Color.web("#22c55e"));
        statusLabel.setText(message);
    }

    private void showError(String title, String detail) {
        showStatus("✖  " + title, true);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(detail != null ? detail : "Unknown error occurred.");
        alert.initOwner(ownerStage);
        alert.showAndWait();
    }

    private static Button iconButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setPrefHeight(34);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; " +
                "-fx-border-radius: 7; -fx-background-radius: 7; " +
                "-fx-padding: 0 14; -fx-font-size: 12px; -fx-font-weight: bold;", bg, fg));
        return btn;
    }
}
