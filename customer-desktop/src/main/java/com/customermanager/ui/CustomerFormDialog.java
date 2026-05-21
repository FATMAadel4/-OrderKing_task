package com.customermanager.ui;

import com.customermanager.model.Customer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

/**
 * Modal dialog used for both "Add Customer" and "Edit Customer".
 * Returns the filled-in Customer on OK, null on Cancel.
 */
public class CustomerFormDialog extends Stage {

    private final TextField nameField  = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();

    private final Label nameError  = errorLabel();
    private final Label emailError = errorLabel();
    private final Label phoneError = errorLabel();

    private Customer result = null;

    public CustomerFormDialog(Stage owner, Customer existing) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        setTitle(existing == null ? "Add New Customer" : "Edit Customer");

        // Pre-fill if editing
        if (existing != null) {
            nameField.setText(existing.getName());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
        }

        // ── Layout ────────────────────────────────────────────────────────────
        VBox root = new VBox(12);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #ffffff;");

        Label title = new Label(existing == null ? "Add New Customer" : "Edit Customer");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#1e293b"));

        root.getChildren().addAll(
                title,
                formRow("Full Name *", nameField, nameError),
                formRow("Email *",     emailField, emailError),
                formRow("Phone",       phoneField, phoneError),
                buildButtons(existing)
        );

        Scene scene = new Scene(root, 400, 340);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css") != null
                        ? getClass().getResource("/styles.css").toExternalForm()
                        : "");
        setScene(scene);
    }

    // ── Form row helper ───────────────────────────────────────────────────────
    private VBox formRow(String labelText, TextField field, Label error) {
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lbl.setTextFill(Color.web("#475569"));

        field.setPrefHeight(36);
        field.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 6; " +
                       "-fx-background-radius: 6; -fx-padding: 0 8;");

        VBox box = new VBox(4, lbl, field, error);
        return box;
    }

    private Label errorLabel() {
        Label lbl = new Label();
        lbl.setTextFill(Color.web("#ef4444"));
        lbl.setFont(Font.font("System", 11));
        lbl.setVisible(false);
        lbl.setManaged(false);
        return lbl;
    }

    // ── Buttons row ───────────────────────────────────────────────────────────
    private HBox buildButtons(Customer existing) {
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(90);
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                           "-fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6;");
        cancelBtn.setOnAction(e -> close());

        Button saveBtn = new Button(existing == null ? "Add Customer" : "Save Changes");
        saveBtn.setPrefWidth(130);
        saveBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                         "-fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> onSave());

        HBox box = new HBox(10, cancelBtn, saveBtn);
        box.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(box, new Insets(8, 0, 0, 0));
        return box;
    }

    // ── Validation + result ───────────────────────────────────────────────────
    private void onSave() {
        boolean valid = true;

        // Reset
        setError(nameError, null);
        setError(emailError, null);
        setError(phoneError, null);

        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty()) {
            setError(nameError, "Name is required.");
            valid = false;
        } else if (name.length() > 100) {
            setError(nameError, "Name must be ≤ 100 characters.");
            valid = false;
        }

        if (email.isEmpty()) {
            setError(emailError, "Email is required.");
            valid = false;
        } else if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setError(emailError, "Enter a valid email address.");
            valid = false;
        }

        if (!phone.isEmpty() && !phone.matches("^[+\\d\\s()\\-]{5,20}$")) {
            setError(phoneError, "Enter a valid phone number.");
            valid = false;
        }

        if (!valid) return;

        result = new Customer(name, email, phone);
        close();
    }

    private void setError(Label label, String message) {
        if (message == null || message.isEmpty()) {
            label.setVisible(false);
            label.setManaged(false);
        } else {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    /** @return the Customer entered by the user, or null if cancelled. */
    public Customer getResult() { return result; }
}
